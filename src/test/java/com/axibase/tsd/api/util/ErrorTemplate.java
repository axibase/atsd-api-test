package com.axibase.tsd.api.util;

public class ErrorTemplate {
    public static final String DATE_FILTER_COMBINATION_REQUIRED = "IllegalArgumentException: Insufficient parameters. One of the following combinations is required: interval, interval + startDate, interval + endDate, startDate + endDate";
    public static final String DATE_FILTER_END_GREATER_START_REQUIRED = "IllegalArgumentException: End Date must be greater than Start Date";
    public static final String DATE_FILTER_WRONG_SYNTAX_TPL = "IllegalArgumentException: Wrong %s syntax: %s";
    public static final String DATE_FILTER_INVALID_FORMAT = "IllegalArgumentException: Invalid date format";
    public static final String JSON_MAPPING_EXCEPTION_UNEXPECTED_CHARACTER = "com.fasterxml.jackson.databind.JsonMappingException: Expected '%s' character but found '%s'";
    public static final String JSON_MAPPING_EXCEPTION_NA = "com.fasterxml.jackson.databind.JsonMappingException: N/A";


    public static final String ENTITY_FILTER_REQUIRED = "IllegalArgumentException: entity or entities or entityGroup or entityExpression must not be empty";

    public static final String BAD_CREDENTIALS = "org.springframework.security.authentication.BadCredentialsException: Bad credentials";
    public static final String USER_NOT_FOUND = "org.springframework.security.core.userdetails.UsernameNotFoundException: User 'Unknown User' not found";

    public static final String EMPTY_TAG = "IllegalArgumentException: Tag \"%s\" has empty value";

    public static final String SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL = "Column '%s' ambiguously defined";
    public static final String SQL_SYNTAX_COMPARISON_TPL = "Syntax error at line %s position %s: no viable alternative at input '%s'";
    public static final String SQL_SYNTAX_DELIMITER_TPL = "Syntax error at line %d position %d: extraneous input '%s' expecting {<EOF>, AND, OR, ORDER, GROUP, LIMIT, WITH, OPTION}";

    public static final String CANNOT_MODIFY_ENTITY_TPL = "IllegalArgumentException: Can not modify entities for entity group '%s'. Please reset expression field first.";

    public static final String UNKNOWN_ENTITY_FIELD_PREFIX = "com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:";
    public static final String TAG_VALUE_ARRAY_PREFIX = "com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance";
    public static final String INTERPOLATE_TYPE_REQUIRED = "IllegalArgumentException: Interpolation type is required";
    public static final String AGGREGATE_NON_DETAIL_REQUIRE_PERIOD = "IllegalArgumentException: Aggregation period is required for aggregation type '%s'";
}
