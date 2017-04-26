package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames;

public class PeriodDaylightSavingTimeTest extends SqlTest {
    private static String METRIC_NAME = TestNames.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TestNames.entity(), METRIC_NAME);

        long beforeStartDst = 1490396400000L; // 24 Mar 2017 23:00 in UTC
        long beforeEndDst = 1509141600000L;   // 27 Oct 2017 22:00 in UTC
        long hour = 3600000;
        for (int i = 0; i < 24 * 3; i++) {
            series.addData(new Sample(Util.ISOFormat(beforeStartDst + hour * i), i));
            series.addData(new Sample(Util.ISOFormat(beforeEndDst + hour * i), i));
        }

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4131
     */
    @Test
    public void testPeriodDaylightSavingTimeOneDay() {
        String sqlQuery = String.format(
                "SELECT count(*), " +
                        "date_format(time, 'yyyy-MM-dd HH:mm', 'Europe/Vienna'), " +
                        "date_format(time, 'yyyy-MM-dd HH:mm') " +
                        "FROM '%s' " +
                        "GROUP BY PERIOD(1 DAY, 'Europe/Vienna')",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"24", "2017-03-25 00:00", "2017-03-24 23:00"},
                {"23", "2017-03-26 00:00", "2017-03-25 23:00"},
                {"24", "2017-03-27 00:00", "2017-03-26 22:00"},
                { "1", "2017-03-28 00:00", "2017-03-27 22:00"},

                {"24", "2017-10-28 00:00", "2017-10-27 22:00"},
                {"25", "2017-10-29 00:00", "2017-10-28 22:00"},
                {"23", "2017-10-30 00:00", "2017-10-29 23:00"},
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }
}
