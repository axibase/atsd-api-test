package com.axibase.tsd.api.method.sql.function.rows;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class LagLeadTest extends SqlTest {
    private static final String METRIC_NAME1 = metric();
    private static final String METRIC_NAME2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(entity(), METRIC_NAME1);
        series1.addSamples(
                Sample.ofDateIntegerText("2017-01-01T12:00:00.000Z", 1, "a"),
                Sample.ofDateIntegerText("2017-01-01T13:00:00.000Z", 0, "a"),
                Sample.ofDateIntegerText("2017-01-02T12:00:00.000Z", 2, "a"),
                Sample.ofDateIntegerText("2017-01-03T12:00:00.000Z", 4, "a"),
                Sample.ofDateIntegerText("2017-01-04T12:00:00.000Z", 7, "b"),
                Sample.ofDateIntegerText("2017-01-05T12:00:00.000Z", 11, "b"),
                Sample.ofDateIntegerText("2017-01-06T12:00:00.000Z", 16, "b"),
                Sample.ofDateIntegerText("2017-01-07T12:00:00.000Z", 23, "c"),
                Sample.ofDateIntegerText("2017-01-08T12:00:00.000Z", 31, "c"),
                Sample.ofDateIntegerText("2017-01-09T12:00:00.000Z", 40, "c")
        );

        Series series2 = series1.copy();
        series2.setMetric(METRIC_NAME2);

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Issue("4032")
    @Test
    public void testLagInSelectClause() {
        String sqlQuery = String.format(
                "SELECT value, lag(value) FROM \"%s\"",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"1", "null"},
                {"0",    "1"},
                {"2",    "0"},
                {"4",    "2"},
                {"7",    "4"},
                {"11",   "7"},
                {"16",  "11"},
                {"23",  "16"},
                {"31",  "23"},
                {"40",  "31"}
        };

        assertSqlQueryRows("Wrong result for LAG function in SELECT clause", expectedRows, sqlQuery);
    }

    @Issue("4032")
    @Test
    public void testLeadInSelectClause() {
        String sqlQuery = String.format(
                "SELECT value, lead(value) FROM \"%s\"",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"1",     "0"},
                {"0",     "2"},
                {"2",     "4"},
                {"4",     "7"},
                {"7",    "11"},
                {"11",   "16"},
                {"16",   "23"},
                {"23",   "31"},
                {"31",   "40"},
                {"40", "null"}
        };

        assertSqlQueryRows("Wrong result for LEAD function in SELECT clause", expectedRows, sqlQuery);
    }

    @Issue("4032")
    @Test
    public void testLagInSelectClauseWithNull() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN value > 0 THEN value END, " +
                        "lag(CASE WHEN value > 0 THEN value END) FROM \"%s\"",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"1",    "null"},
                {"null",    "1"},
                {"2",    "null"},
                {"4",       "2"},
                {"7",       "4"},
                {"11",      "7"},
                {"16",     "11"},
                {"23",     "16"},
                {"31",     "23"},
                {"40",     "31"}
        };

        assertSqlQueryRows("Wrong result for LAG function in SELECT clause with null", expectedRows, sqlQuery);
    }

    @Issue("4032")
    @Test
    public void testLeadInSelectClauseWithNull() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN value > 0 THEN value END, " +
                        "lead(CASE WHEN value > 0 THEN value END) FROM \"%s\"",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"1",    "null"},
                {"null",    "2"},
                {"2",       "4"},
                {"4",       "7"},
                {"7",      "11"},
                {"11",     "16"},
                {"16",     "23"},
                {"23",     "31"},
                {"31",     "40"},
                {"40",   "null"}
        };

        assertSqlQueryRows("Wrong result for LEAD function in SELECT clause with null", expectedRows, sqlQuery);
    }

    @Issue("4032")
    @Test
    public void testLagInSelectExpression() {
        String sqlQuery = String.format(
                "SELECT isnull(lag(sum(value)) - sum(value), 0) " +
                        "FROM \"%s\" " +
                        "GROUP BY text",
                METRIC_NAME1
        );

        String[][] expectedRows = {{"0"}, {"-27"}, {"-60"}};

        assertSqlQueryRows("Wrong result for LAG function in SELECT expression", expectedRows, sqlQuery);
    }

    @Issue("4032")
    @Test
    public void testLeadInSelectExpression() {
        String sqlQuery = String.format(
                "SELECT isnull(lead(sum(value)) - sum(value), 0) " +
                        "FROM \"%s\" " +
                        "GROUP BY text",
                METRIC_NAME1
        );

        String[][] expectedRows = {{"27"}, {"60"}, {"0"}};

        assertSqlQueryRows("Wrong result for LEAD function in SELECT expression", expectedRows, sqlQuery);
    }

    @Issue("4032")
    @Test
    public void testLagInEmptyResult() {
        String sqlQuery = String.format(
                "SELECT lag(value) " +
                        "FROM \"%s\" " +
                        "WHERE text < 'a'",
                METRIC_NAME1
        );

        String[][] expectedRows = {};

        assertSqlQueryRows("Wrong result for LAG function with empty result set", expectedRows, sqlQuery);
    }

    @Issue("4032")
    @Test
    public void testLeadInEmptyResult() {
        String sqlQuery = String.format(
                "SELECT lead(value) " +
                        "FROM \"%s\" " +
                        "WHERE text < 'a'",
                METRIC_NAME1
        );

        String[][] expectedRows = {};

        assertSqlQueryRows("Wrong result for LEAD function with empty result set", expectedRows, sqlQuery);
    }

    @Issue("4328")
    @Test
    public void testLeadInJoin() {
        String sqlQuery = String.format(
                "SELECT lead(m1.value), lag(m2.value), lead(m1.value) + lag(m2.value) " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.value > 15 AND m2.value > 20 " +
                        "ORDER BY m1.value",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {  "31", "null", "null"},
                {  "40",   "23",   "63"},
                {"null",   "31", "null"}
        };

        assertSqlQueryRows("Wrong result for LAG/LEAD functions in JOIN", expectedRows, sqlQuery);
    }
}
