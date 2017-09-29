package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class EndtimeTimezoneTest extends SqlTest {

    @AllArgsConstructor
    @Data
    private static class TestData {
        String timeZoneId;
        Truncation truncation;
        int plusAmount;
        PlusUnit plusUnit;
    }

    @AllArgsConstructor
    @Data
    private static class Truncation {
        String endtimeKeyword;
        ChronoUnit truncationUnit;
        boolean isFuture;
    }

    @AllArgsConstructor
    @Data
    private static class PlusUnit {
        String strUnit;
        ChronoUnit unit;
    }

    private static final String[] tzList = {
            "America/Los_Angeles",
            "Europe/London",
            "Europe/Moscow",
            "Asia/Kathmandu",
            "Asia/Kolkata",
            "Pacific/Chatham",
            "Pacific/Kiritimati",
            "Etc/UTC",
    };

    private static final Truncation[] truncations = {
            new Truncation("current_minute", ChronoUnit.MINUTES, false),
            new Truncation(  "current_hour",   ChronoUnit.HOURS, false),
            new Truncation(   "current_day",    ChronoUnit.DAYS, false),
            new Truncation(   "next_minute", ChronoUnit.MINUTES,  true),
            new Truncation(     "next_hour",   ChronoUnit.HOURS,  true),
            new Truncation(      "next_day",    ChronoUnit.DAYS,  true)
    };

    private static final PlusUnit[] endtimeUnits = {
            new PlusUnit("minute", ChronoUnit.MINUTES),
            new PlusUnit(  "hour",   ChronoUnit.HOURS),
            new PlusUnit(   "day",    ChronoUnit.DAYS),
            new PlusUnit( "month",  ChronoUnit.MONTHS)
    };

    private static final int[] shifts = {-6, 0, 6};

    @DataProvider
    public Object[][] provideTestData() {
        List<Object[]> casesList = new ArrayList<>();
        for (String id : tzList) {
            for (Truncation truncation : truncations) {
                for (PlusUnit endtimeUnit : endtimeUnits) {
                    for (int shift : shifts) {
                        casesList.add(new Object[]{new TestData(id, truncation, shift, endtimeUnit)});
                    }
                }
            }
        }
        Object[][] result = new Object[casesList.size()][];
        casesList.toArray(result);
        return result;
    }

    @Issue("4171")
    @Test(
            description = "Test different timezones as argument of endtime function " +
                    "with current/future keywords",
            dataProvider = "provideTestData"
    )
    public void testEndtimeFunctionTimeZoneWithCurrentKeywords(TestData testData) {
        StringBuilder endtimePlusBuilder = new StringBuilder();
        int count = testData.plusAmount;
        if (count != 0) {
            endtimePlusBuilder.append(count < 0 ? " - " : " + ");
            endtimePlusBuilder.append(count < 0 ? -count : count);
            endtimePlusBuilder.append(" * ");
            endtimePlusBuilder.append(testData.plusUnit.strUnit);
        }

        String functionUsage = String.format("endtime(%s %s, '%s')",
                testData.truncation.endtimeKeyword, endtimePlusBuilder, testData.timeZoneId);

        String sqlQuery = "SELECT now, " + functionUsage;

        StringTable resultTable = queryTable(sqlQuery);

        long now = Long.valueOf(resultTable.getValueAt(0, 0));
        long endtimeResult = TestUtil.truncateTime(now,
                TimeZone.getTimeZone(testData.timeZoneId), testData.truncation.truncationUnit);

        if (testData.truncation.isFuture) {
            endtimeResult = TestUtil.plusTime(endtimeResult, 1,
                    TimeZone.getTimeZone(testData.timeZoneId), testData.truncation.truncationUnit);
        }

        if (count != 0) {
            endtimeResult = TestUtil.plusTime(endtimeResult, count,
                    TimeZone.getTimeZone(testData.timeZoneId), testData.plusUnit.unit);
        }

        String[][] expectedRows = {
                {String.valueOf(now), String.valueOf(endtimeResult)}
        };

        String errorMessage = String.format("Wrong result of %s function", functionUsage);
        assertRowsMatch(errorMessage, expectedRows, resultTable, sqlQuery);
    }
}
