package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DatetimeTypeTest extends SqlTest {
    private static Series series1;
    private static Series series2;

    @BeforeClass
    public static void prepareData() throws Exception {
        series1 = Mocks.series();
        series2 = Mocks.series();

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    /**
     * #4221
     */
    @Test
    public void testDatetimeTypeAsIsnullResult() {
        String sqlQuery = String.format(
                "SELECT isnull(t1.datetime, t2.datetime) " +
                        "FROM '%s' t1 OUTER JOIN '%s' t2 " +
                        "LIMIT 2",
                series1.getMetric(), series2.getMetric()
        );

        String[][] expectedRows = {
                {
                        Mocks.ISO_TIME

                },
                {
                        Mocks.ISO_TIME
                }
        };

        assertSqlQueryRows("Datetime type mismatch (when joined using OUTER JOIN)", expectedRows, sqlQuery);
    }

    /**
     * #4221
     */
    @Test
    public void testTimeTypeAsIsnullResult() {
        String sqlQuery = String.format(
                "SELECT isnull(t1.time, t2.time) " +
                        "FROM '%s' t1 OUTER JOIN '%s' t2 " +
                        "LIMIT 2",
                series1.getMetric(), series2.getMetric()
        );

        String[][] expectedRows = {
                {
                        Mocks.MILLS_TIME.toString()

                },
                {
                        Mocks.MILLS_TIME.toString()
                }
        };

        assertSqlQueryRows("Time type mismatch (when joined using OUTER JOIN)", expectedRows, sqlQuery);
    }

    /**
     * #4221
     */
    @Test
    public void testDatetimeTypeInStringFunctionsWithOuterJoin() {
        // whole reproduction of #4221 failure
        String sqlQuery = String.format(
                "SELECT isnull(t1.value, t2.value), isnull(t1.datetime, t2.datetime), " +
                        "isnull(t1.time, t2.time), isnull(t1.entity, t2.entity) " +
                        "FROM '%s' t1 OUTER JOIN '%s' t2 " +
                        "ORDER BY 1 " +
                        "LIMIT 2",
                series1.getMetric(), series2.getMetric()
        );

        String[][] expectedRows = {
                {
                        Mocks.DECIMAL_VALUE.toString(), Mocks.ISO_TIME, Mocks.MILLS_TIME.toString(), series1.getEntity()

                },
                {
                        Mocks.DECIMAL_VALUE.toString(), Mocks.ISO_TIME, Mocks.MILLS_TIME.toString(), series2.getEntity()
                }
        };

        assertSqlQueryRows("Datetime (time) type mismatch (when joined using OUTER JOIN)", expectedRows, sqlQuery);
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
                "FROM '%s' t1 " +
                "LIMIT 1",
                series1.getMetric()
        );

        String[][] expectedRows = {
                {
                        Mocks.ISO_TIME.toUpperCase(),
                        Mocks.ISO_TIME.toLowerCase(),
                        Mocks.ISO_TIME.replace('3', '0'),
                        String.valueOf(Mocks.ISO_TIME.length()),
                        Mocks.ISO_TIME + "word",
                        Mocks.ISO_TIME + "123",
                        String.valueOf(Mocks.ISO_TIME.indexOf("16") + 1),
                        String.valueOf(Mocks.ISO_TIME.indexOf("16") + 1),
                        Mocks.ISO_TIME.substring(24 - 1)
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
                "FROM '%s' t1 " +
                "LIMIT 1",
                series1.getMetric()
        );

        String[][] expectedRows = {
                {
                        Mocks.ISO_TIME.toUpperCase(),
                        Mocks.ISO_TIME.toLowerCase(),
                        Mocks.ISO_TIME.replace('3', '0'),
                        String.valueOf(Mocks.ISO_TIME.length()),
                        Mocks.ISO_TIME + "word",
                        Mocks.ISO_TIME + "123",
                        String.valueOf(Mocks.ISO_TIME.indexOf("16") + 1),
                        String.valueOf(Mocks.ISO_TIME.indexOf("16") + 1),
                        Mocks.ISO_TIME.substring(24 - 1)
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
                "FROM '%s' t1 " +
                "LIMIT 1",
                series1.getMetric()
        );

        String[][] expectedRows = {
                {
                        Mocks.MILLS_TIME.toString().toUpperCase(),
                        Mocks.MILLS_TIME.toString().toLowerCase(),
                        Mocks.MILLS_TIME.toString().replace('0', '9'),
                        String.valueOf(Mocks.MILLS_TIME.toString().length()),
                        Mocks.MILLS_TIME.toString() + "word",
                        Mocks.MILLS_TIME.toString() + "123",
                        String.valueOf(Mocks.MILLS_TIME.toString().indexOf("0") + 1),
                        String.valueOf(Mocks.MILLS_TIME.toString().indexOf("0") + 1),
                        Mocks.MILLS_TIME.toString().substring(13 - 1)
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
                "FROM '%s' t1 " +
                "LIMIT 1",
                series1.getMetric()
        );

        String[][] expectedRows = {
                {
                        Mocks.MILLS_TIME.toString().toUpperCase(),
                        Mocks.MILLS_TIME.toString().toLowerCase(),
                        Mocks.MILLS_TIME.toString().replace('0', '9'),
                        String.valueOf(Mocks.MILLS_TIME.toString().length()),
                        Mocks.MILLS_TIME.toString() + "word",
                        Mocks.MILLS_TIME.toString() + "123",
                        String.valueOf(Mocks.MILLS_TIME.toString().indexOf("0") + 1),
                        String.valueOf(Mocks.MILLS_TIME.toString().indexOf("0") + 1),
                        Mocks.MILLS_TIME.toString().substring(13 - 1)
                }
        };

        assertSqlQueryRows("Time (as ISNULL result) in STRING functions behaves not like UNIX-time string", expectedRows, sqlQuery);
    }
}
