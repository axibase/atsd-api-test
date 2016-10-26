package com.axibase.tsd.api.model.sql.operator;


public enum IsNullOperator {
    ISNULL("IS NULL"), ISNOTNULL("IS NOT NULL");
    private String text;

    IsNullOperator(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
