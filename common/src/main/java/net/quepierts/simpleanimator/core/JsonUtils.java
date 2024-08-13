package net.quepierts.simpleanimator.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {
    public static String getString(String key, JsonObject jsonObject, String str) {
        JsonElement element = jsonObject.get(key);
        if (element != null) {
            return element.getAsString();
        } else {
            return str;
        }
    }

    public static boolean getBoolean(String key, JsonObject jsonObject, boolean bl) {
        JsonElement element = jsonObject.get(key);
        if (element != null) {
            return element.getAsBoolean();
        } else {
            return bl;
        }
    }

    public static int getInt(String key, JsonObject jsonObject, int i) {
        JsonElement element = jsonObject.get(key);
        if (element != null) {
            return element.getAsInt();
        } else {
            return i;
        }
    }
}
