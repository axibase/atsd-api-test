package com.axibase.tsd.api.method.sql.clause.insertandupdate;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

public class SqlInsertIntoErrorsTest extends SqlTest {
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    @Test(
            description = "Tests that if one of the declared parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetDeclaredParameterInsertion() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, not_set) VALUES('%s', '%s', %d)"
                ,Mocks.metric(), Mocks.entity(), ISO_TIME, VALUE);
        String errorMessage = "IllegalArgumentException: No value specified for column 'not_set'";
        assertBadRequest("Invalid SQL query with unset declared parameter was accepted!", errorMessage, sqlQuery);
    }

    @Test(
            description = "Tests that if one of declared parameters' value is empty, error is thrown"
    )
    @Issue("5962")
    public void testEmptyDeclaredParameterInsertion(){
        String sqlQuery = InsertionType.INSERT_INTO.insertionQuery(Mocks.metric(), TestUtil.toUnmodifiableMap(
                "entity", Mocks.entity(), "datetime", ISO_TIME, "value", VALUE, "empty", null
                ));
        String errorMessage = "net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: \")\" \")\"\n" +
                "    at line 1, column 359.\n" +
                "\n" +
                "Was expecting one of:\n" +
                "\n" +
                "    \"+\"\n" +
                "    \"-\"\n" +
                "    \"?\"\n" +
                "    \"@\"\n" +
                "    \"@@\"\n" +
                "    \"ACTION\"\n" +
                "    \"ANY\"\n" +
                "    \"BYTE\"\n" +
                "    \"CASCADE\"\n" +
                "    \"CASE\"\n" +
                "    \"CAST\"\n" +
                "    \"CHAR\"\n" +
                "    \"COLUMN\"\n" +
                "    \"COLUMNS\"\n" +
                "    \"COMMENT\"\n" +
                "    \"COMMIT\"\n" +
                "    \"DO\"\n" +
                "    \"ENABLE\"\n" +
                "    \"END\"\n" +
                "    \"EXTRACT\"\n" +
                "    \"FIRST\"\n" +
                "    \"FOLLOWING\"\n" +
                "    \"GROUP_CONCAT\"\n" +
                "    \"INDEX\"\n" +
                "    \"INSERT\"\n" +
                "    \"INTERVAL\"\n" +
                "    \"ISNULL\"\n" +
                "    \"KEY\"\n" +
                "    \"LAST\"\n" +
                "    \"MATERIALIZED\"\n" +
                "    \"NO\"\n" +
                "    \"NULL\"\n" +
                "    \"NULLS\"\n" +
                "    \"OPEN\"\n" +
                "    \"OVER\"\n" +
                "    \"PARTITION\"\n" +
                "    \"PERCENT\"\n" +
                "    \"PRECISION\"\n" +
                "    \"PRIMARY\"\n" +
                "    \"PRIOR\"\n" +
                "    \"RANGE\"\n" +
                "    \"REPLACE\"\n" +
                "    \"ROW\"\n" +
                "    \"ROWS\"\n" +
                "    \"SEPARATOR\"\n" +
                "    \"SIBLINGS\"\n" +
                "    \"TABLE\"\n" +
                "    \"TEMP\"\n" +
                "    \"TEMPORARY\"\n" +
                "    \"TRUNCATE\"\n" +
                "    \"TYPE\"\n" +
                "    \"UNSIGNED\"\n" +
                "    \"VALUE\"\n" +
                "    \"VALUES\"\n" +
                "    \"XML\"\n" +
                "    \"ZONE\"\n" +
                "    \"{d\"\n" +
                "    \"{t\"\n" +
                "    \"{ts\"\n" +
                "    \"~\"\n" +
                "    <K_DATETIMELITERAL>\n" +
                "    <K_TIME_KEY_EXPR>\n" +
                "    <S_CHAR_LITERAL>\n" +
                "    <S_DOUBLE>\n" +
                "    <S_HEX>\n" +
                "    <S_IDENTIFIER>\n" +
                "    <S_LONG>\n" +
                "    <S_QUOTED_IDENTIFIER>\n";
        assertBadRequest("Invalid SQL query with empty declared parameter was accepted!", errorMessage, sqlQuery);
    }

    @Test(
            description = "Tests that if one of the required parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetRequiredParameterInsertion() {
        String sqlQuery = InsertionType.INSERT_INTO.insertionQuery(Mocks.metric(),
                ImmutableMap.of("entity", Mocks.entity(), "datetime", ISO_TIME)); //value is not set
        assertBadRequest("Invalid SQL query with unset required parameter was accepted!", "IllegalArgumentException: Either value or text is required",sqlQuery);
    }
}