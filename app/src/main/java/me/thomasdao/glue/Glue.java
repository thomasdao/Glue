package me.thomasdao.glue;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thomasdao on 15/12/15.
 *
 * A local cache cache data into Preference. Using SQLite in Android is too complicated
 * and there is no good and flexible ORM solution.
 *
 * LocalCache will always cache data in memory and occasionally flush to Preference. To
 * cache data in memory, it uses standard HashMap data structure. LocalCache expects its
 * objects to implement Pinnable interface, where it will use "unique()" method
 * to determine the key to use.
 *
 * A object is cached by a key with format "modelName:unique". This is used as key for both
 * HashMap and Preference cache. Whenever a object is saved, we also keep track of how many
 * objects belong to a model, in a key "modelName". The value of "modelName" key is a json array
 * of all keys with that class.
 *
 */
public class Glue {

    /**
     * Keep objects in memory
     */
    private static ConcurrentHashMap<String, Pinnable> hashMap;
    private static HashMap<Class, String> modelNames;

    /**
     * Initialize Glue with a context
     * @param ctx
     */
    public static void init(Context ctx) {
        CacheHelper.init(ctx);
        hashMap = new ConcurrentHashMap<>();
        modelNames = new HashMap<>();
    }

    /************************************************************************************
     * Key for an object: "modelName:unique"
     * Key for all object belong to the same modelName: "modelName"
     ************************************************************************************/

    private static String internalKey(String modelName, String unique) {
        if (unique == null) return null;

        return modelName + ":" + unique;
    }

    private static String internalKey(Pinnable object) {
        String modelName = getModelName(object);
        if (modelName == null) return null;

        return internalKey(modelName, object.unique());
    }

    // Get all keys with same model name
    private static HashSet<String> getAllKeysForModel(String modelName) {
        // Keep track of all objects belong to a class name
        String ids = CacheHelper.getString(modelName);
        HashSet<String> keys = null;

        if (ids != null && ids.length() > 0) {
            try {
                keys = JSONHelper.OBJECT_MAPPER.readValue(ids, new TypeReference<HashSet<String>>(){});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (keys == null) {
            keys = new HashSet<>();
        }

        return keys;
    }

    // Convert a set to json string
    private static String setToString(HashSet<String> set) {
        if (set == null) return "[]";
        return JSONHelper.toJSON(set);
    }

    /**
     * Get relationships for an object using reflection
     * @param pinnable
     * @return
     */
    private static Set<Pinnable> getRelationships(Pinnable pinnable) {
        if (pinnable == null) return null;

        Set<Pinnable> relationships = new HashSet<>();

        for(Field field : pinnable.getClass().getDeclaredFields()) {
            // Do not consider static field
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // Set field accessible to avoid exception
            field.setAccessible(true);

            Class type = field.getType();

            // One to one relationship
            if (Pinnable.class.isAssignableFrom(type)) {
                try {
                    Pinnable val = (Pinnable) field.get(pinnable);
                    if (val != null) {
                        relationships.add(val);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            // One to many relationship
            if (Collection.class.isAssignableFrom(type)) {
                try {
                    Collection collection = (Collection) field.get(pinnable);
                    if (collection != null) {
                        for (Object item : collection) {
                            if (item != null) {
                                relationships.add((Pinnable) item);
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }

        return relationships;
    }

    private static String getModelName(Object obj) {
        if (obj == null) return null;
        Class cls = obj.getClass();
        return getModelNameForClass(cls);
    }

    private static String getModelNameForClass(Class cls) {
        if (!modelNames.containsKey(cls)) {
            if (cls.isAnnotationPresent(ModelName.class)) {
                ModelName annotation = (ModelName) cls.getAnnotation(ModelName.class);
                modelNames.put(cls, annotation.value());
            } else {
                throw new RuntimeException("Annotation @ModelName is missing for class: " + cls.getSimpleName());
            }
        }

        return modelNames.get(cls);
    }


    /************************************************************************************
     * GET
     ************************************************************************************/

    public static Pinnable get(String unique, Class cls) {
        String modelName = getModelNameForClass(cls);
        String key = internalKey(modelName, unique);
        return getByInternalKey(key, cls);
    }

    private static Pinnable getByInternalKey(String key, Class cls) {
        if (key == null) return null;

        Pinnable pinnable = null;

        // Check if object exist in memory cache
        if (hashMap.containsKey(key)) {
            pinnable = hashMap.get(key);
        } else {
            // If object does not exist in memory cache, load the object
            // and its relationships from preference
            String val = CacheHelper.getString(key);
            if (val != null) {
                pinnable = JSONHelper.fromJSON(val, cls);

                Set<Pinnable> relationships = getRelationships(pinnable);
                if (relationships != null && relationships.size() > 0) {
                    for (Pinnable obj : relationships) {
                        if (obj != null) {
                            get(obj.unique(), obj.getClass());
                        }
                    }
                }

                // Update memory cache
                if (pinnable != null) {
                    hashMap.put(key, pinnable);
                }
            }
        }

        return pinnable;
    }

    public static ArrayList getAll(Class cls) {
        ArrayList<Pinnable> results = new ArrayList<>();
        String modelName = getModelNameForClass(cls);
        HashSet<String> set = getAllKeysForModel(modelName);

        for (String key : set) {
            Pinnable obj = getByInternalKey(key, cls);
            if (obj != null) {
                results.add(obj);
            }
        }

        return results;
    }

    /**
     * Merge second object to first object
     * @param first
     * @param second
     * @return first object
     */
    private static Pinnable mergeObject(Pinnable first, Pinnable second) {
        if (second == null || first.equals(second)) {
            return first;
        }

        for(Field field : second.getClass().getDeclaredFields()) {
            // Do not consider static field
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // Set field accessible to avoid exception
            field.setAccessible(true);

            // Naive way to copy fields from second object to first object
            // Based on assumption that first object and second object are same class
            try {
                Object val2 = field.get(second);
                if (val2 != null) {
                    field.set(first, val2);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return first;
    }


    /**
     * Save an object to both memory and preference
     * @param object
     */
    public static void pin(Pinnable object) {
        if (object == null) return;

        ArrayList<Pinnable> list = new ArrayList<>();
        list.add(object);
        pinAll(list);
    }

    public static void pinAll(Collection collection) {
        if (collection == null || collection.size() == 0) return;


        for (Object obj : collection) {
            if (obj == null) continue;

            Class cls = obj.getClass();

            Pinnable object = (Pinnable) obj;
            Pinnable existingObj = get(object.unique(), cls);

            // Do nothing if no changes
            if (object.equals(existingObj)) {
                continue;
            }

            // Merge new object to existing object, return existing object
            if (existingObj == null) {
                existingObj = object;
            } else {
                mergeObject(existingObj, object);
            }

            // Get key
            String key = internalKey(existingObj);

            // Update memory cache
            hashMap.put(key, existingObj);

            HashMap<String, String> buffers = new HashMap<>();

            // Update Preference cache
            buffers.put(key, JSONHelper.toJSON(existingObj));

            // Keep track of all objects belong to a class name
            String modelName = getModelNameForClass(cls);
            HashSet<String> set = getAllKeysForModel(modelName);
            set.add(key);
            buffers.put(modelName, setToString(set));

            // Save to disk
            CacheHelper.save(buffers);

            // Check relationships
            Set<Pinnable> relationships = getRelationships(existingObj);
            pinAll(relationships);
        }
    }

    public static void deleteAll(String modelName) {
        HashSet<String> set = getAllKeysForModel(modelName);
        ArrayList<String> keys = new ArrayList<>();

        for (String key : set) {
            keys.add(key);
            hashMap.remove(key);
        }

        keys.add(modelName);
        CacheHelper.delete(keys);
    }

    public static void deleteObject(Pinnable object) {
        ArrayList<Pinnable> list = new ArrayList<>();
        list.add(object);
        String modelName = getModelName(object);
        deleteObjects(list, modelName);
    }

    public static void deleteObjects(Collection<Pinnable> collection, String modelName) {
        ArrayList<String> keys = new ArrayList<>();

        HashSet<String> array = getAllKeysForModel(modelName);
        HashMap<String, Boolean> map = new HashMap<>();
        for (String key : array) {
            map.put(key, Boolean.TRUE);
        }

        for (Pinnable object : collection) {
            String k = internalKey(object);
            keys.add(k);
            hashMap.remove(k);
            map.remove(k);
        }

        array = new HashSet<>();
        for (String k : map.keySet()) {
            array.add(k);
        }

        // Update underlying preference
        CacheHelper.save(modelName, setToString(array));
        CacheHelper.delete(keys);
    }

    public static void clearAll() {
        CacheHelper.clear();
        hashMap.clear();
    }

    public static void clearMemoryCache() {
        hashMap.clear();
    }
}
