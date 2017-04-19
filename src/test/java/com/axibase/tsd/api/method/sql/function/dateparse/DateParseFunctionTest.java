package com.axibase.tsd.api.method.sql.function.dateparse;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.TestUtil;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateParseFunctionTest extends SqlTest {
    private static TimeZone timeZone;

    @BeforeClass
    public static void prepareData() throws Exception {
        timeZone = TestUtil.getServerTimeZone();
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseISODefault() {
        String sqlQuery = "SELECT date_parse('1970-01-01T01:00:00.000Z')";

        String[][] expectedRows = {{"3600000"}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseISOFormat() {
        String sqlQuery = "SELECT date_parse('1970-01-01T01:00:00.000Z', " +
                "\"yyyy-MM-dd'T'HH:mm:ss.SSS ZZ\")";

        String[][] expectedRows = {{"3600000"}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseCustomFormat() throws ParseException {
        String strDate = "31.03.2017 12:36:03.283";
        String sqlQuery = String.format("SELECT date_parse('%s', " +
                "'dd.MM.yyyy HH:mm:ss.SSS')", strDate);

        DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        format.setTimeZone(timeZone);

        String[][] expectedRows = {{Long.toString(format.parse(strDate).getTime())}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseTimezoneAndCustomFormat() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283 -08:00', " +
                "'dd.MM.yyyy HH:mm:ss.SSS ZZ')";

        String[][] expectedRows = {{"1490992563283"}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseLongTimezoneAndCustomFormat() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283 Europe/Berlin', " +
                "'dd.MM.yyyy HH:mm:ss.SSS ZZZ')";

        String[][] expectedRows = {{"1490956563283"}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseCustomFormatWithLongTimezone() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283', " +
                "'dd.MM.yyyy HH:mm:ss.SSS', 'Europe/Berlin')";

        String[][] expectedRows = {{"1490956563283"}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseCustomFormatWithNumericTimezone() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283', " +
                "'dd.MM.yyyy HH:mm:ss.SSS', '+01:00')";

        String[][] expectedRows = {{"1490960163283"}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }

    /**
     * #4050
     */
    @Test
    public void testDateParseTimezoneBoth() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283 Europe/Berlin', " +
                "'dd.MM.yyyy HH:mm:ss.SSS ZZZ', 'Europe/Berlin')";

        String[][] expectedRows = {{"1490956563283"}};

        assertSqlQueryRows("Incorrect result for date_parse in: " + sqlQuery,
                expectedRows, sqlQuery);
    }
}
