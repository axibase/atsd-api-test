package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlOperatorIsNullWithMathFunctionsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-operator-is-null-with-math-functions-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.setData(Arrays.asList(
                new Sample("2016-06-29T08:00:00.000Z", "2.11"),
                new Sample("2016-06-29T08:00:01.000Z", "7.567"),
                new Sample("2016-06-29T08:00:02.000Z", "-1.23")
                )
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testIsNotNullWithMathFunction() {
        String sqlQuery = String.format(
                "SELECT SQRT(value) FROM '%s'\nWHERE entity = '%s'AND SQRT(value) IS NOT NULL",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.sqrt(2.11))),
                Collections.singletonList(Double.toString(Math.sqrt(7.567)))
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3049
     */
    @Test
    public void testIsNullWithMathFunction() {
        String sqlQuery = String.format(
                "SELECT SQRT(value) FROM '%s'\nWHERE entity = '%s'AND SQRT(value) IS NULL",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("NaN")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3049
     */
    @Test
    public void testIsNotNullWithMathFunctionAliasInOrderBy() {
        String sqlQuery = String.format(
                "SELECT SQRT(value) AS 'sqrt' FROM '%s'\nWHERE entity = '%s'AND SQRT(value) IS NOT NULL\nORDER BY 'sqrt'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.sqrt(2.11d))),
                Collections.singletonList(Double.toString(Math.sqrt(7.567d)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testIsNullWithMathFunctionAliasInOrderBy() {
        String sqlQuery = String.format(
                "SELECT SQRT(value) AS 'sqrt' FROM '%s'\nWHERE entity = '%s'AND SQRT(value) IS NULL\nORDER BY 'sqrt'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("NaN")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3049
     */
    @Test
    public void testIsNullWithMathFunctionComposeAliasInOrderBy() {
        String sqlQuery = String.format(
                "SELECT SQRT(value) AS 'sqrt' FROM '%s'\nWHERE entity = '%s'AND (SQRT(value) + ABS(value))/value IS NULL\nORDER BY 'sqrt'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }
}
