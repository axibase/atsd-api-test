package com.axibase.tsd.api.model.command;


public class FieldFormat {
    private FieldFormat() {

    }

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
        String result = s;
        if (result == null) {
            return null;
        }
        if (result.contains("\"")) {
            result = result.replaceAll("\"", "\"\"");
        }
        char[] escapeChars = {'=', '"', ' ', '\r', '\n', '\t'};
        for (char c : escapeChars) {
            if (result.indexOf(c) >= 0) {
                return String.format("\"%s\"", result);
            }
        }
        return result;
    }
}
