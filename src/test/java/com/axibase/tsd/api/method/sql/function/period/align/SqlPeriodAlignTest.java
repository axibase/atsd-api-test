package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.model.version.Version;
import com.axibase.tsd.api.util.Registry;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.TestUtil.addTimeUnitsInTimezone;
import static org.testng.AssertJUnit.assertEquals;

public class SqlPeriodAlignTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-period-align";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    private ZoneId serverTimezone;

    @BeforeClass
    public void prepareDataSet() throws Exception {
        Registry.Entity.checkExists(TEST_ENTITY_NAME);
        Registry.Metric.checkExists(TEST_METRIC_NAME);

        Version version = VersionMethod.queryVersion().readEntity(Version.class);
        serverTimezone = ZoneId.of(version.getDate().getTimeZone().getName());

        insertSamples(
                Sample.ofDateInteger("2016-06-03T09:20:00.124Z", 16),
                Sample.ofDateDecimal("2016-06-03T09:26:00.000Z", new BigDecimal("8.1")),
                Sample.ofDateInteger("2016-06-03T09:36:00.000Z", 6),
                Sample.ofDateInteger("2016-06-03T09:41:00.321Z", 19),
                Sample.ofDateInteger("2016-06-03T09:45:00.126Z", 19),
                Sample.ofDateInteger("2016-06-03T09:45:00.400Z", 17)
        );
    }

    private void insertSamples(Sample... samples) throws Exception {
        Series series = new Series();
        series.setEntity(TEST_ENTITY_NAME);
        series.setMetric(TEST_METRIC_NAME);
        series.addSamples(samples);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @Issue("2906")
    @Test
    public void testStartTimeInclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\"  %nWHERE datetime >= '2016-06-03T09:20:00.123Z' " +
                        "AND datetime < '2016-06-03T09:45:00.000Z' %n GROUP BY PERIOD(5 minute, NONE, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        final List<List<String>> resultTableRows =
                queryResponse(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by start time inclusive(123 ms)
                Arrays.asList("2016-06-03T09:20:00.123Z", "16.0"),
                Arrays.asList("2016-06-03T09:25:00.123Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.123Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.123Z", "19.0")
        );
        assertEquals(expectedTableRows, resultTableRows);
    }

    @Issue("2906")
    @Test
    public void testStartTimeExclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\"  %nWHERE datetime > '2016-06-03T09:20:00.123Z' " +
                        "AND datetime < '2016-06-03T09:45:00.000Z' %nGROUP BY PERIOD(5 minute, NONE, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        final List<List<String>> resultTableRows =
                queryResponse(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by start time exclusive(124 ms)
                Arrays.asList("2016-06-03T09:20:00.124Z", "16.0"),
                Arrays.asList("2016-06-03T09:25:00.124Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.124Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.124Z", "19.0")
        );
        assertEquals(expectedTableRows, resultTableRows);
    }


    @Issue("2906")
    @Test
    public void testEndTimeInclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\"  %nWHERE datetime >= '2016-06-03T09:20:00.000Z' " +
                        "AND datetime <= '2016-06-03T09:45:00.321Z' %nGROUP BY PERIOD(5 minute, NONE, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        final List<List<String>> resultTableRows =
                queryResponse(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by end time inclusive(322 ms)
                Arrays.asList("2016-06-03T09:25:00.322Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.322Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.322Z", "19.0")
        );
        assertEquals(expectedTableRows, resultTableRows);
    }


    @Issue("2906")
    @Test
    public void testEndTimeExclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\"  %nWHERE datetime >= '2016-06-03T09:20:00.123Z' AND " +
                        "datetime <= '2016-06-03T09:45:00.323Z' %nGROUP BY PERIOD(5 minute, NONE, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        final List<List<String>> resultTableRows =
                queryResponse(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by start time inclusive(324 ms)
                Arrays.asList("2016-06-03T09:25:00.324Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.324Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.324Z", "19.0")
        );
        assertEquals(expectedTableRows, resultTableRows);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMillisecondStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2000-01-01T00:00:00.000Z", 0),
                Sample.ofDateInteger("2000-01-01T00:00:00.001Z", 1),
                Sample.ofDateInteger("2000-01-01T00:00:00.002Z", 2),
                Sample.ofDateInteger("2000-01-01T00:00:00.003Z", 3),
                Sample.ofDateInteger("2000-01-01T00:00:00.004Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2000-01-01T00:00:00.001Z' AND datetime < '2000-01-01T00:00:00.004Z' " +
                        "GROUP BY PERIOD(1 MILLISECOND, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2000-01-01T00:00:00.001Z", "1"},
                {"2000-01-01T00:00:00.002Z", "2"},
                {"2000-01-01T00:00:00.003Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMillisecondEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2000-01-01T00:00:00.000Z", 0),
                Sample.ofDateInteger("2000-01-01T00:00:00.001Z", 1),
                Sample.ofDateInteger("2000-01-01T00:00:00.002Z", 2),
                Sample.ofDateInteger("2000-01-01T00:00:00.003Z", 3),
                Sample.ofDateInteger("2000-01-01T00:00:00.004Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2000-01-01T00:00:00.001Z' AND datetime < '2000-01-01T00:00:00.004Z' " +
                        "GROUP BY PERIOD(1 MILLISECOND, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2000-01-01T00:00:00.001Z", "1"},
                {"2000-01-01T00:00:00.002Z", "2"},
                {"2000-01-01T00:00:00.003Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMillisecondFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2000-01-01T00:00:00.000Z", 0),
                Sample.ofDateInteger("2000-01-01T00:00:00.001Z", 1),
                Sample.ofDateInteger("2000-01-01T00:00:00.002Z", 2),
                Sample.ofDateInteger("2000-01-01T00:00:00.003Z", 3),
                Sample.ofDateInteger("2000-01-01T00:00:00.004Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2000-01-01T00:00:00.001Z' AND datetime < '2000-01-01T00:00:00.004Z' " +
                        "GROUP BY PERIOD(1 MILLISECOND, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2000-01-01T00:00:00.001Z", "1"},
                {"2000-01-01T00:00:00.002Z", "2"},
                {"2000-01-01T00:00:00.003Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodSecondStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2001-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2001-01-01T00:00:01.005Z", 1),
                Sample.ofDateInteger("2001-01-01T00:00:02.005Z", 2),
                Sample.ofDateInteger("2001-01-01T00:00:03.005Z", 3),
                Sample.ofDateInteger("2001-01-01T00:00:04.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2001-01-01T00:00:01.000Z' AND datetime < '2001-01-01T00:00:03.007Z' " +
                        "GROUP BY PERIOD(1 SECOND, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2001-01-01T00:00:01.000Z", "1"},
                {"2001-01-01T00:00:02.000Z", "2"},
                {"2001-01-01T00:00:03.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodSecondEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2001-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2001-01-01T00:00:01.005Z", 1),
                Sample.ofDateInteger("2001-01-01T00:00:02.005Z", 2),
                Sample.ofDateInteger("2001-01-01T00:00:03.005Z", 3),
                Sample.ofDateInteger("2001-01-01T00:00:04.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2001-01-01T00:00:01.000Z' AND datetime < '2001-01-01T00:00:03.007Z' " +
                        "GROUP BY PERIOD(1 SECOND, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2001-01-01T00:00:01.007Z", "2"},
                {"2001-01-01T00:00:02.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodSecondFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2001-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2001-01-01T00:00:01.005Z", 1),
                Sample.ofDateInteger("2001-01-01T00:00:02.005Z", 2),
                Sample.ofDateInteger("2001-01-01T00:00:03.005Z", 3),
                Sample.ofDateInteger("2001-01-01T00:00:04.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2001-01-01T00:00:01.000Z' AND datetime < '2001-01-01T00:00:03.007Z' " +
                        "GROUP BY PERIOD(1 SECOND, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2001-01-01T00:00:01.005Z", "1"},
                {"2001-01-01T00:00:02.005Z", "2"},
                {"2001-01-01T00:00:03.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMinuteStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2002-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2002-01-01T00:01:00.005Z", 1),
                Sample.ofDateInteger("2002-01-01T00:02:00.005Z", 2),
                Sample.ofDateInteger("2002-01-01T00:03:00.005Z", 3),
                Sample.ofDateInteger("2002-01-01T00:04:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2002-01-01T00:01:00.000Z' AND datetime < '2002-01-01T00:03:00.007Z' " +
                        "GROUP BY PERIOD(1 MINUTE, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2002-01-01T00:01:00.000Z", "1"},
                {"2002-01-01T00:02:00.000Z", "2"},
                {"2002-01-01T00:03:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMinuteEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2002-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2002-01-01T00:01:00.005Z", 1),
                Sample.ofDateInteger("2002-01-01T00:02:00.005Z", 2),
                Sample.ofDateInteger("2002-01-01T00:03:00.005Z", 3),
                Sample.ofDateInteger("2002-01-01T00:04:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2002-01-01T00:01:00.000Z' AND datetime < '2002-01-01T00:03:00.007Z' " +
                        "GROUP BY PERIOD(1 MINUTE, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2002-01-01T00:01:00.007Z", "2"},
                {"2002-01-01T00:02:00.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMinuteFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2002-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2002-01-01T00:01:00.005Z", 1),
                Sample.ofDateInteger("2002-01-01T00:02:00.005Z", 2),
                Sample.ofDateInteger("2002-01-01T00:03:00.005Z", 3),
                Sample.ofDateInteger("2002-01-01T00:04:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2002-01-01T00:01:00.000Z' AND datetime < '2002-01-01T00:03:00.007Z' " +
                        "GROUP BY PERIOD(1 MINUTE, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2002-01-01T00:01:00.005Z", "1"},
                {"2002-01-01T00:02:00.005Z", "2"},
                {"2002-01-01T00:03:00.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodHourStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2003-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2003-01-01T01:00:00.005Z", 1),
                Sample.ofDateInteger("2003-01-01T02:00:00.005Z", 2),
                Sample.ofDateInteger("2003-01-01T03:00:00.005Z", 3),
                Sample.ofDateInteger("2003-01-01T04:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2003-01-01T01:00:00.000Z' AND datetime < '2003-01-01T03:00:00.007Z' " +
                        "GROUP BY PERIOD(1 HOUR, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2003-01-01T01:00:00.000Z", "1"},
                {"2003-01-01T02:00:00.000Z", "2"},
                {"2003-01-01T03:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodHourEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2003-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2003-01-01T01:00:00.005Z", 1),
                Sample.ofDateInteger("2003-01-01T02:00:00.005Z", 2),
                Sample.ofDateInteger("2003-01-01T03:00:00.005Z", 3),
                Sample.ofDateInteger("2003-01-01T04:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2003-01-01T01:00:00.000Z' AND datetime < '2003-01-01T03:00:00.007Z' " +
                        "GROUP BY PERIOD(1 HOUR, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2003-01-01T01:00:00.007Z", "2"},
                {"2003-01-01T02:00:00.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodHourFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2003-01-01T00:00:00.005Z", 0),
                Sample.ofDateInteger("2003-01-01T01:00:00.005Z", 1),
                Sample.ofDateInteger("2003-01-01T02:00:00.005Z", 2),
                Sample.ofDateInteger("2003-01-01T03:00:00.005Z", 3),
                Sample.ofDateInteger("2003-01-01T04:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2003-01-01T01:00:00.000Z' AND datetime < '2003-01-01T03:00:00.007Z' " +
                        "GROUP BY PERIOD(1 HOUR, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2003-01-01T01:00:00.005Z", "1"},
                {"2003-01-01T02:00:00.005Z", "2"},
                {"2003-01-01T03:00:00.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodDayStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2003-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2004-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2004-01-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2004-01-03T00:00:00.005Z", 3),
                Sample.ofDateInteger("2004-01-04T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2004-01-01T00:00:00.000Z' AND datetime < '2004-01-03T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 DAY, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2004-01-01T00:00:00.000Z", "1"},
                {"2004-01-02T00:00:00.000Z", "2"},
                {"2004-01-03T00:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodDayEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2003-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2004-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2004-01-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2004-01-03T00:00:00.005Z", 3),
                Sample.ofDateInteger("2004-01-04T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2004-01-01T00:00:00.000Z' AND datetime < '2004-01-03T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 DAY, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2004-01-01T00:00:00.007Z", "2"},
                {"2004-01-02T00:00:00.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodDayFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2003-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2004-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2004-01-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2004-01-03T00:00:00.005Z", 3),
                Sample.ofDateInteger("2004-01-04T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2004-01-01T00:00:00.000Z' AND datetime < '2004-01-03T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 DAY, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2004-01-01T00:00:00.005Z", "1"},
                {"2004-01-02T00:00:00.005Z", "2"},
                {"2004-01-03T00:00:00.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testPeriodsTimeGroupingDSTChangedStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2004-03-25T00:00:00.005Z", 25),
                Sample.ofDateInteger("2004-03-26T00:00:00.005Z", 26),
                Sample.ofDateInteger("2004-03-27T00:00:00.005Z", 27),
                Sample.ofDateInteger("2004-03-28T00:00:00.005Z", 28),
                Sample.ofDateInteger("2004-03-29T00:00:00.005Z", 29),
                Sample.ofDateInteger("2004-03-30T00:00:00.005Z", 30),
                Sample.ofDateInteger("2004-03-31T00:00:00.005Z", 31)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2004-03-26T00:00:00Z' AND datetime < '2004-03-31T00:00:00Z' " +
                        "GROUP BY PERIOD(1 DAY, START_TIME, 'Europe/Moscow')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2004-03-26T00:00:00.000Z", "26"},
                {"2004-03-27T00:00:00.000Z", "27"},
                {"2004-03-27T23:00:00.000Z", "28"},
                {"2004-03-28T23:00:00.000Z", "29"},
                {"2004-03-29T23:00:00.000Z", "30"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Issue("4700")
    @Test
    public void testPeriodsTimeGroupingDSTChangedEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2004-03-25T00:00:00.005Z", 25),
                Sample.ofDateInteger("2004-03-26T00:00:00.005Z", 26),
                Sample.ofDateInteger("2004-03-27T00:00:00.005Z", 27),
                Sample.ofDateInteger("2004-03-28T00:00:00.005Z", 28),
                Sample.ofDateInteger("2004-03-29T00:00:00.005Z", 29),
                Sample.ofDateInteger("2004-03-30T00:00:00.005Z", 30),
                Sample.ofDateInteger("2004-03-31T00:00:00.005Z", 31)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2004-03-26T00:00:00Z' AND datetime < '2004-03-31T00:00:00Z' " +
                        "GROUP BY PERIOD(1 DAY, END_TIME, 'Europe/Moscow')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2004-03-26T01:00:00.000Z", "27"},
                {"2004-03-28T00:00:00.000Z", "28"},
                {"2004-03-29T00:00:00.000Z", "29"},
                {"2004-03-30T00:00:00.000Z", "30"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testPeriodsTimeGroupingDSTChangedFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2004-03-25T00:00:00.005Z", 25),
                Sample.ofDateInteger("2004-03-26T00:00:00.005Z", 26),
                Sample.ofDateInteger("2004-03-27T00:00:00.005Z", 27),
                Sample.ofDateInteger("2004-03-28T00:00:00.005Z", 28),
                Sample.ofDateInteger("2004-03-29T00:00:00.005Z", 29),
                Sample.ofDateInteger("2004-03-30T00:00:00.005Z", 30),
                Sample.ofDateInteger("2004-03-31T00:00:00.005Z", 31)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2004-03-26T00:00:00Z' AND datetime < '2004-03-31T00:00:00Z' " +
                        "GROUP BY PERIOD(1 DAY, FIRST_VALUE_TIME, 'Europe/Moscow')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2004-03-26T00:00:00.005Z", "26"},
                {"2004-03-27T00:00:00.005Z", "27"},
                {"2004-03-27T23:00:00.005Z", "28"},
                {"2004-03-28T23:00:00.005Z", "29"},
                {"2004-03-29T23:00:00.005Z", "30"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodWeekStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2004-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2005-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2005-01-08T00:00:00.005Z", 2),
                Sample.ofDateInteger("2005-01-15T00:00:00.005Z", 3),
                Sample.ofDateInteger("2005-01-16T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2005-01-01T00:00:00.000Z' AND datetime < '2005-01-15T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 WEEK, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2005-01-01T00:00:00.000Z", "1"},
                {"2005-01-08T00:00:00.000Z", "2"},
                {"2005-01-15T00:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodWeekEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2004-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2005-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2005-01-08T00:00:00.005Z", 2),
                Sample.ofDateInteger("2005-01-15T00:00:00.005Z", 3),
                Sample.ofDateInteger("2005-01-16T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2005-01-01T00:00:00.000Z' AND datetime < '2005-01-15T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 WEEK, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2005-01-01T00:00:00.007Z", "2"},
                {"2005-01-08T00:00:00.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodWeekFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2004-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2005-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2005-01-08T00:00:00.005Z", 2),
                Sample.ofDateInteger("2005-01-15T00:00:00.005Z", 3),
                Sample.ofDateInteger("2005-01-16T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2005-01-01T00:00:00.000Z' AND datetime < '2005-01-15T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 WEEK, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2005-01-01T00:00:00.005Z", "1"},
                {"2005-01-08T00:00:00.005Z", "2"},
                {"2005-01-15T00:00:00.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMonthStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-01-02T00:00:00.005Z", 0),
                Sample.ofDateInteger("2006-02-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2006-03-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2006-04-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2006-05-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2006-02-02T00:00:00.000Z' AND datetime < '2006-04-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 MONTH, START_TIME, 'ETC/Utc')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2006-02-02T00:00:00.000Z", "1"},
                {"2006-03-02T00:00:00.000Z", "2"},
                {"2006-04-02T00:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMonthEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-01-02T00:00:00.005Z", 0),
                Sample.ofDateInteger("2006-02-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2006-03-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2006-04-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2006-05-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2006-02-02T00:00:00.000Z' AND datetime < '2006-04-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 MONTH, END_TIME, 'ETC/Utc')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2006-02-02T00:00:00.007Z", "2"},
                {"2006-03-02T00:00:00.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodMonthFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-01-02T00:00:00.005Z", 0),
                Sample.ofDateInteger("2006-02-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2006-03-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2006-04-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2006-05-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2006-02-02T00:00:00.000Z' AND datetime < '2006-04-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 MONTH, FIRST_VALUE_TIME, 'ETC/Utc')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2006-02-02T00:00:00.005Z", "1"},
                {"2006-03-02T00:00:00.005Z", "2"},
                {"2006-04-02T00:00:00.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodQuarterStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-12-30T00:00:00.005Z", 0),
                Sample.ofDateInteger("2007-01-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2007-04-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2007-07-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2007-08-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2007-01-02T00:00:00.000Z' AND datetime < '2007-07-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 QUARTER, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2007-01-02T00:00:00.000Z", "1"},
                {"2007-04-02T00:00:00.000Z", "2"},
                {"2007-07-02T00:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodQuarterEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-12-30T00:00:00.005Z", 0),
                Sample.ofDateInteger("2007-01-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2007-04-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2007-07-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2007-08-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2007-01-02T00:00:00.000Z' AND datetime < '2007-07-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 QUARTER, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2007-01-02T00:00:00.007Z", "2"},
                {"2007-04-02T00:00:00.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodQuarterFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-12-30T00:00:00.005Z", 0),
                Sample.ofDateInteger("2007-01-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2007-04-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2007-07-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2007-08-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2007-01-02T00:00:00.000Z' AND datetime < '2007-07-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 QUARTER, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2007-01-02T00:00:00.005Z", "1"},
                {"2007-04-02T00:00:00.005Z", "2"},
                {"2007-07-02T00:00:00.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodQuarterServerTimezoneStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-12-30T00:00:00.005Z", 0),
                Sample.ofDateInteger("2007-01-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2007-04-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2007-07-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2007-08-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2007-01-02T00:00:00.000Z' AND datetime < '2007-07-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 QUARTER, START_TIME)",
                TEST_METRIC_NAME
        );

        String resultUtcDate1 = "2007-01-02T00:00:00.000Z";
        String resultUtcDate2 = addTimeUnitsInTimezone(resultUtcDate1, serverTimezone, TimeUnit.MONTH, 3);
        String resultUtcDate3 = addTimeUnitsInTimezone(resultUtcDate1, serverTimezone, TimeUnit.MONTH, 6);

        String[][] expectedRows = new String[][]{
                {resultUtcDate1, "1"},
                {resultUtcDate2, "2"},
                {resultUtcDate3, "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodQuarterServerTimezoneEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-12-30T00:00:00.005Z", 0),
                Sample.ofDateInteger("2007-01-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2007-04-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2007-07-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2007-08-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2007-01-02T00:00:00.000Z' AND datetime < '2007-07-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 QUARTER, END_TIME)",
                TEST_METRIC_NAME
        );

        String resultUtcDate1 = "2007-04-02T00:00:00.007Z";
        String resultUtcDate2 = addTimeUnitsInTimezone(resultUtcDate1, serverTimezone, TimeUnit.MONTH, -3);

        String[][] expectedRows = new String[][]{
                {resultUtcDate2, "2"},
                {resultUtcDate1, "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodQuarterServerTimezoneFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2006-12-30T00:00:00.005Z", 0),
                Sample.ofDateInteger("2007-01-02T00:00:00.005Z", 1),
                Sample.ofDateInteger("2007-04-02T00:00:00.005Z", 2),
                Sample.ofDateInteger("2007-07-02T00:00:00.005Z", 3),
                Sample.ofDateInteger("2007-08-02T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2007-01-02T00:00:00.000Z' AND datetime < '2007-07-02T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 QUARTER, FIRST_VALUE_TIME)",
                TEST_METRIC_NAME
        );

        String resultUtcDate1 = "2007-01-02T00:00:00.005Z";
        String resultUtcDate2 = addTimeUnitsInTimezone(resultUtcDate1, serverTimezone, TimeUnit.MONTH, 3);
        String resultUtcDate3 = addTimeUnitsInTimezone(resultUtcDate1, serverTimezone, TimeUnit.MONTH, 6);

        String[][] expectedRows = new String[][]{
                {resultUtcDate1, "1"},
                {resultUtcDate2, "2"},
                {resultUtcDate3, "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodYearStartTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2007-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2008-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2009-01-01T00:00:00.005Z", 2),
                Sample.ofDateInteger("2010-01-01T00:00:00.005Z", 3),
                Sample.ofDateInteger("2011-01-01T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2008-01-01T00:00:00.000Z' AND datetime < '2010-01-01T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 YEAR, START_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2008-01-01T00:00:00.000Z", "1"},
                {"2009-01-01T00:00:00.000Z", "2"},
                {"2010-01-01T00:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodYearEndTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2007-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2008-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2009-01-01T00:00:00.005Z", 2),
                Sample.ofDateInteger("2010-01-01T00:00:00.005Z", 3),
                Sample.ofDateInteger("2011-01-01T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2008-01-01T00:00:00.000Z' AND datetime < '2010-01-01T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 YEAR, END_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2008-01-01T00:00:00.007Z", "2"},
                {"2009-01-01T00:00:00.007Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4175")
    @Test
    public void testGroupByPeriodYearFirstValueTime() throws Exception {
        insertSamples(
                Sample.ofDateInteger("2007-12-31T00:00:00.005Z", 0),
                Sample.ofDateInteger("2008-01-01T00:00:00.005Z", 1),
                Sample.ofDateInteger("2009-01-01T00:00:00.005Z", 2),
                Sample.ofDateInteger("2010-01-01T00:00:00.005Z", 3),
                Sample.ofDateInteger("2011-01-01T00:00:00.005Z", 4)
        );

        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM \"%s\" " +
                        "WHERE datetime >= '2008-01-01T00:00:00.000Z' AND datetime < '2010-01-01T00:00:00.007Z' " +
                        "GROUP BY PERIOD(1 YEAR, FIRST_VALUE_TIME, 'Etc/UTC')",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = new String[][]{
                {"2008-01-01T00:00:00.005Z", "1"},
                {"2009-01-01T00:00:00.005Z", "2"},
                {"2010-01-01T00:00:00.005Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
