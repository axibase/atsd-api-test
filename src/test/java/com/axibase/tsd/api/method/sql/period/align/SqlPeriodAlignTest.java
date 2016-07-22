package com.axibase.tsd.api.method.sql.period.align;

import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlPeriodAlignTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-period-align";


    @BeforeClass
    public static void prepareDataSet() {
        Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");
        sendSamplesToSeries(testSeries,
                new Sample("2016-06-03T09:20:00.124Z", "16.0"),
                new Sample("2016-06-03T09:26:00.000Z", "8.1"),
                new Sample("2016-06-03T09:36:00.000Z", "6.0"),
                new Sample("2016-06-03T09:41:00.321Z", "19.0"),
                new Sample("2016-06-03T09:45:00.126Z", "19.0"),
                new Sample("2016-06-03T09:45:00.400Z", "17.0")
        );
    }

    /**
     * Following tests related to #2906
     */


    /**
     * redmine: 2906
     */
    @Test
    public void testStartTimeInclusiveAlignment() {
        final String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-align-metric' \n" +
                        "WHERE datetime >= '2016-06-03T09:20:00.123Z' AND datetime < '2016-06-03T09:45:00.000Z'\n" +
                        "GROUP BY PERIOD(5 minute, NONE, START_TIME)";

        final List<List<String>> resultTableRows =
                executeQuery(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by start time inclusive(123 ms)
                Arrays.asList("2016-06-03T09:20:00.123Z", "16.0"),
                Arrays.asList("2016-06-03T09:25:00.123Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.123Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.123Z", "19.0")
        );
        Assert.assertEquals(expectedTableRows, resultTableRows);
    }


    /**
     * redmine: 2906
     */
    @Test
    public void testStartTimeExclusiveAlignment() {
        final String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-align-metric' \n" +
                        "WHERE datetime > '2016-06-03T09:20:00.123Z' AND datetime < '2016-06-03T09:45:00.000Z'\n" +
                        "GROUP BY PERIOD(5 minute, NONE, START_TIME)";

        final List<List<String>> resultTableRows =
                executeQuery(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by start time exclusive(124 ms)
                Arrays.asList("2016-06-03T09:20:00.124Z", "16.0"),
                Arrays.asList("2016-06-03T09:25:00.124Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.124Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.124Z", "19.0")
        );
        Assert.assertEquals(expectedTableRows, resultTableRows);
    }


    /**
     * redmine: 2906
     */
    @Test
    public void testEndTimeInclusiveAlignment() {
        final String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-align-metric' \n" +
                        "WHERE datetime >= '2016-06-03T09:20:00.000Z' AND datetime <= '2016-06-03T09:45:00.321Z'\n" +
                        "GROUP BY PERIOD(5 minute, NONE, END_TIME)";

        final List<List<String>> resultTableRows =
                executeQuery(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by end time inclusive(322 ms)
                Arrays.asList("2016-06-03T09:25:00.322Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.322Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.322Z", "19.0")
        );
        Assert.assertEquals(expectedTableRows, resultTableRows);
    }


    /**
     * redmine: 2906
     */
    @Test
    public void testEndTimeExclusiveAlignment() {
        final String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-align-metric' \n" +
                        "WHERE datetime >= '2016-06-03T09:20:00.123Z' AND datetime <= '2016-06-03T09:45:00.323Z'\n" +
                        "GROUP BY PERIOD(5 minute, NONE, END_TIME)";

        final List<List<String>> resultTableRows =
                executeQuery(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = Arrays.asList(
                // Expect align by start time inclusive(324 ms)
                Arrays.asList("2016-06-03T09:25:00.324Z", "8.1"),
                Arrays.asList("2016-06-03T09:35:00.324Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.324Z", "19.0")
        );
        Assert.assertEquals(expectedTableRows, resultTableRows);
    }
}
