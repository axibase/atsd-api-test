package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class PeriodDaylightSavingTimeTest extends SqlTest {
    private static final String METRIC_NAME1 = metric();
    private static final String METRIC_NAME2 = metric();
    private static final String METRIC_NAME3 = metric();
    private static final String QUERY_TEMPLATE =
            "SELECT count(*), " +
            "date_format(time, 'yyyy-MM-dd HH:mm', '%1$s'), " +
            "date_format(time, 'yyyy-MM-dd HH:mm', 'UTC') " +
            "FROM \"%2$s\" " +
            "GROUP BY PERIOD(1 DAY, '%1$s')";

    // three days
    private static final int HOURS = 24 * 3;
    private static final long HOUR_DURATION = 60 * 60 * 1000;

    @BeforeClass
    public static void prepareData() throws Exception {

        Series series1 = new Series(entity(), METRIC_NAME1);
        insertDateRange(series1, Util.getMillis("2017-03-24T23:00Z"));
        insertDateRange(series1, Util.getMillis("2017-10-27T22:00Z"));


        Series series2 = new Series(entity(), METRIC_NAME2);
        insertDateRange(series2, Util.getMillis("2017-03-11T08:00Z"));
        insertDateRange(series2, Util.getMillis("2017-11-04T07:00Z"));

        Series series3 = new Series(entity(), METRIC_NAME3);
        insertDateRange(series3, Util.getMillis("2011-03-25T21:00Z"));
        insertDateRange(series3, Util.getMillis("2014-10-24T20:00Z"));
        insertDateRange(series3, Util.getMillis("2017-03-24T21:00Z"));

        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    private static void insertDateRange(Series s, long start) {
        for (int i = 0; i < HOURS; i++) {
            s.addSamples(Sample.ofDateInteger(Util.ISOFormat(start + HOUR_DURATION * i), i));
        }
    }

    /**
     * #4131
     */
    @Test
    public void testPeriodDaylightSavingTimeTzPositive() {
        String sqlQuery = String.format(QUERY_TEMPLATE, "Europe/Vienna", METRIC_NAME1);

        String[][] expectedRows = {
                {"24", "2017-03-25 00:00", "2017-03-24 23:00"},
                {"23", "2017-03-26 00:00", "2017-03-25 23:00"},
                {"24", "2017-03-27 00:00", "2017-03-26 22:00"},
                { "1", "2017-03-28 00:00", "2017-03-27 22:00"},

                {"24", "2017-10-28 00:00", "2017-10-27 22:00"},
                {"25", "2017-10-29 00:00", "2017-10-28 22:00"},
                {"23", "2017-10-30 00:00", "2017-10-29 23:00"},
        };

        assertSqlQueryRows("Wrong result for GROUP BY PERIOD(1 day) in positive offset timezone with DST",
                expectedRows, sqlQuery);
    }

    /**
     * #4131
     */
    @Test
    public void testPeriodDaylightSavingTimeTzNegative() {
        String sqlQuery = String.format(QUERY_TEMPLATE, "America/Los_Angeles", METRIC_NAME2);

        String[][] expectedRows = {
                {"24", "2017-03-11 00:00", "2017-03-11 08:00"},
                {"23", "2017-03-12 00:00", "2017-03-12 08:00"},
                {"24", "2017-03-13 00:00", "2017-03-13 07:00"},
                { "1", "2017-03-14 00:00", "2017-03-14 07:00"},

                {"24", "2017-11-04 00:00", "2017-11-04 07:00"},
                {"25", "2017-11-05 00:00", "2017-11-05 07:00"},
                {"23", "2017-11-06 00:00", "2017-11-06 08:00"},
        };

        assertSqlQueryRows("Wrong result for GROUP BY PERIOD(1 day) in negative offset timezone with DST",
                expectedRows, sqlQuery);
    }

    /**
     * #4131
     */
    @Test
    public void testPeriodDaylightSavingTimeTzChange() {
        String sqlQuery = String.format(QUERY_TEMPLATE, "Europe/Moscow", METRIC_NAME3);

        String[][] expectedRows = {
                {"24", "2011-03-26 00:00", "2011-03-25 21:00"},
                {"23", "2011-03-27 00:00", "2011-03-26 21:00"},
                {"24", "2011-03-28 00:00", "2011-03-27 20:00"},
                { "1", "2011-03-29 00:00", "2011-03-28 20:00"},

                {"24", "2014-10-25 00:00", "2014-10-24 20:00"},
                {"25", "2014-10-26 00:00", "2014-10-25 20:00"},
                {"23", "2014-10-27 00:00", "2014-10-26 21:00"},

                {"24", "2017-03-25 00:00", "2017-03-24 21:00"},
                {"24", "2017-03-26 00:00", "2017-03-25 21:00"},
                {"24", "2017-03-27 00:00", "2017-03-26 21:00"},
        };

        assertSqlQueryRows("Wrong result for GROUP BY PERIOD(1 day) when changing timezone",
                expectedRows, sqlQuery);
    }
}
