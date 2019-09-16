package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.sql.SqlTest;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DateAddTest extends SqlTest {
    private static final String SQL_QUERY_TEMPLATE = "SELECT date_format(DATEADD(%s, 1, '%s'), 'yyyy-MM-dd HH:mm:ss')";
    private static final Date DATE = new Date(1546300800000l); //2019-01-01 00:00:00

    private String[] patterns() {
        return new String[]{
                "yyyy",
                "yyyy-MM",
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSS",
                //"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        };
    }

    private String[][] timeUnitAndExpectedResult() {
        return new String[][] {
                {"second", "2019-01-01 00:00:01"},
                {"minute", "2019-01-01 00:01:00"},
                {"hour", "2019-01-01 01:00:00"},
                {"day", "2019-01-02 00:00:00"},
                {"week", "2019-01-08 00:00:00"},
                {"month", "2019-02-01 00:00:00"},
                {"quarter", "2019-04-01 00:00:00"},
                {"year", "2020-01-01 00:00:00"},
        };
    }

    @DataProvider
    public Object[][] datetimeUnitAndResult() {
        List<Object[]> result = new ArrayList<>();
        String[][] unitAndExpectedResult = timeUnitAndExpectedResult();
        for (String pattern : patterns()) {
            for (String[] unitAndResult : unitAndExpectedResult) {
                result.add(new Object[]{simpleDateFormat(pattern), unitAndResult[0], unitAndResult[1]});
            }
        }
        return result.toArray(new Object[0][0]);
    }

    @Test(
            dataProvider = "datetimeUnitAndResult",
            description = "Test DATEADD with different arguments"
    )
    @Issue("6528")
    public void dateAddTest(String datetime, String timeUnit, String expectedResult) {
        String sqlQuery = String.format(SQL_QUERY_TEMPLATE, timeUnit, datetime);
        String[][] sqlQueryRow = {
                {expectedResult}
        };
        assertSqlQueryRows(sqlQueryRow, sqlQuery);
    }

    private String simpleDateFormat(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(DATE);
    }
}
