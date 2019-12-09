package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class FullInterpolationTest extends SqlTest {
    private static final String TEST_ENTITY = Mocks.entity();
    private static final String TEST_METRIC_1 = Mocks.metric();
    private static final String TEST_METRIC_2 = Mocks.metric();
    private static final String FULL_DETAILS_ASSERT_MSG =
            "DETAILS interpolation must cause error in FULL boundary mode";
    private static final String FULL_DETAILS_ERROR_MSG =
            "FULL boundary mode is not suitable for DETAILS interpolation at line 1 position 325 near \"INTERPOLATE\"";

    @BeforeTest
    public void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY, TEST_METRIC_1).addSamples(
                Sample.ofDateDecimal("2017-01-01T12:00:00Z", new BigDecimal("1.5")),
                Sample.ofDateDecimal("2017-01-03T12:00:00Z", new BigDecimal("3.5")),
                Sample.ofDateDecimal("2017-01-05T12:00:00Z", new BigDecimal("5.5"))
        );

        Series series2 = new Series(TEST_ENTITY, TEST_METRIC_2).addSamples(
                Sample.ofDateDecimal("2017-01-02T12:00:00Z", new BigDecimal("2.5")),
                Sample.ofDateDecimal("2017-01-04T12:00:00Z", new BigDecimal("4.5")),
                Sample.ofDateDecimal("2017-01-06T12:00:00Z", new BigDecimal("6.5"))
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation")
    public void testFullInterpolationLinear() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T00:00:00.000Z", "2"},
                {"2017-01-03T00:00:00.000Z", "3"},
                {"2017-01-04T00:00:00.000Z", "4"},
                {"2017-01-05T00:00:00.000Z", "5"}
        };

        assertSqlQueryRows("Incorrect full linear interpolation", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation")
    public void testFullInterpolationPrevious() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T00:00:00.000Z", "1.5"},
                {"2017-01-03T00:00:00.000Z", "1.5"},
                {"2017-01-04T00:00:00.000Z", "3.5"},
                {"2017-01-05T00:00:00.000Z", "3.5"},
                {"2017-01-06T00:00:00.000Z", "5.5"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outside period")
    public void testFullInterpolationLinearNoValues() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T00:00:00.000Z", "4"},
                {"2017-01-05T00:00:00.000Z", "5"},
        };

        assertSqlQueryRows("Incorrect full linear interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation outside period")
    public void testFullInterpolationPreviousNoValues() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T00:00:00.000Z", "3.5"},
                {"2017-01-05T00:00:00.000Z", "3.5"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outside period")
    public void testFullInterpolationLinearSingleValue() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "3"},
                {"2017-01-04T00:00:00.000Z", "4"},
        };

        assertSqlQueryRows("Incorrect full linear interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation outside period")
    public void testFullInterpolationPreviousSingleValue() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "1.5"},
                {"2017-01-04T00:00:00.000Z", "3.5"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation with fill=true")
    public void testFullInterpolationLinearExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, TRUE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "1.5"},
                {"2017-01-02T00:00:00.000Z", "2"},
                {"2017-01-03T00:00:00.000Z", "3"},
                {"2017-01-04T00:00:00.000Z", "4"},
                {"2017-01-05T00:00:00.000Z", "5"},
                {"2017-01-06T00:00:00.000Z", "5.5"},
                {"2017-01-07T00:00:00.000Z", "5.5"},
        };

        assertSqlQueryRows("Incorrect full linear interpolation with fill=true", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation with fill=true")
    public void testFullInterpolationPreviousExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, TRUE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "1.5"},
                {"2017-01-02T00:00:00.000Z", "1.5"},
                {"2017-01-03T00:00:00.000Z", "1.5"},
                {"2017-01-04T00:00:00.000Z", "3.5"},
                {"2017-01-05T00:00:00.000Z", "3.5"},
                {"2017-01-06T00:00:00.000Z", "5.5"},
                {"2017-01-07T00:00:00.000Z", "5.5"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation with fill=true", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation with fill=true")
    public void testFullInterpolationLinearNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "NaN"},
                {"2017-01-02T00:00:00.000Z", "2"},
                {"2017-01-03T00:00:00.000Z", "3"},
                {"2017-01-04T00:00:00.000Z", "4"},
                {"2017-01-05T00:00:00.000Z", "5"},
                {"2017-01-06T00:00:00.000Z", "NaN"},
                {"2017-01-07T00:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect full linear interpolation with fill=true", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation with fill=true")
    public void testFullInterpolationPreviousNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "NaN"},
                {"2017-01-02T00:00:00.000Z", "1.5"},
                {"2017-01-03T00:00:00.000Z", "1.5"},
                {"2017-01-04T00:00:00.000Z", "3.5"},
                {"2017-01-05T00:00:00.000Z", "3.5"},
                {"2017-01-06T00:00:00.000Z", "5.5"},
                {"2017-01-07T00:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation with fill=true", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation with START_TIME align")
    public void testFullInterpolationLinearStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T06:00:00.000Z", "2.25"},
                {"2017-01-03T06:00:00.000Z", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25"}
        };

        assertSqlQueryRows("Incorrect full linear interpolation with START_TIME align", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation with START_TIME align")
    public void testFullInterpolationPreviousStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T06:00:00.000Z", "1.5"},
                {"2017-01-03T06:00:00.000Z", "1.5"},
                {"2017-01-04T06:00:00.000Z", "3.5"},
                {"2017-01-05T06:00:00.000Z", "3.5"},
                {"2017-01-06T06:00:00.000Z", "5.5"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation with START_TIME align", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation with START_TIME align and fill=true")
    public void testFullInterpolationLinearStartTimeExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, TRUE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T06:00:00.000Z", "1.5"},
                {"2017-01-02T06:00:00.000Z", "2.25"},
                {"2017-01-03T06:00:00.000Z", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25"},
                {"2017-01-06T06:00:00.000Z", "5.5"},
                {"2017-01-07T06:00:00.000Z", "5.5"},
        };

        assertSqlQueryRows(
                "Incorrect full linear interpolation with START_TIME align and fill=true",
                expectedRows,
                sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation with START_TIME align and fill=true")
    public void testFullInterpolationPreviousStartTimeExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, TRUE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T06:00:00.000Z", "1.5"},
                {"2017-01-02T06:00:00.000Z", "1.5"},
                {"2017-01-03T06:00:00.000Z", "1.5"},
                {"2017-01-04T06:00:00.000Z", "3.5"},
                {"2017-01-05T06:00:00.000Z", "3.5"},
                {"2017-01-06T06:00:00.000Z", "5.5"},
                {"2017-01-07T06:00:00.000Z", "5.5"},
        };

        assertSqlQueryRows(
                "Incorrect full previous interpolation with START_TIME align and fill=true",
                expectedRows,
                sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outside period with START_TIME align")
    public void testFullInterpolationLinearNoValuesStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-04T06:00:00Z' AND '2017-01-05T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T06:00:00.000Z", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25"},
        };

        assertSqlQueryRows("Incorrect full linear interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation outside period with START_TIME align")
    public void testFullInterpolationPreviousNoValuesStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-04T06:00:00Z' AND '2017-01-05T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T06:00:00.000Z", "3.5"},
                {"2017-01-05T06:00:00.000Z", "3.5"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outside period with START_TIME align")
    public void testFullInterpolationLinearSingleValueStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-03T06:00:00Z' AND '2017-01-04T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25"},
        };

        assertSqlQueryRows("Incorrect full linear interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation outside period with START_TIME align")
    public void testFullInterpolationPreviousSingleValueStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-03T06:00:00Z' AND '2017-01-04T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "1.5"},
                {"2017-01-04T06:00:00.000Z", "3.5"},
        };

        assertSqlQueryRows("Incorrect full previous interpolation outside period", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation")
    public void testFullInterpolationLinearTimezone() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'Europe/Moscow') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T21:00:00.000Z", "1.875"},
                {"2017-01-02T21:00:00.000Z", "2.875"},
                {"2017-01-03T21:00:00.000Z", "3.875"},
                {"2017-01-04T21:00:00.000Z", "4.875"}
        };

        assertSqlQueryRows("Incorrect full linear interpolation", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full previous interpolation")
    public void testFullInterpolationPreviousTimezone() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'Europe/Moscow') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T21:00:00.000Z", "1.5"},
                {"2017-01-02T21:00:00.000Z", "1.5"},
                {"2017-01-03T21:00:00.000Z", "3.5"},
                {"2017-01-04T21:00:00.000Z", "3.5"},
                {"2017-01-05T21:00:00.000Z", "5.5"}
        };

        assertSqlQueryRows("Incorrect full previous interpolation", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinear() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinear() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T00:00:00.000Z", "2", "null"},
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
                {"2017-01-06T00:00:00.000Z", "null", "6"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinDetailLinear() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, LINEAR, FULL, FALSE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPrevious() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "4.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPrevious() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T00:00:00.000Z", "1.5", "null"},
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T00:00:00.000Z", "null", "6.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousDetail() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, PREVIOUS, FULL, FALSE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearNoValueMain() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearNoValueMain() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
        };
        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearDetailNoValueMain() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, LINEAR, FULL, FALSE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousNoValueMain() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousNoValueMain() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousDetailNoValueMain() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, PREVIOUS, FULL, FALSE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearNoValueJoined() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearNoValueJoined() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
        };
        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearDetailNoValueJoined() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, LINEAR, FULL, FALSE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousNoValueJoined() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousNoValueJoined() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousDetailNoValueJoined() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, PREVIOUS, FULL, FALSE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, TRUE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T00:00:00.000Z", "2", "2.5"},
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "6"},
                {"2017-01-07T00:00:00.000Z", "5.5", "6.5"},
                {"2017-01-08T00:00:00.000Z", "5.5", "6.5"}
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, TRUE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T00:00:00.000Z", "2", "2.5"},
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "6"},
                {"2017-01-07T00:00:00.000Z", "5.5", "6.5"},
                {"2017-01-08T00:00:00.000Z", "5.5", "6.5"}
        };


        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearDetailExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, LINEAR, FULL, TRUE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ERROR_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, TRUE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T00:00:00.000Z", "5.5", "6.5"},
                {"2017-01-08T00:00:00.000Z", "5.5", "6.5"}
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, TRUE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T00:00:00.000Z", "5.5", "6.5"},
                {"2017-01-08T00:00:00.000Z", "5.5", "6.5"}
        };


        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousDetailExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, PREVIOUS, FULL, TRUE) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "NaN", "NaN"},
                {"2017-01-02T00:00:00.000Z", "2", "NaN"},
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
                {"2017-01-06T00:00:00.000Z", "NaN", "6"},
                {"2017-01-07T00:00:00.000Z", "NaN", "NaN"},
                {"2017-01-08T00:00:00.000Z", "NaN", "NaN"}
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "NaN", "NaN"},
                {"2017-01-02T00:00:00.000Z", "2", "NaN"},
                {"2017-01-03T00:00:00.000Z", "3", "3"},
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
                {"2017-01-06T00:00:00.000Z", "NaN", "6"},
                {"2017-01-07T00:00:00.000Z", "NaN", "NaN"},
                {"2017-01-08T00:00:00.000Z", "NaN", "NaN"}
        };


        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearDetailNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, LINEAR, FULL, VALUE NaN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "NaN", "NaN"},
                {"2017-01-02T00:00:00.000Z", "1.5", "NaN"},
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T00:00:00.000Z", "NaN", "6.5"},
                {"2017-01-08T00:00:00.000Z", "NaN", "NaN"}
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T00:00:00.000Z", "NaN", "NaN"},
                {"2017-01-02T00:00:00.000Z", "1.5", "NaN"},
                {"2017-01-03T00:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T00:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T00:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T00:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T00:00:00.000Z", "NaN", "6.5"},
                {"2017-01-08T00:00:00.000Z", "NaN", "NaN"}
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousDetailNaN() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-08T00:00:00Z' " +
                        "WITH INTERPOLATE(DETAIL, PREVIOUS, FULL, VALUE NaN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        assertBadRequest(FULL_DETAILS_ASSERT_MSG, FULL_DETAILS_ERROR_MSG, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "3.25", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25", "5.25"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T06:00:00.000Z", "2.25", "null"},
                {"2017-01-03T06:00:00.000Z", "3.25", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25", "5.25"},
                {"2017-01-06T06:00:00.000Z", "null", "6.25"}
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T06:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T06:00:00.000Z", "5.5", "4.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T06:00:00.000Z", "1.5", "null"},
                {"2017-01-03T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T06:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T06:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T06:00:00.000Z", "null", "6.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearStartTimeExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, TRUE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T06:00:00.000Z", "2.25", "2.5"},
                {"2017-01-03T06:00:00.000Z", "3.25", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25", "5.25"},
                {"2017-01-06T06:00:00.000Z", "5.5", "6.25"},
                {"2017-01-07T06:00:00.000Z", "5.5", "6.5"}
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearStartTimeExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, TRUE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T06:00:00.000Z", "2.25", "2.5"},
                {"2017-01-03T06:00:00.000Z", "3.25", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25", "5.25"},
                {"2017-01-06T06:00:00.000Z", "5.5", "6.25"},
                {"2017-01-07T06:00:00.000Z", "5.5", "6.5"}
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousStartTimeExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, TRUE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-03T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T06:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T06:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T06:00:00.000Z", "5.5", "6.5"}
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousStartTimeExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T06:00:00Z' AND '2017-01-07T10:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, TRUE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-02T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-03T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T06:00:00.000Z", "3.5", "4.5"},
                {"2017-01-06T06:00:00.000Z", "5.5", "4.5"},
                {"2017-01-07T06:00:00.000Z", "5.5", "6.5"}
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearNoValueMainStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T00:00:00.000Z", "4", "4"},
                {"2017-01-05T00:00:00.000Z", "5", "5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearNoValueMainStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T06:00:00Z' AND '2017-01-05T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T06:00:00.000Z", "4.25", "4.25"},
                {"2017-01-05T06:00:00.000Z", "5.25", "5.25"},
        };
        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousNoValueMainStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T06:00:00Z' AND '2017-01-05T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T06:00:00.000Z", "3.5", "4.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousNoValueMainStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-04T06:00:00Z' AND '2017-01-05T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
                {"2017-01-05T06:00:00.000Z", "3.5", "4.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearNoValueJoinedStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T06:00:00Z' AND '2017-01-04T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "3.25", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25", "4.25"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearNoValueJoinedStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T06:00:00Z' AND '2017-01-04T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "3.25", "3.25"},
                {"2017-01-04T06:00:00.000Z", "4.25", "4.25"},
        };
        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousNoValueJoinedStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T06:00:00Z' AND '2017-01-04T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousNoValueJoinedStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-03T06:00:00Z' AND '2017-01-04T06:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, START_TIME, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-03T06:00:00.000Z", "1.5", "2.5"},
                {"2017-01-04T06:00:00.000Z", "3.5", "2.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation join")
    public void testFullInterpolationJoinLinearTimezone() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'Europe/Moscow') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T21:00:00.000Z", "2.875", "2.875"},
                {"2017-01-03T21:00:00.000Z", "3.875", "3.875"},
                {"2017-01-04T21:00:00.000Z", "4.875", "4.875"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinLinearTimezone() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, FULL, FALSE, CALENDAR, 'Europe/Moscow') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T21:00:00.000Z", "1.875", "null"},
                {"2017-01-02T21:00:00.000Z", "2.875", "2.875"},
                {"2017-01-03T21:00:00.000Z", "3.875", "3.875"},
                {"2017-01-04T21:00:00.000Z", "4.875", "4.875"},
                {"2017-01-05T21:00:00.000Z", "null", "5.875"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full interpolation join")
    public void testFullInterpolationJoinPreviousTimezone() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'Europe/Moscow') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-02T21:00:00.000Z", "1.5", "2.5"},
                {"2017-01-03T21:00:00.000Z", "3.5", "2.5"},
                {"2017-01-04T21:00:00.000Z", "3.5", "4.5"},
                {"2017-01-05T21:00:00.000Z", "5.5", "4.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation join", expectedRows, sqlQuery);
    }

    @Issue("4814")
    @Test(description = "test full linear interpolation outer join")
    public void testFullInterpolationFullOuterJoinPreviousTimezone() {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "FULL OUTER JOIN \"%s\" t2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, FULL, FALSE, CALENDAR, 'Europe/Moscow') " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2);

        String[][] expectedRows = new String[][]{
                {"2017-01-01T21:00:00.000Z", "1.5", "null"},
                {"2017-01-02T21:00:00.000Z", "1.5", "2.5"},
                {"2017-01-03T21:00:00.000Z", "3.5", "2.5"},
                {"2017-01-04T21:00:00.000Z", "3.5", "4.5"},
                {"2017-01-05T21:00:00.000Z", "5.5", "4.5"},
                {"2017-01-06T21:00:00.000Z", "null", "6.5"},
        };

        assertSqlQueryRows("Incorrect full interpolation outer join", expectedRows, sqlQuery);
    }
}
