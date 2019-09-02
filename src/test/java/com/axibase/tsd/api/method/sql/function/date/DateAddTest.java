package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.sql.SqlTest;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class DateAddTest extends SqlTest {
    private static final String SQL_QUERY_TEMPLATE = "SELECT date_format(DATEADD(%s, 1, '%s'), 'yyyy-MM-dd HH:mm:ss')";

    private Object[][] dateTime() {
        return new String[][] {
                {"2019"},
                {"2019-01"},
                {"2019-01-01"},
                {"2019-01-01 00:00:00"},
                {"2019-01-01 00:00:00.000"},
                {"2019-01-01T00:00:00.000+05:45"} //Kathmandu timezone
        };
    }

    private Object[][] timeUnitAndExpectedResult() {
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
        for(Object[] datetime: dateTime()) {
            for(Object[] unitAndResult : timeUnitAndExpectedResult()) {
                result.add(new Object[]{datetime[0], unitAndResult[0], unitAndResult[1]});
            }
        }
        return result.toArray(new Object[dateTime().length * timeUnitAndExpectedResult().length][3]);
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

}
