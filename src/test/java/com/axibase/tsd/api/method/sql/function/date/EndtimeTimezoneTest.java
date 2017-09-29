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
import java.util.TimeZone;

public class EndtimeTimezoneTest extends SqlTest {

    @AllArgsConstructor
    @Data
    private static class TestData {
        String timeZoneId;
        String endtimeKeyword;
        ChronoUnit truncationUint;
        boolean isFuture;
    }

    private static final Object[][] keywords = {
            {"current_minute", ChronoUnit.MINUTES, false},
            {  "current_hour",   ChronoUnit.HOURS, false},
            {   "current_day",    ChronoUnit.DAYS, false},
            {   "next_minute", ChronoUnit.MINUTES,  true},
            {     "next_hour",   ChronoUnit.HOURS,  true},
            {      "next_day",    ChronoUnit.DAYS,  true},
    };

    @DataProvider
    public Object[][] provideTestData() {
        String[] ids = TimeZone.getAvailableIDs();
        Object[][] result = new Object[ids.length * keywords.length][];
        for (int i = 0; i < ids.length; i++) {
            for (int j = 0; j < keywords.length; j++) {
                result[i * keywords.length + j] = new Object[]{
                        new TestData(ids[i], (String) keywords[j][0],
                                (ChronoUnit) keywords[j][1], (boolean) keywords[j][2])
                };
            }
        }
        return result;
    }

    @Issue("4171")
    @Test(
            description = "Test different timezones as argument of endtime function " +
                    "with current/future keywords",
            dataProvider = "provideTestData"
    )
    public void testEndtimeFunctionTimeZoneWithCurrenctKeywords(TestData testData) {
        String sqlQuery = String.format("SELECT now, endtime(%s, '%s')",
                testData.endtimeKeyword, testData.timeZoneId);

        StringTable resultTable = queryTable(sqlQuery);

        long now = Long.valueOf(resultTable.getValueAt(0, 0));
        long endtimeResult = TestUtil.truncateTime(now, testData.isFuture ? 1 : 0,
                    TimeZone.getTimeZone(testData.timeZoneId), testData.truncationUint);

        String[][] expectedRows = {
                {String.valueOf(now), String.valueOf(endtimeResult)}
        };

        String errorMessage = String.format("Wrong result of endtime(%s, '%s') function",
                testData.endtimeKeyword, testData.timeZoneId);
        assertRowsMatch(errorMessage, expectedRows, resultTable, sqlQuery);
    }
}
