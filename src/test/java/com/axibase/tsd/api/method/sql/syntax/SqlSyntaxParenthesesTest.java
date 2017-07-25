package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlSyntaxParenthesesTest extends SqlTest {
    private final String TEST_METRIC = metric();

    @BeforeTest
    public void prepareData() throws Exception {
        String entity = entity();

        Series series = new Series(entity, TEST_METRIC);
        series.addSamples(
                new Sample("2017-01-01T00:00:00Z", 0, "zero"),
                new Sample("2017-01-01T00:00:01Z", 1),
                new Sample("2017-01-01T00:00:02Z", 2),
                new Sample("2017-01-01T00:00:03Z", 3)
        );
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4195
     */
    @Test
    public void testEqualsInParentheses() {
        String sqlQuery = String.format(
                "SELECT value " +
                "FROM '%s' " +
                "WHERE (value = 1) OR (value = 2)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"1"},
                {"2"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4195
     */
    @Test
    public void testLessGreatInParentheses() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE (value < 1) OR (value >= 2)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"0"},
                {"2"},
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4195
     */
    @Test
    public void testLogicalOperationsOrderInParentheses() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE ((value < 1) OR (value > 2)) AND (value != 0)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4195
     */
    @Test
    public void testNotNullInParentheses() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE (text IS NOT NULL) OR (value = 2)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"0"},
                {"2"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4195
     */
    @Test
    public void testRegexInParentheses() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE (text REGEX 'ze..') AND (value = 0)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"0"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
