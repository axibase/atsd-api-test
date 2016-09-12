package com.axibase.tsd.api;

public class AtsdErrorMessage {
    public static final String DATE_FILTER_COMBINATION_REQUIRED = "IllegalArgumentException: Insufficient parameters. One of the following combinations is required: interval, interval + startDate, interval + endDate, startDate + endDate";
    public static final String DATE_FILTER_END_GREATER_START_REQUIRED ="IllegalArgumentException: End Date must be greater than Start Date";

    public static final String BAD_CREDENTIALS ="org.springframework.security.authentication.BadCredentialsException: Bad credentials";

    public static final String EMPTY_TAG = "IllegalArgumentException: Tag \"%s\" has empty value";

    public static final String SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL = "Column '%s' ambiguously defined";
    public static final String SQL_SYNTAX_COMPARISON_TPL = "Syntax error at line %s position %s: no viable alternative at input '%s'";
    public static final String SQL_SYNTAX_DELIMITER_TPL = "Syntax error at line %d position %d: extraneous input '%s' expecting {<EOF>, AND, OR, ORDER, GROUP, LIMIT, WITH, OPTION}";

    public static final String CANNOT_MODIFY_ENTITY_TPL = "IllegalArgumentException: Can not modify entities for entity group '%s'. Please reset expression field first.";

    public static final String UNKNOWN_ENTITY_FIELD_PREFIX = "org.codehaus.jackson.map.exc.UnrecognizedPropertyException:";
    public static final String TAG_VALUE_ARRAY_PREFIX = "org.codehaus.jackson.map.JsonMappingException: Can not deserialize instance";
    public static final String INTERPOLATE_TYPE_REQUIRED = "IllegalArgumentException: Interpolation type is required";
    public static final String AGGREGATE_NON_DETAIL_REQUIRE_PERIOD = "IllegalArgumentException: Aggregation period is required for aggregation type '%s'";
}
