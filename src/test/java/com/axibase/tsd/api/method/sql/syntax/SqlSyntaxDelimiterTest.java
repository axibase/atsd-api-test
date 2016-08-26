package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlSyntaxDelimiterTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-syntax-delimiter-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String ERROR_MESSAGE_TEMPLATE =
            "Syntax error at line %d position %d: extraneous input '%s' expecting {<EOF>, AND, OR, ORDER, GROUP, LIMIT, WITH}";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addData(new Sample("2016-06-29T08:00:00.000Z", "0"));
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * Issue #3227
     */
    @Test
    public void testResultWithoutDelimiter() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "2016-06-29T08:00:00.000Z", "0")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiter() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s';",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "2016-06-29T08:00:00.000Z", "0")
        );


        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedBySpaces() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s'  ;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "2016-06-29T08:00:00.000Z", "0")
        );


        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedByLF() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s' %n;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "2016-06-29T08:00:00.000Z", "0")
        );


        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedByCR() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s'\r;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "2016-06-29T08:00:00.000Z", "0")
        );


        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedByCRLF() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s'\r %n;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "2016-06-29T08:00:00.000Z", "0")
        );


        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedByLetter() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s' a;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(response, String.format(ERROR_MESSAGE_TEMPLATE, 2, 43, 'a'));
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedByNumber() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s' 1;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest("Query must return correct table",
                response, String.format(ERROR_MESSAGE_TEMPLATE, 2, 43, '1')
        );
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedByMultipleEOF() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s'  %n %n\r %n;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "2016-06-29T08:00:00.000Z", "0")
        );


        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSymbolsAfter() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s';123",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest("Query must return correct table",
                response, "Syntax error at line 2 position 42: extraneous input ';123' " +
                        "expecting {<EOF>, AND, OR, ORDER, GROUP, LIMIT, WITH}");
    }

    /**
     * Issue #3227
     */
    @Test
    public void testResultWithDelimiterSeparatedByAND() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %nWHERE entity='%s' AND;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest("Query must return correct table",
                response, "Syntax error at line 2 position 46: no viable alternative at input '<EOF>'");
    }
}
