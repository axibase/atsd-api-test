package com.axibase.tsd.api.model.command;


public class FieldFormat {
    private static String simple(String field, String value) {
        return String.format(" %s:%s", field, value);
    }

    public static String quoted(String field, String value) {
        return simple(field, escape(value));
    }

    public static String keyValue(String field, String key, String value) {
        return simple(field, String.format("%s=%s", escape(key), escape(value)));
    }

    private static String escape(String s) {
        if (s.contains("\"")) {
            s = s.replaceAll("\"", "\"\"");
        }
        char[] escapeChars = {'=', '"', ' ', '\r', '\n', '\t'};
        for (char c : escapeChars) {
            if (s.indexOf(c) >= 0) {
                return String.format("\"%s\"", s);
            }
        }
        return s;
    }
}
