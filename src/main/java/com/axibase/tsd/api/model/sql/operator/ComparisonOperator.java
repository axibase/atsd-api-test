package com.axibase.tsd.api.model.sql.operator;


public enum ComparisonOperator {
    EQUALS("="), NOT_EQUALS("!="), NOT_EQUALS_ALTER("<>"), LESS("<"), LESS_OR_EQUALS("<="), GREATER(">"),
    GREATER_OR_EQUALS(">=");
    private String text;

    ComparisonOperator(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
