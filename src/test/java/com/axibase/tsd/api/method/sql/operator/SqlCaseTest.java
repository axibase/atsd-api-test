package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;


public class SqlCaseTest extends SqlTest {
    private static final String TEST_METRIC_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME, "tag1", "abc", "tag2", "123");

        series.addSamples(
                Sample.ofDateInteger("2016-06-03T09:20:01.000Z", 1),
                Sample.ofDateInteger("2016-06-03T09:20:02.000Z", 15),
                Sample.ofDateInteger("2016-06-03T09:20:03.000Z", 40)
        );

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #3421
     */
    @Test
    public void testCaseThatGivesCharactersInSelect() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN t1.value > 30 THEN 'b'" +
                        " WHEN t1.value <= 1 THEN 'a'" +
                        " ELSE 'c' END" +
                        " FROM \"%s\" t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"a"},
                {"c"},
                {"b"}
        };

        assertSqlQueryRows("CASE in SELECT gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseInSelectWithAggregateExpressions() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN avg(t1.value) > 2 THEN 'b'" +
                        " ELSE 'c' END" +
                        " FROM \"%s\" t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"b"}
        };

        assertSqlQueryRows("CASE in SELECT with Aggregate Expressions gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseReturnsNull() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN t1.value = 30 THEN 'b' END" +
                        " FROM \"%s\" t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"null"},
                {"null"},
                {"null"}
        };

        assertSqlQueryRows("CASE not returning null", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseNullCheck() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN t1.value IS NOT NULL THEN 'ok' END" +
                        " FROM \"%s\" t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"ok"},
                {"ok"},
                {"ok"}
        };

        assertSqlQueryRows("CASE can not check for null", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseWithTagThatGivesNumbersInSelect() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN t1.tags.tag1 = 'abc' THEN t1.value" +
                        " ELSE 'error' END" +
                        " FROM \"%s\" t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"15"},
                {"40"}
        };

        assertSqlQueryRows("CASE with tags in SELECT gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseWithLocateInSelect() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN LOCATE('b', t1.tags.tag1) = 2 THEN t1.value" +
                        " ELSE 'error' END" +
                        " FROM \"%s\" t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"15"},
                {"40"}
        };

        assertSqlQueryRows("CASE with LOCATE in SELECT gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseInSelectWithGrouping() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN t1.value > 20 THEN t1.value" +
                        " ELSE tags.tag2 END as \"ATTRIBUTE\"," +
                        " sum(value)" +
                        " FROM \"%s\" t1" +
                        " GROUP BY \"ATTRIBUTE\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"123", "16"},
                {"40", "40"}
        };

        assertSqlQueryRows("CASE in SELECT with grouping gives wrong result", expectedRows, sqlQuery);
    }

    // Add tests to cover CASE expression usage in GROUP, and HAVING clauses.

    /**
     * #3421
     */
    @Test
    public void testCaseInWhere() {
        String sqlQuery = String.format(
                "SELECT value" +
                        " FROM \"%s\" t1" +
                        " WHERE CASE WHEN t1.value > 10 THEN t1.value < 35" +
                        " ELSE t1.value > 10 END",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"15"}
        };

        assertSqlQueryRows("CASE in WHERE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseInGroupBy() {
        String sqlQuery = String.format(
                "SELECT sum(value)" +
                        " FROM \"%s\" t1" +
                        " GROUP BY CASE WHEN t1.value > 10 THEN tags.tag1" +
                        " ELSE tags.tag2 END",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"55"}
        };

        assertSqlQueryRows("CASE in GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3421
     */
    @Test
    public void testCaseInHaving() {
        String sqlQuery = String.format(
                "SELECT sum(value)" +
                        " FROM \"%s\" t1" +
                        " GROUP BY CASE WHEN t1.value > 10 THEN tags.tag1" +
                        " ELSE tags.tag2 END" +
                        " HAVING CASE WHEN sum(value) > 10 THEN avg(value) > 10" +
                        " ELSE avg(value) > 5 END",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"55"}
        };

        assertSqlQueryRows("CASE in HAVING gives wrong result", expectedRows, sqlQuery);
    }

    /**
     *  #3913
     */
    @Test
    public void testCaseInExpression() throws Exception {
        String sqlQuery = String.format(
                "SELECT 100 - CASE WHEN value < 30 THEN value ELSE 100 END FROM \"%s\"",
                TEST_METRIC_NAME);

        String[][] expectedRows = {
                {"99"},
                {"85"},
                {"0"}
        };

        assertSqlQueryRows(
                "Incorrect query result with CASE operator in expression",
                expectedRows,
                sqlQuery);
    }

    /**
     *  #3913
     */
    @Test
    public void testCaseInAggregationFunction() throws Exception {
        String sqlQuery = String.format(
                "SELECT SUM(100 - CASE WHEN value < 30 THEN value ELSE 100 END) FROM \"%s\"",
                TEST_METRIC_NAME);

        String[][] expectedRows = {{"184"}};

        assertSqlQueryRows(
                "Incorrect query result with CASE operator in aggregation function",
                expectedRows,
                sqlQuery);
    }

    /**
     *  #3913
     */
    @Test
    public void testCaseInCastFunction() throws Exception {
        String sqlQuery = String.format(
                "SELECT CAST(100 - CASE WHEN value < 30 THEN 0 ELSE 100 END AS STRING) FROM \"%s\"",
                TEST_METRIC_NAME);

        String[][] expectedRows = {
                {"100"},
                {"100"},
                {"0"}};

        assertSqlQueryRows(
                "Incorrect query result with CASE operator in cast function",
                expectedRows,
                sqlQuery);
    }
}
