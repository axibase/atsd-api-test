package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.text.ParseException;

public class DateParseFunctionTest extends SqlTest {
    @Issue("4050")
    @Test
    public void testDateParseISODefault() {
        String sqlQuery = "SELECT date_parse('1970-01-01T01:00:00.000Z')";

        String[][] expectedRows = {{"3600000"}};

        assertSqlQueryRows("Incorrect result for default date_parse",
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseISOFormat() {
        String sqlQuery = "SELECT date_parse('1970-01-01T01:00:00.000Z', " +
                "'yyyy-MM-dd''T''HH:mm:ss.SSSZZ')";

        String[][] expectedRows = {{"3600000"}};

        assertSqlQueryRows("Incorrect result for date_parse with ISO (as custom) format",
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseCustomFormat() throws ParseException {
        /* Use server local time */
        String format = "dd.MM.yyyy HH:mm:ss.SSS";
        String strDate = "31.03.2017 12:36:03.283";
        String sqlQuery = String.format("SELECT date_parse('%s', '" + format + "')", strDate);

        final long time = Util.parseAsMillis(strDate, format, Util.getServerZoneId());
        String[][] expectedRows = {{Long.toString(time)}};

        assertSqlQueryRows("Incorrect result for date_parse with custom format",
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseTimezoneAndCustomFormat() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283 -08:00', " +
                "'dd.MM.yyyy HH:mm:ss.SSS ZZ')";

        String[][] expectedRows = {{"1490992563283"}};

        assertSqlQueryRows("Incorrect result for date_parse with custom format with timezone",
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseLongTimezoneAndCustomFormat() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283 Europe/Berlin', " +
                "'dd.MM.yyyy HH:mm:ss.SSS ZZZ')";

        String[][] expectedRows = {{"1490956563283"}};

        assertSqlQueryRows("Incorrect result for date_parse with custom format with long timezone",
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseCustomFormatWithLongTimezone() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283', " +
                "'dd.MM.yyyy HH:mm:ss.SSS', 'Europe/Berlin')";

        String[][] expectedRows = {{"1490956563283"}};

        assertSqlQueryRows("Incorrect result for date_parse with long timezone argument",
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseCustomFormatWithNumericTimezone() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283', " +
                "'dd.MM.yyyy HH:mm:ss.SSS', '+01:00')";

        String[][] expectedRows = {{"1490960163283"}};

        assertSqlQueryRows("Incorrect result for date_parse with numeric timezone argument",
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseTimezoneBoth() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283 Europe/Berlin', " +
                "'dd.MM.yyyy HH:mm:ss.SSS ZZZ', 'Europe/Berlin')";

        String[][] expectedRows = {{"1490956563283"}};

        assertSqlQueryRows("Incorrect result for date_parse with with long timezone in format and argument" + sqlQuery,
                expectedRows, sqlQuery);
    }

    @Issue("4050")
    @Test
    public void testDateParseTimezoneBothNumeric() {
        String sqlQuery = "SELECT date_parse('31.03.2017 12:36:03.283 +01:00', " +
                "'dd.MM.yyyy HH:mm:ss.SSS ZZ', '+01:00')";

        String[][] expectedRows = {{"1490960163283"}};

        assertSqlQueryRows("Incorrect result for date_parse with with numeric timezone in format and argument" + sqlQuery,
                expectedRows, sqlQuery);
    }
}
