package com.axibase.tsd.api.method.sql.operator;


public class StringOperators {
    public enum Unary {
        ISNULL("IS NULL"), ISNOTNULL("IS NOT NULL");
        private String text;

        Unary(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum Binary {
        EQUALS("="), NOT_EQUALS("!="), NOT_EQUALS_ALTER("<>"), LESS("<"), LESS_OR_EQUALS("<="), GREATER(">"),
        GREATER_OR_EQUALS(">="), LIKE("LIKE"), REGEX("REGEX");
        private String text;

        Binary(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
