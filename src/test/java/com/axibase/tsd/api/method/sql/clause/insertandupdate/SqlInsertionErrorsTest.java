package com.axibase.tsd.api.method.sql.clause.insertandupdate;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SqlInsertionErrorsTest extends SqlTest {
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private final InsertionType insertionType;

    @Factory(dataProvider = "insertionType", dataProviderClass = InsertionType.class)
    public SqlInsertionErrorsTest(InsertionType insertionType) {
        this.insertionType = insertionType;
    }

    @Test(
            description = "Tests that if one of the declared parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetDeclaredParameterInsertion() {
        String sqlQuery;
        String assertMessage;
        if(insertionType == InsertionType.INSERT_INTO) { //cannot create ImmutableMap with unset parameter
            sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, not_set) VALUES('%s', '%s', %d)"
                    , Mocks.metric(), Mocks.entity(), ISO_TIME, VALUE);
            assertMessage = "IllegalArgumentException: No value specified for column 'not_set'";
        } else {
            sqlQuery = String.format("UPDATE \"%s\" SET entity='%s', datetime='%s', value=%d, not_set="
                    , Mocks.metric(), Mocks.entity(), ISO_TIME, VALUE);
            assertMessage = "net.sf.jsqlparser.parser.ParseException: Encountered unexpected token:<EOF>\n" + //assert messages are different for two clauses
                    "    at line 1, column 326.\n" +
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
        }
        assertBadRequest("Invalid SQL query with unset declared parameter was accepted!", assertMessage, sqlQuery);
    }

    @Test(
            description = "Tests that if one of the required parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetRequiredParameterInsertion() {
        String sqlQuery = insertionType.insertionQuery(Mocks.metric(),
                ImmutableMap.of("entity", Mocks.entity(), "datetime", ISO_TIME)); //value is not set
        assertBadRequest("Invalid SQL query with unset required parameter was accepted!", "IllegalArgumentException: Either value or text is required",sqlQuery);
    }
}