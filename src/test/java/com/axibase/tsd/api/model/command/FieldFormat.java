package com.axibase.tsd.api.model.command;


import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;

public class FieldFormat {
    private static String simple(String field, String value) {
        return String.format("%s:%s ", field, value);
    }

    public static String quoted(String field, String value) {
        return simple(field, escapeCsv(value));
    }

    public static String tag(String key, String value) {
        return simple("t", String.format("%s=%s", escapeCsv(key), escapeCsv(value)));
    }
}
