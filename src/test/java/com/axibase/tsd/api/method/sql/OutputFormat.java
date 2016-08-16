package com.axibase.tsd.api.method.sql;

/**
 * @author Igor Shmagrinskiy
 */
public enum OutputFormat {
    JSON("json"),
    CSV("csv");

    private String text;

    OutputFormat(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
