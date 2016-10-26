package com.axibase.tsd.api.model.sql.operator;

public enum StringOperator {
    LIKE("LIKE"), REGEX("REGEX");
    private String text;

    StringOperator(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
