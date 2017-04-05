package com.axibase.tsd.api.method.sql.function.dateparse;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Mocks;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateParseFunctionTest extends SqlTest {
    private static String tzName;

    @BeforeClass
    public static void prepareData() throws Exception {
        String res = queryATSDVersion().readEntity(String.class);
        JSONObject obj = new JSONObject(res);
        tzName = obj.getJSONObject("date").getJSONObject("timeZone").getString("name");

        SeriesMethod.insertSeriesCheck(Mocks.series());
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseISODefault() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "1970-01-01T01:00:00.000Z";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s')",
                runner.dateToParse
        );
        runner.sdFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
        runner.tz = TimeZone.getTimeZone(tzName);

        runner.runTest();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseISOFormat() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "1970-01-01T01:00:00.000Z";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s', \"yyyy-MM-dd'T'HH:mm:ss.SSSZZ\")",
                runner.dateToParse
        );
        runner.sdFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
        runner.tz = TimeZone.getTimeZone(tzName);

        runner.runTest();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseCustomFormat() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "31.03.2017 12:36:03.283";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s', 'dd.MM.yyyy HH:mm:ss.SSS')",
                runner.dateToParse
        );
        runner.sdFormat = "dd.MM.yyyy HH:mm:ss.SSS";
        runner.tz = TimeZone.getTimeZone(tzName);

        runner.runTest();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseTimezoneAndCustomFormat() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "31.03.2017 12:36:03.283 -08:00";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s', 'dd.MM.yyyy HH:mm:ss.SSS ZZ')",
                runner.dateToParse
        );
        runner.sdFormat = "dd.MM.yyyy HH:mm:ss.SSS X";
        runner.tz = TimeZone.getTimeZone(tzName);

        runner.runTest();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseLongTimezoneAndCustomFormat() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "31.03.2017 12:36:03.283";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s Europe/Berlin', 'dd.MM.yyyy HH:mm:ss.SSS ZZZ')",
                runner.dateToParse
        );
        runner.sdFormat = "dd.MM.yyyy HH:mm:ss.SSS";
        runner.tz = TimeZone.getTimeZone("Europe/Berlin");

        runner.runTest();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseCustomFormatWithLongTimezone() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "31.03.2017 12:36:03.283";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s', 'dd.MM.yyyy HH:mm:ss.SSS', 'Europe/Berlin')",
                runner.dateToParse
        );
        runner.sdFormat = "dd.MM.yyyy HH:mm:ss.SSS";
        runner.tz = TimeZone.getTimeZone("Europe/Berlin");

        runner.runTest();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseCustomFormatWithNumericTimezone() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "31.03.2017 12:36:03.283";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s', 'dd.MM.yyyy HH:mm:ss.SSS', '+01:00')",
                runner.dateToParse
        );
        runner.sdFormat = "dd.MM.yyyy HH:mm:ss.SSS";
        runner.tz = TimeZone.getTimeZone("GMT+01:00");

        runner.runTest();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseTimezoneBoth() {
        TestRunner runner = new TestRunner();
        runner.dateToParse = "31.03.2017 12:36:03.283";
        runner.sqlQuery = String.format(
                "SELECT date_parse('%s Europe/Berlin', 'dd.MM.yyyy HH:mm:ss.SSS ZZZ', 'Europe/Berlin')",
                runner.dateToParse
        );
        runner.sdFormat = "dd.MM.yyyy HH:mm:ss.SSS";
        runner.tz = TimeZone.getTimeZone("Europe/Berlin");

        runner.runTest();
    }

    private class TestRunner {
        String dateToParse;
        String sqlQuery;
        String sdFormat;
        TimeZone tz;

        void runTest() {
            DateFormat format = new SimpleDateFormat(sdFormat);
            format.setTimeZone(tz);

            String[][] expectedRows;
            try {
                expectedRows = new String[][]{{
                        Long.toString(format.parse(dateToParse).getTime())
                }};
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            assertSqlQueryRows("Incorrect result for date_parse in " + sqlQuery, expectedRows, sqlQuery);
        }
    }
}
