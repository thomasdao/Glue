package me.thomasdao.glue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by thomasdao on 21/12/15.
 */
public class JSONHelper {

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        return objectMapper;
    }

    public static final ObjectMapper OBJECT_MAPPER = getObjectMapper();

    public static String toJSON(Object obj) {

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Pinnable fromJSON(String str, Class cls) {
        try {
            return (Pinnable) OBJECT_MAPPER.readValue(str, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
