package com.codedroidx.editor;

import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonTools {

    public static boolean isLikelyArray(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) return c == '[';
        }
        return false;
    }

    public static String pretty(String input) throws Exception {
        if (isLikelyArray(input)) {
            JSONArray arr = new JSONArray(input);
            return arr.toString(2);
        } else {
            JSONObject obj = new JSONObject(input);
            return obj.toString(2);
        }
    }

    public static void validate(String input) throws Exception {
        if (isLikelyArray(input)) new JSONArray(input);
        else new JSONObject(input);
    }
}
