package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.ErrorTemplate;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



public class SqlSyntaxDelimiterTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-syntax-delimiter-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(Sample.ofDateInteger("2016-06-29T08:00:00.000Z", 0));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @Issue("3227")
    @Test
    public void testResultWithoutDelimiter() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "1467187200000",
                        "2016-06-29T08:00:00.000Z",
                        "0",
                        "null",
                        TEST_METRIC_NAME,
                        TEST_ENTITY_NAME,
                        "null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiter() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s';",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "1467187200000",
                        "2016-06-29T08:00:00.000Z",
                        "0",
                        "null",
                        TEST_METRIC_NAME,
                        TEST_ENTITY_NAME,
                        "null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedBySpaces() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s'  ;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "1467187200000",
                        "2016-06-29T08:00:00.000Z",
                        "0",
                        "null",
                        TEST_METRIC_NAME,
                        TEST_ENTITY_NAME,
                        "null")
        );


        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedByLF() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s' %n;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "1467187200000",
                        "2016-06-29T08:00:00.000Z",
                        "0",
                        "null",
                        TEST_METRIC_NAME,
                        TEST_ENTITY_NAME,
                        "null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedByCR() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s'\r;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "1467187200000",
                        "2016-06-29T08:00:00.000Z",
                        "0",
                        "null",
                        TEST_METRIC_NAME,
                        TEST_ENTITY_NAME,
                        "null")
        );


        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedByCRLF() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s'\r %n;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "1467187200000",
                        "2016-06-29T08:00:00.000Z",
                        "0",
                        "null",
                        TEST_METRIC_NAME,
                        TEST_ENTITY_NAME,
                        "null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedByLetter() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s' a;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = queryResponse(sqlQuery);

        String expectedErrorMessage = ErrorTemplate.Sql.syntaxError(2, 43,
                extraneousErrorMessage("a", "{<EOF>, '+', '-', '*', '/', '%', '!=', '<>', '<=', '>=', " +
                        "'>', '<', '=', '.', IS, AND, OR, NOT, LIKE, REGEX, IN, BETWEEN, ORDER, GROUP, LIMIT, WITH, OPTION}")
        );
        assertBadRequest(expectedErrorMessage, response);
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedByNumber() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s' 1;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = queryResponse(sqlQuery);
        String expectedMessage = ErrorTemplate.Sql.syntaxError(
                2, 43,
                extraneousErrorMessage("1", "{<EOF>, '+', '-', '*', '/', '%', '!=', '<>', '<=', '>='," +
                        " '>', '<', '=', '.', IS, AND, OR, NOT, LIKE, REGEX, IN, BETWEEN, ORDER, GROUP, LIMIT, WITH, OPTION}"
                )
        );
        assertBadRequest("Query must return correct table",
                expectedMessage, response
        );
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedByMultipleEOF() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s'  %n %n\r %n;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "1467187200000",
                        "2016-06-29T08:00:00.000Z",
                        "0",
                        "null",
                        TEST_METRIC_NAME,
                        TEST_ENTITY_NAME,
                        "null")
        );


        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3227")
    @Test
    public void testResultWithDelimiterSymbolsAfter() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s';123",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = queryResponse(sqlQuery);

        String expectedErrorMessage = ErrorTemplate.Sql.syntaxError(
                2, 42, tokenRecognitionError(";")
        );
        assertBadRequest("Query must return correct table",
                expectedErrorMessage, response);
    }

    @Issue("3227")
    @Test
    public void testResultWithDelimiterSeparatedByAND() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %nWHERE entity='%s' AND;",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        Response response = queryResponse(sqlQuery);

        String expectedMessage = ErrorTemplate.Sql.syntaxError(2, 46,
                "no viable alternative at input '<EOF>'"
        );
        assertBadRequest("Query must return correct table",
                expectedMessage, response);
    }

    private String tokenRecognitionError(String token) {
        final String template = "token recognition error at: '%s'";
        return String.format(template, token);
    }

    private String extraneousErrorMessage(String actual, String expected) {
        String template = "extraneous input '%s' expecting %s";
        return String.format(template, actual, expected);
    }
}
