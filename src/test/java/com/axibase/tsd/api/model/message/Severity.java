package com.axibase.tsd.api.model.message;

public enum Severity {
    UNDEFINED(0),
    UNKNOWN(1),
    NORMAL(2),
    WARNING(3),
    MINOR(4),
    MAJOR(5),
    CRITICAL(6),
    FATAL(7);
    private int numVal;

    Severity(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }

    public static String[] names() {
        Severity[] severities = values();
        String[] names = new String[severities.length];

        for (int i = 0; i < severities.length; i++) {
            names[i] = severities[i].name();
        }
        return names;
    }
}

