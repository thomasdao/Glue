package me.thomasdao.glue;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by thomasdao on 16/12/15.
 */
public class CacheHelper {
    private static Context context;
    private static final String PRIVATE_CACHE = "PRIVATE_CACHE";

    public static void init(Context ctx) {
        context = ctx;
    }

    public static SharedPreferences getPrivateSharedPreference() {
        return context.getSharedPreferences(PRIVATE_CACHE, Context.MODE_PRIVATE);
    }

    public static void save(String key, String val) {
        SharedPreferences preferences = getPrivateSharedPreference();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static String getString(String key) {
        SharedPreferences preferences = getPrivateSharedPreference();
        return preferences.getString(key, null);
    }


    public static void save(HashMap<String, String> map) {
        SharedPreferences preferences = getPrivateSharedPreference();
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : map.keySet()) {
            editor.putString(key, map.get(key));
        }
        editor.commit();
    }

    public static void delete(Collection<String> keys) {
        SharedPreferences preferences = getPrivateSharedPreference();
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }

    public static void clear() {
        SharedPreferences preferences = getPrivateSharedPreference();
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
