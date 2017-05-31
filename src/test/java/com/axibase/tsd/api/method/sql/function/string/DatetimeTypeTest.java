package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DatetimeTypeTest extends SqlTest {
    private static Series series;

    @BeforeClass
    public static void prepareData() throws Exception {
        series = Mocks.series();

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4221
     */
    @Test
    public void testDatetimeTypeInStringFunctions() {
        String sqlQuery = String.format(
                "SELECT upper(datetime), lower(datetime), replace(datetime, \"3\", \"0\"), length(datetime), " +
                        "concat(datetime, \"word\"), concat(datetime, 123), locate(16, datetime), " +
                        "locate(\"16\", datetime), substr(datetime, 24) " +
                "FROM '%s' t1",
                series.getMetric()
        );

        String[][] expectedRows = {
                {
                        "2016-06-03T09:23:00.000Z",
                        "2016-06-03t09:23:00.000z",
                        "2016-06-00T09:20:00.000Z",
                        "24",
                        "2016-06-03T09:23:00.000Zword",
                        "2016-06-03T09:23:00.000Z123",
                        "3",
                        "3",
                        "Z"
                }
        };

        assertSqlQueryRows("Datetime in STRING functions behaves not like ISO-time string", expectedRows, sqlQuery);
    }

    /**
     * #4221
     */
    @Test
    public void testDatetimeAsIsnullResultTypeInStringFunctions() {
        String sqlQuery = String.format(
                "SELECT upper(isnull(NaN, datetime)), lower(isnull(NaN, datetime)), " +
                        "replace(isnull(NaN, datetime), \"3\", \"0\"), length(isnull(NaN, datetime)), " +
                        "concat(isnull(NaN, datetime), \"word\"), concat(isnull(NaN, datetime), 123), " +
                        "locate(16, isnull(NaN, datetime)), locate(\"16\", isnull(NaN, datetime)), " +
                        "substr(isnull(NaN, datetime), 24)" +
                "FROM '%s' t1",
                series.getMetric()
        );

        String[][] expectedRows = {
                {
                        "2016-06-03T09:23:00.000Z",
                        "2016-06-03t09:23:00.000z",
                        "2016-06-00T09:20:00.000Z",
                        "24",
                        "2016-06-03T09:23:00.000Zword",
                        "2016-06-03T09:23:00.000Z123",
                        "3",
                        "3",
                        "Z"
                }
        };

        assertSqlQueryRows("Datetime (as ISNULL result) in STRING functions behaves not like ISO-time string", expectedRows, sqlQuery);
    }

    /**
     * #4221
     */
    @Test
    public void testTimeTypeInStringFunctions() throws Exception {
        String sqlQuery = String.format(
                "SELECT upper(time), lower(time), replace(time, \"0\", \"9\"), length(time), concat(time, \"word\"), " +
                        "concat(time, 123), locate(0, time), locate(\"0\", time), substr(time, 13)" +
                        "FROM '%s' t1",
                series.getMetric()
        );

        String[][] expectedRows = {
                {
                        "1464945780000",
                        "1464945780000",
                        "1464945789999",
                        "13",
                        "1464945780000word",
                        "1464945780000123",
                        "10",
                        "10",
                        "0"
                }
        };

        assertSqlQueryRows("Time in STRING functions behaves not like UNIX-time string", expectedRows, sqlQuery);
    }

    /**
     * #4221
     */
    @Test
    public void testTimeAsIsnullResultTypeInStringFunctions() throws Exception {
        String sqlQuery = String.format(
                "SELECT upper(isnull(NaN, time)), lower(isnull(NaN, time)), replace(isnull(NaN, time), \"0\", \"9\"), " +
                        "length(isnull(NaN, time)), concat(isnull(NaN, time), \"word\"), " +
                        "concat(isnull(NaN, time), 123), locate(0, isnull(NaN, time)), locate(\"0\", " +
                        "isnull(NaN, time)), substr(isnull(NaN, time), 13)" +
                        "FROM '%s' t1",
                series.getMetric()
        );

        String[][] expectedRows = {
                {
                        "1464945780000",
                        "1464945780000",
                        "1464945789999",
                        "13",
                        "1464945780000word",
                        "1464945780000123",
                        "10",
                        "10",
                        "0"
                }
        };

        assertSqlQueryRows("Time (as ISNULL result) in STRING functions behaves not like UNIX-time string", expectedRows, sqlQuery);
    }
}
