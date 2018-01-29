package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.series.SeriesMethod.insertSeriesCheck;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlPeriodMonthTest extends SqlTest {
    private final String TEST_METRIC = metric();

    @BeforeTest
    public void prepareData() throws Exception {
        Series series = new Series(entity(), TEST_METRIC);
        series.addSamples(
                Sample.ofDateInteger("2017-01-31T00:00:05Z", 1),
                Sample.ofDateInteger("2017-02-27T00:00:05Z", 2),
                Sample.ofDateInteger("2017-03-31T00:00:05Z", 3),
                Sample.ofDateInteger("2017-04-30T00:00:05Z", 4),
                Sample.ofDateInteger("2017-05-31T00:00:05Z", 5)
        );

        insertSeriesCheck(series);
    }

    @Issue("4866")
    @Test(description = "test GROUP BY MONTH with UTC timezone")
    public void testGroupByMonthCalendar() {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2017-01-31T00:00:00Z' AND datetime < '2017-05-31T23:00:00Z' " +
                        "GROUP BY PERIOD(1 MONTH, 'UTC')",
                TEST_METRIC
        );

        String[][] expected = new String[][] {
                {"2017-01-01T00:00:00.000Z", "1"},
                {"2017-02-01T00:00:00.000Z", "2"},
                {"2017-03-01T00:00:00.000Z", "3"},
                {"2017-04-01T00:00:00.000Z", "4"},
                {"2017-05-01T00:00:00.000Z", "5"}
        };

        assertSqlQueryRows("Wrong result GROUP BY MONTH with UTC timezone",
                expected, sqlQuery);
    }

    @Issue("4866")
    @Test(description = "test GROUP BY MONTH with START_TIME")
    public void testGroupByMonthStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2017-01-31T00:00:00Z' AND datetime < '2017-05-31T23:00:00Z' " +
                        "GROUP BY PERIOD(1 MONTH, START_TIME, 'UTC')",
                TEST_METRIC
        );

        String[][] expected = new String[][] {
                {"2017-01-31T00:00:00.000Z", "2"},
                {"2017-03-31T00:00:00.000Z", "3"},
                {"2017-04-30T00:00:00.000Z", "4"},
                {"2017-05-31T00:00:00.000Z", "5"}
        };

        assertSqlQueryRows("Wrong result GROUP BY MONTH with START_TIME",
                expected, sqlQuery);
    }

    @Issue("4866")
    @Test(description = "test GROUP BY MONTH with END_TIME")
    public void testGroupByMonthEndTime() {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2017-01-31T00:00:00Z' AND datetime < '2017-05-31T23:00:00Z' " +
                        "GROUP BY PERIOD(1 MONTH, END_TIME, 'UTC')",
                TEST_METRIC
        );

        String[][] expected = new String[][] {
                {"2017-01-31T23:00:00.000Z", "2"},
                {"2017-02-28T23:00:00.000Z", "3"},
                {"2017-03-31T23:00:00.000Z", "4"},
                {"2017-04-30T23:00:00.000Z", "5"}
        };

        assertSqlQueryRows("Wrong result GROUP BY MONTH with END_TIME",
                expected, sqlQuery);
    }

    @Issue("4866")
    @Test(description = "test GROUP BY MONTH with FIRST_VALUE_TIME")
    public void testGroupByMonthFirstValueTime() {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2017-01-31T00:00:00Z' AND datetime < '2017-05-31T23:00:00Z' " +
                        "GROUP BY PERIOD(1 MONTH, FIRST_VALUE_TIME, 'UTC')",
                TEST_METRIC
        );

        String[][] expected = new String[][] {
                {"2017-01-31T00:00:05.000Z", "2"},
                {"2017-03-31T00:00:05.000Z", "3"},
                {"2017-04-30T00:00:05.000Z", "4"},
                {"2017-05-31T00:00:05.000Z", "5"},
        };

        assertSqlQueryRows("Wrong result GROUP BY MONTH with FIRST_VALUE_TIME",
                expected, sqlQuery);
    }
}
