package shop.xmz.lol.loratadine.antileak.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtil {
    private final JsonObject object;

    public JsonUtil(JsonObject object) {
        this.object = object;
    }

    public String getString(String name, String defaultValue) {
        if (!object.has(name)) return defaultValue;
        final JsonElement element = object.get(name);
        if (!element.isJsonPrimitive()) return defaultValue;
        return element.getAsString();
    }

    public long getLong(String name, long defaultValue) {
        if (!object.has(name)) return defaultValue;
        final JsonElement element = object.get(name);
        if (!element.isJsonPrimitive()) return defaultValue;
        return element.getAsLong();
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        if (!object.has(name)) return defaultValue;
        final JsonElement element = object.get(name);
        if (!element.isJsonPrimitive()) return defaultValue;
        return element.getAsBoolean();
    }

    public int getInt(String name, int defaultValue) {
        if (!object.has(name)) return defaultValue;
        final JsonElement element = object.get(name);
        if (!element.isJsonPrimitive()) return defaultValue;
        return element.getAsInt();
    }
}
