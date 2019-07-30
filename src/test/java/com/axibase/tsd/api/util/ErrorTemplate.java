package com.axibase.tsd.api.util;

public class ErrorTemplate {
    public static final String DATE_FILTER_COMBINATION_REQUIRED = "IllegalArgumentException: Insufficient parameters. One of the following combinations is required: interval, interval + startDate, interval + endDate, startDate + endDate";
    public static final String DATE_FILTER_END_GREATER_START_REQUIRED = "IllegalArgumentException: End Date must be greater than Start Date";
    public static final String DATE_FILTER_WRONG_SYNTAX_TPL = "IllegalArgumentException: Wrong %s syntax: %s";
    public static final String DATE_FILTER_INVALID_FORMAT = "IllegalArgumentException: Invalid date format";
    public static final String JSON_MAPPING_EXCEPTION_UNEXPECTED_CHARACTER = "com.fasterxml.jackson.databind.JsonMappingException: Expected '%s' character but found '%s'";
    public static final String JSON_MAPPING_EXCEPTION_NA = "com.fasterxml.jackson.databind.JsonMappingException: N/A";


    public static final String ENTITY_FILTER_REQUIRED = "IllegalArgumentException: entity or entities or entityGroup or entityExpression must not be empty";

    public static final String BAD_CREDENTIALS = "code 03";
    public static final String USER_NOT_FOUND = "code 02";

    public static final String EMPTY_TAG = "IllegalArgumentException: Tag \"%s\" has empty value";

    public static final String SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL = "Column '%s' ambiguously defined";
    public static final String SQL_SYNTAX_COMPARISON_TPL = "Syntax error at line %s position %s: no viable alternative at input '%s'";

    public static final String CANNOT_MODIFY_ENTITY_TPL = "IllegalArgumentException: Can not modify entities for entity group '%s'. Please reset expression field first.";

    public static final String UNKNOWN_ENTITY_FIELD_PREFIX = "com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:";
    public static final String TAG_VALUE_ARRAY_SUBSTRING = "not deserialize instance";
    public static final String INTERPOLATE_TYPE_REQUIRED = "IllegalArgumentException: Interpolation type is required";
    public static final String AGGREGATE_NON_DETAIL_REQUIRE_PERIOD = "IllegalArgumentException: Aggregation period is required for aggregation type '%s'";

    public static class Sql {
        private static final String SYNTAX_ERROR = "Syntax error at line %d position %d: %s";
        private static final String INVALID_IDENTIFIER = "Invalid identifier '%s'";
        private static final String INVALID_DATE_VALUE = "Invalid date value: '%s'";
        private static final String AMBIGUOUS_COLUMN = "Column '%s' ambiguously defined";
        public static final String MISSING_METRIC_EXPRESSION_IN_THE_WHERE_CLAUSE = "Missing metric expression in the where clause";
        public static final String AMBIGIOUSLY_CONDITION = "Condition in where clause ambiguously defined";
        public static final String DATETIME_IN_GROUP_CLAUSE = "Invalid grouping for column \"datetime\". Remove the " +
                "column from the SELECT clause, apply an aggregation function to the column, or add the column to the " +
                "GROUP BY clause.";

        public static String syntaxError(int line, int position, String message) {
            return String.format(SYNTAX_ERROR, line, position, message);
        }

        public static String ambigiouslyColumn(String columnName) {
            return String.format(AMBIGUOUS_COLUMN, columnName);
        }

        public static String invalidIdentifier(String alias) {
            return String.format(INVALID_IDENTIFIER, alias);
        }

        public static String invalidDateValue(String value) {
            return String.format(INVALID_DATE_VALUE, value);
        }
    }
}
