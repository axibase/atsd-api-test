package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereDatetimeComplexConditionTest extends SqlTest {
    private final String TEST_ENTITY = entity();
    private final String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY, TEST_METRIC);
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-03T00:00:00Z", 3),
                Sample.ofDateInteger("2017-01-04T00:00:00Z", 4)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("4592")
    @Test
    public void testDatetimeAndCondition() {
        String query = String.format(
                "SELECT datetime, value " +
                "FROM \"%s\" " +
                "WHERE datetime < '2017-01-03T00:00:00Z' AND value = 2",
                TEST_METRIC);

        String[][] expectedResult = {
                {"2017-01-02T00:00:00.000Z", "2"}
        };

        assertSqlQueryRows(expectedResult, query);
    }

    @Issue("4592")
    @Test
    public void testDatetimeBetweenAndCondition() {
        String query = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-03T00:00:00Z' AND value = 2",
                TEST_METRIC);

        String[][] expectedResult = {
                {"2017-01-02T00:00:00.000Z", "2"}
        };

        assertSqlQueryRows(expectedResult, query);
    }

    @Issue("4592")
    @Test
    public void testDatetimeOrCondition() {
        String query = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime < '2017-01-03T00:00:00Z' OR value = 3",
                TEST_METRIC);

        String[][] expectedResult = {
                {"2017-01-01T00:00:00.000Z", "1"},
                {"2017-01-02T00:00:00.000Z", "2"},
                {"2017-01-03T00:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedResult, query);
    }

    @Issue("4592")
    @Test
    public void testDatetimeBetweenOrCondition() {
        String query = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' OR value = 2",
                TEST_METRIC);

        String[][] expectedResult = {
                {"2017-01-02T00:00:00.000Z", "2"},
                {"2017-01-03T00:00:00.000Z", "3"},
                {"2017-01-04T00:00:00.000Z", "4"}
        };

        assertSqlQueryRows(expectedResult, query);
    }

    @Issue("4592")
    @Test
    public void testDatetimeComplexCondition() {
        String query = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE (NOT datetime <= '2017-01-03T00:00:00Z' OR value = 2) AND" +
                        " (datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-03T00:00:00Z')",
                TEST_METRIC);

        String[][] expectedResult = {
                {"2017-01-02T00:00:00.000Z", "2"}
        };

        assertSqlQueryRows(expectedResult, query);
    }

    @Issue("4592")
    @Test
    public void testDatetimeTwoIntervalsCondition() {
        String query = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime = '2017-01-01T00:00:00Z'" +
                        " OR value = 2" +
                        " OR datetime = '2017-01-03T00:00:00Z'",
                TEST_METRIC);

        String[][] expectedResult = {
                {"2017-01-01T00:00:00.000Z", "1"},
                {"2017-01-02T00:00:00.000Z", "2"},
                {"2017-01-03T00:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedResult, query);
    }
}
