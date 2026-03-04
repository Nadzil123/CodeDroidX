package com.codedroidx.editor;

public final class Ext {
    public static String ext(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return (i >= 0 && i < name.length()-1) ? name.substring(i+1).toLowerCase() : "";
    }
}
