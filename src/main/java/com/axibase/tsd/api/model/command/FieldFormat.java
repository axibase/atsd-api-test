package com.axibase.tsd.api.model.command;


import org.apache.commons.lang3.StringUtils;

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
        if (s == null) {
            return null;
        } else if ("".equals(s)) {
            return "\"\"";
        }
        final String withReplacedQuotes = StringUtils.replace(s, "\"", "\"\"");
        final char[] escapeChars = {'=', '"', ' ', '\r', '\n', '\t'};
        return StringUtils.containsAny(withReplacedQuotes, escapeChars) ? '"' + withReplacedQuotes + '"' : withReplacedQuotes;
    }
}
