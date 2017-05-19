package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;


public class SqlPeriodAlignTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-period-align";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void prepareDataSet() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            setData(Arrays.asList(
                    //millisecond interval
                    new Sample("2000-01-01T00:00:00.001Z", "1"),
                    new Sample("2000-01-01T00:00:00.002Z", "2"),
                    new Sample("2000-01-01T00:00:00.003Z", "3"),

                    //second interval
                    new Sample("2001-01-01T00:00:01.005Z", "1"),
                    new Sample("2001-01-01T00:00:02.005Z", "2"),
                    new Sample("2001-01-01T00:00:03.005Z", "3"),

                    //minute interval
                    new Sample("2002-01-01T00:01:00.005Z", "1"),
                    new Sample("2002-01-01T00:02:00.005Z", "2"),
                    new Sample("2002-01-01T00:03:00.005Z", "3"),

                    //hour interval
                    new Sample("2003-01-01T01:00:00.005Z", "1"),
                    new Sample("2003-01-01T02:00:00.005Z", "2"),
                    new Sample("2003-01-01T03:00:00.005Z", "3"),

                    //day interval
                    new Sample("2004-01-01T00:00:00.005Z", "1"),
                    new Sample("2004-01-02T00:00:00.005Z", "2"),
                    new Sample("2004-01-03T00:00:00.005Z", "3"),

                    new Sample("2004-03-26T00:00:00.005Z", "26"),
                    new Sample("2004-03-27T00:00:00.005Z", "27"),
                    new Sample("2004-03-28T00:00:00.005Z", "28"),
                    new Sample("2004-03-29T00:00:00.005Z", "29"),
                    new Sample("2004-03-30T00:00:00.005Z", "30"),
                    new Sample("2004-03-31T00:00:00.005Z", "31"),

                    //week interval
                    new Sample("2005-01-01T00:00:00.005Z", "1"),
                    new Sample("2005-01-08T00:00:00.005Z", "2"),
                    new Sample("2005-01-15T00:00:00.005Z", "3"),

                    //month interval
                    new Sample("2006-01-01T00:00:00.005Z", "1"),
                    new Sample("2006-02-01T00:00:00.005Z", "2"),
                    new Sample("2006-03-01T00:00:00.005Z", "3"),

                    //quarter interval
                    new Sample("2007-01-01T00:00:00.005Z", "1"),
                    new Sample("2007-04-01T00:00:00.005Z", "2"),
                    new Sample("2007-07-01T00:00:00.005Z", "3"),

                    //year interval
                    new Sample("2008-01-01T00:00:00.005Z", "1"),
                    new Sample("2009-01-01T00:00:00.005Z", "2"),
                    new Sample("2010-01-01T00:00:00.005Z", "3"),

                    new Sample("2016-06-03T09:20:00.124Z", "16.0"),
                    new Sample("2016-06-03T09:26:00.000Z", "8.1"),
                    new Sample("2016-06-03T09:36:00.000Z", "6.0"),
                    new Sample("2016-06-03T09:41:00.321Z", "19.0"),
                    new Sample("2016-06-03T09:45:00.126Z", "19.0"),
                    new Sample("2016-06-03T09:45:00.400Z", "17.0")
            ));
        }};
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /*
      #2906
     */
    @Test
    public void testStartTimeInclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM '%s'  %nWHERE datetime >= '2016-06-03T09:20:00.123Z' " +
                        "AND datetime < '2016-06-03T09:45:00.000Z' %n GROUP BY PERIOD(5 minute, NONE, START_TIME)",
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


    /**
     *  2906
     */
    @Test
    public void testStartTimeExclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM '%s'  %nWHERE datetime > '2016-06-03T09:20:00.123Z' " +
                        "AND datetime < '2016-06-03T09:45:00.000Z' %nGROUP BY PERIOD(5 minute, NONE, START_TIME)",
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


    /**
     *  2906
     */
    @Test
    public void testEndTimeInclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM '%s'  %nWHERE datetime >= '2016-06-03T09:20:00.000Z' " +
                        "AND datetime <= '2016-06-03T09:45:00.321Z' %nGROUP BY PERIOD(5 minute, NONE, END_TIME)",
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


    /**
     *  2906
     */
    @Test
    public void testEndTimeExclusiveAlignment() {
        final String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM '%s'  %nWHERE datetime >= '2016-06-03T09:20:00.123Z' AND " +
                        "datetime <= '2016-06-03T09:45:00.323Z' %nGROUP BY PERIOD(5 minute, NONE, END_TIME)",
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


    @DataProvider(name = "periodsQueryParametersProvider")
    public Object[][] providePeriodsQueryParameters() {
        return new Object[][]{
                //<editor-fold desc="MILLISECOND">
                {
                    "2000-01-01T00:00:00.001Z",
                    "2000-01-01T00:00:00.004Z",
                    "MILLISECOND",
                    "START_TIME",
                    new String[][] {
                            {"2000-01-01T00:00:00.001Z", "1"},
                            {"2000-01-01T00:00:00.002Z", "2"},
                            {"2000-01-01T00:00:00.003Z", "3"}
                    }
                },
                {
                    "2000-01-01T00:00:00.001Z",
                    "2000-01-01T00:00:00.004Z",
                    "MILLISECOND",
                    "END_TIME",
                    new String[][] {
                            {"2000-01-01T00:00:00.001Z", "1"},
                            {"2000-01-01T00:00:00.002Z", "2"},
                            {"2000-01-01T00:00:00.003Z", "3"}
                    }
                },
                {
                    "2000-01-01T00:00:00.001Z",
                    "2000-01-01T00:00:00.004Z",
                    "MILLISECOND",
                    "FIRST_VALUE_TIME",
                    new String[][] {
                            {"2000-01-01T00:00:00.001Z", "1"},
                            {"2000-01-01T00:00:00.002Z", "2"},
                            {"2000-01-01T00:00:00.003Z", "3"}
                    }
                },
                //</editor-fold>

                //<editor-fold desc="SECOND">
                {
                    "2001-01-01T00:00:01.000Z",
                    "2001-01-01T00:00:04.007Z",
                    "SECOND",
                    "START_TIME",
                    new String[][] {
                            {"2001-01-01T00:00:01.000Z", "1"},
                            {"2001-01-01T00:00:02.000Z", "2"},
                            {"2001-01-01T00:00:03.000Z", "3"}
                    }
                },
                {
                    "2001-01-01T00:00:01.000Z",
                    "2001-01-01T00:00:04.007Z",
                    "SECOND",
                    "END_TIME",
                    new String[][] {
                            {"2001-01-01T00:00:01.007Z", "2"},
                            {"2001-01-01T00:00:02.007Z", "3"}
                    }
                },
                {
                    "2001-01-01T00:00:01.000Z",
                    "2001-01-01T00:00:04.007Z",
                    "SECOND",
                    "FIRST_VALUE_TIME",
                    new String[][] {
                            {"2001-01-01T00:00:01.005Z", "1"},
                            {"2001-01-01T00:00:02.005Z", "2"},
                            {"2001-01-01T00:00:03.005Z", "3"}
                    }
                },
                //</editor-fold>

                //<editor-fold desc="MINUTE">
                {
                        "2002-01-01T00:01:00.000Z",
                        "2002-01-01T00:04:00.007Z",
                        "MINUTE",
                        "START_TIME",
                        new String[][] {
                                {"2002-01-01T00:01:00.000Z", "1"},
                                {"2002-01-01T00:02:00.000Z", "2"},
                                {"2002-01-01T00:03:00.000Z", "3"}
                        }
                },
                {
                        "2002-01-01T00:01:00.000Z",
                        "2002-01-01T00:04:00.007Z",
                        "MINUTE",
                        "END_TIME",
                        new String[][] {
                                {"2002-01-01T00:01:00.007Z", "2"},
                                {"2002-01-01T00:02:00.007Z", "3"}
                        }
                },
                {
                        "2002-01-01T00:01:00.000Z",
                        "2002-01-01T00:04:00.007Z",
                        "MINUTE",
                        "FIRST_VALUE_TIME",
                        new String[][] {
                                {"2002-01-01T00:01:00.005Z", "1"},
                                {"2002-01-01T00:02:00.005Z", "2"},
                                {"2002-01-01T00:03:00.005Z", "3"}
                        }
                },
                //</editor-fold>

                //<editor-fold desc="HOUR">
                {
                        "2003-01-01T01:00:00.000Z",
                        "2003-01-01T04:00:00.007Z",
                        "HOUR",
                        "START_TIME",
                        new String[][] {
                                {"2003-01-01T01:00:00.000Z", "1"},
                                {"2003-01-01T02:00:00.000Z", "2"},
                                {"2003-01-01T03:00:00.000Z", "3"}
                        }
                },
                {
                        "2003-01-01T01:00:00.000Z",
                        "2003-01-01T04:00:00.007Z",
                        "HOUR",
                        "END_TIME",
                        new String[][] {
                                {"2003-01-01T01:00:00.007Z", "2"},
                                {"2003-01-01T02:00:00.007Z", "3"}
                        }
                },
                {
                        "2003-01-01T01:00:00.000Z",
                        "2003-01-01T04:00:00.007Z",
                        "HOUR",
                        "FIRST_VALUE_TIME",
                        new String[][] {
                                {"2003-01-01T01:00:00.005Z", "1"},
                                {"2003-01-01T02:00:00.005Z", "2"},
                                {"2003-01-01T03:00:00.005Z", "3"}
                        }
                },
                //</editor-fold>

                //<editor-fold desc="DAY">
                {
                        "2004-01-01T00:00:00.000Z",
                        "2004-01-04T00:00:00.007Z",
                        "DAY",
                        "START_TIME",
                        new String[][] {
                                {"2004-01-01T00:00:00.000Z", "1"},
                                {"2004-01-02T00:00:00.000Z", "2"},
                                {"2004-01-03T00:00:00.000Z", "3"}
                        }
                },
                {
                        "2004-01-01T00:00:00.000Z",
                        "2004-01-04T00:00:00.007Z",
                        "DAY",
                        "END_TIME",
                        new String[][] {
                                {"2004-01-01T00:00:00.007Z", "2"},
                                {"2004-01-02T00:00:00.007Z", "3"}
                        }
                },
                {
                        "2004-01-01T00:00:00.000Z",
                        "2004-01-04T00:00:00.007Z",
                        "DAY",
                        "FIRST_VALUE_TIME",
                        new String[][] {
                                {"2004-01-01T00:00:00.005Z", "1"},
                                {"2004-01-02T00:00:00.005Z", "2"},
                                {"2004-01-03T00:00:00.005Z", "3"}
                        }
                },
                //</editor-fold>

                //<editor-fold desc="WEEK">
                {
                        "2005-01-01T00:00:00.000Z",
                        "2005-01-15T00:00:00.007Z",
                        "WEEK",
                        "START_TIME",
                        new String[][] {
                                {"2005-01-01T00:00:00.000Z", "1"},
                                {"2005-01-08T00:00:00.000Z", "2"},
                                {"2005-01-15T00:00:00.000Z", "3"}
                        }
                },
                {
                        "2005-01-01T00:00:00.000Z",
                        "2005-01-15T00:00:00.007Z",
                        "WEEK",
                        "END_TIME",
                        new String[][] {
                                {"2005-01-01T00:00:00.007Z", "2"},
                                {"2005-01-08T00:00:00.007Z", "3"}
                        }
                },
                {
                        "2005-01-01T00:00:00.000Z",
                        "2005-01-15T00:00:00.007Z",
                        "WEEK",
                        "FIRST_VALUE_TIME",
                        new String[][] {
                                {"2005-01-01T00:00:00.005Z", "1"},
                                {"2005-01-08T00:00:00.005Z", "2"},
                                {"2005-01-15T00:00:00.005Z", "3"}
                        }
                },
                //</editor-fold>

                //<editor-fold desc="MONTH">
                {
                        "2006-01-01T00:00:00.000Z",
                        "2006-04-01T00:00:00.007Z",
                        "MONTH",
                        "START_TIME",
                        new String[][] {
                                {"2006-01-01T00:00:00.000Z", "1"},
                                {"2006-02-01T00:00:00.000Z", "2"},
                                {"2006-03-01T00:00:00.000Z", "3"}
                        }
                },
                {
                        "2006-01-01T00:00:00.000Z",
                        "2006-04-01T00:00:00.007Z",
                        "MONTH",
                        "END_TIME",
                        new String[][] {
                                {"2006-01-01T00:00:00.007Z", "2"},
                                {"2006-02-01T00:00:00.007Z", "3"}
                        }
                },
                {
                        "2006-01-01T00:00:00.000Z",
                        "2006-04-01T00:00:00.007Z",
                        "MONTH",
                        "FIRST_VALUE_TIME",
                        new String[][] {
                                {"2006-01-01T00:00:00.005Z", "1"},
                                {"2006-02-01T00:00:00.005Z", "2"},
                                {"2006-03-01T00:00:00.005Z", "3"}
                        }
                },
                //</editor-fold>

                //<editor-fold desc="QUARTER">
                {
                        "2007-01-01T00:00:00.000Z",
                        "2007-07-01T00:00:00.007Z",
                        "QUARTER",
                        "START_TIME",
                        new String[][] {
                                {"2007-01-01T00:00:00.000Z", "1"},
                                {"2007-04-01T00:00:00.000Z", "2"},
                                {"2007-07-01T00:00:00.000Z", "3"}
                        }
                },
                {
                        "2007-01-01T00:00:00.000Z",
                        "2007-07-01T00:00:00.007Z",
                        "QUARTER",
                        "END_TIME",
                        new String[][] {
                                {"2007-01-01T00:00:00.007Z", "2"},
                                {"2007-04-01T00:00:00.007Z", "3"}
                        }
                },
                {
                        "2007-01-01T00:00:00.000Z",
                        "2007-07-01T00:00:00.007Z",
                        "QUARTER",
                        "FIRST_VALUE_TIME",
                        new String[][] {
                                {"2007-01-01T00:00:00.005Z", "1"},
                                {"2007-04-01T00:00:00.005Z", "2"},
                                {"2007-07-01T00:00:00.005Z", "3"}
                        }
                },
                //</editor-fold>

                //<editor-fold desc="YEAR">
                {
                        "2008-01-01T00:00:00.000Z",
                        "2010-01-01T00:00:00.007Z",
                        "YEAR",
                        "START_TIME",
                        new String[][] {
                                {"2008-01-01T00:00:00.000Z", "1"},
                                {"2009-01-01T00:00:00.000Z", "2"},
                                {"2010-01-01T00:00:00.000Z", "3"}
                        }
                },
                {
                        "2008-01-01T00:00:00.000Z",
                        "2010-01-01T00:00:00.007Z",
                        "YEAR",
                        "END_TIME",
                        new String[][] {
                                {"2008-01-01T00:00:00.007Z", "2"},
                                {"2009-01-01T00:00:00.007Z", "3"}
                        }
                },
                {
                        "2008-01-01T00:00:00.000Z",
                        "2010-01-01T00:00:00.007Z",
                        "YEAR",
                        "FIRST_VALUE_TIME",
                        new String[][] {
                                {"2008-01-01T00:00:00.005Z", "1"},
                                {"2009-01-01T00:00:00.005Z", "2"},
                                {"2010-01-01T00:00:00.005Z", "3"}
                        }
                },
                //</editor-fold>
        };
    }


    /**
     * #4175
     */
    @Test(dataProvider = "periodsQueryParametersProvider")
    public void testPeriodsTimeGrouping(
            String beginTime,
            String endTime,
            String period,
            String align,
            String[][] expectedRows) {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                "FROM '%s' " +
                "WHERE datetime >= '%s' AND datetime < '%s' " +
                "GROUP BY PERIOD(1 %s, %s)",
                TEST_METRIC_NAME,
                beginTime,
                endTime,
                period,
                align
        );

        assertSqlQueryRows(String.format("Incorrect grouping by %s and align by %s", period, align), expectedRows, sqlQuery);
    }

    /**
     * #4175
     */
    @Test
    public void testPeriodsTimeGroupingDSTChangedStartTime() {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM '%s' " +
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

    /**
     * #4175
     */
    @Test
    public void testPeriodsTimeGroupingDSTChangedEndTime() {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM '%s' " +
                        "WHERE datetime >= '2004-03-26T00:00:00Z' AND datetime < '2004-03-31T00:00:00Z' " +
                        "GROUP BY PERIOD(1 DAY, END_TIME, 'Europe/Moscow')",
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

    /**
     * #4175
     */
    @Test
    public void testPeriodsTimeGroupingDSTChangedFirstValueTime() {
        String sqlQuery = String.format(
                "SELECT datetime, MAX(value) " +
                        "FROM '%s' " +
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
}
