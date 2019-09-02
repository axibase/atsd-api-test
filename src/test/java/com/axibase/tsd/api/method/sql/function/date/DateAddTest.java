package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.sql.SqlTest;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DateAddTest extends SqlTest {
    private static final String SQL_QUERY_TEMPLATE = "SELECT date_format(DATEADD(%s, 1, '%s'), 'yyyy-MM-dd HH:mm:ss')";

    @DataProvider
    public Object[][] dateTime() {
        return new String[][] {
                {"2019"},
                {"2019-01"},
                {"2019-01-01"},
                {"2019-01-01 00:00:00"},
                {"2019-01-01 00:00:00.000"},
                {"2019-01-01T00:00:00.000+05:45"} //Kathmandu timezone
        };
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with second argument"
    )
    @Issue("6528")
    public void testAddSecond(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "second", isoTime);
        String[][] sqlQueryRow = {
                {"2019-01-01 00:00:01"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with minute argument"
    )
    @Issue("6528")
    public void testAddMinutes(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "minute", isoTime);
        String[][] sqlQueryRow = {
                {"2019-01-01 00:01:00"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with hour argument"
    )
    @Issue("6528")
    public void testAddHours(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "hour", isoTime);
        String[][] sqlQueryRow = {
                {"2019-01-01 01:00:00"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with day argument"
    )
    @Issue("6528")
    public void testAddDays(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "day", isoTime);
        String[][] sqlQueryRow = {
                {"2019-01-02 00:00:00"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with week argument"
    )
    @Issue("6528")
    public void testAddWeeks(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "week", isoTime);
        String[][] sqlQueryRow = {
                {"2019-01-08 00:00:00"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with month argument"
    )
    @Issue("6528")
    public void testAddMonths(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "month", isoTime);
        String[][] sqlQueryRow = {
                {"2019-02-01 00:00:00"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with quarter argument"
    )
    @Issue("6528")
    public void testAddQuarters(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "quarter", isoTime);
        String[][] sqlQueryRow = {
                {"2019-04-01 00:00:00"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    @Test(
            dataProvider = "dateTime",
            description = "Test DATEADD with year argument"
    )
    @Issue("6528")
    public void testAddYears(String isoTime) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, "year", isoTime);
        String[][] sqlQueryRow = {
                {"2020-01-01 00:00:00"}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }
}
