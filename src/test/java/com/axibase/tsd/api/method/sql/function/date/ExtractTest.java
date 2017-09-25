package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.function.Function;

@Slf4j
public class ExtractTest extends SqlTest {
    private static final String DATE_STRING_A = "2016-06-01T15:04:03.002Z";
    private static final String DATE_STRING_B = "2017-07-02T13:06:01.004Z";
    private static ZonedDateTime dateA = zonedFromStringDate(DATE_STRING_A);
    private static ZonedDateTime dateB = zonedFromStringDate(DATE_STRING_B);
    private static final String METRIC_NAME = Mocks.metric();

    private static ZonedDateTime zonedFromStringDate(String date) {
        return ZonedDateTime.ofInstant(Instant.parse(date), Util.getServerTimeZone().toZoneId());
    }

    private enum DateAccessor {
        GET_YEAR("year", date -> String.valueOf(date.getYear())),
        GET_QUARTER("quarter", date -> String.valueOf(date.getMonth().ordinal() / 3 + 1)),
        GET_MONTH("month", date -> String.valueOf(date.getMonth().getValue())),
        GET_DAY("day", date -> String.valueOf(date.getDayOfMonth())),
        GET_DAYOFWEEK("dayofweek", date -> String.valueOf(date.getDayOfWeek().getValue())),
        GET_HOUR("hour", date -> String.valueOf(date.getHour())),
        GET_MINUTE("minute", date -> String.valueOf(date.getMinute())),
        GET_SECOND("second", date -> String.valueOf(date.getSecond()));

        String part;
        Function<ZonedDateTime, String> accessFunction;

        DateAccessor(String part, Function<ZonedDateTime, String> accessFunction) {
            this.part = part;
            this.accessFunction = accessFunction;
        }

        String apply(ZonedDateTime date) {
            return accessFunction.apply(date);
        }
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        series.addSamples(
                Sample.ofDateInteger(DATE_STRING_A, 1),
                Sample.ofDateInteger(DATE_STRING_B, 2)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public Object[][] provideDatePartNameAndAccessor() {
        DateAccessor[] accessors = DateAccessor.values();
        String[] extractionSources = new String[]{"time", "datetime"};

        Object[][] testData = new Object[accessors.length * extractionSources.length][];
        for (int i = 0; i < accessors.length; i++) {
            for (int j = 0; j < extractionSources.length; j++) {
                testData[i * extractionSources.length + j] = new Object[] {
                        extractionSources[j], accessors[i]
                };
            }
        }
        return testData;
    }

    @Issue("4393")
    @Test(
            dataProvider = "provideDatePartNameAndAccessor",
            description = "Test extract(... from ...) with " +
                    "different parts (year, month, ...) and sources (time, datetime)"
    )
    public void testExtractFrom(String source, DateAccessor accessor) {
        String sqlQuery = String.format(
                "SELECT extract(%s FROM %s) " +
                        "FROM \"%s\"",
                accessor.part, source, METRIC_NAME
        );

        String[][] expectedRows = {
                {accessor.apply(dateA)},
                {accessor.apply(dateB)},
        };

        String assertMessage = String.format("Wrong result for extract(%s FROM %s)", accessor.part, source);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }

    @Issue("4393")
    @Test(
            dataProvider = "provideDatePartNameAndAccessor",
            description = "Test different extraction functions " +
                    "(year, month, ...) and sources (time, datetime)"
    )
    public void testIndividualExtractFunctions(String source, DateAccessor accessor) {
        String sqlQuery = String.format(
                "SELECT %s(%s) " +
                        "FROM \"%s\"",
                accessor.part, source, METRIC_NAME
        );

        String[][] expectedRows = {
                {accessor.apply(dateA)},
                {accessor.apply(dateB)},
        };

        String assertMessage = String.format("Wrong result for function %s with argument %s", accessor.part, source);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }

    @Issue("4393")
    @Test(
            dataProvider = "provideDatePartNameAndAccessor",
            description = "Test when using extract(... FROM ...) result in WHERE ... IN (...)"
    )
    public void testWhereExtractIn(String source, DateAccessor accessor) {
        String extractedValue = accessor.apply(dateA);

        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE extract(%s FROM %s) IN (%s, 2018, 0, 'a')",
                METRIC_NAME, accessor.part, source, extractedValue
        );

        String[][] expectedRows = {
                {DATE_STRING_A, "1"}
        };

        String assertMessage = String.format("Wrong result when using " +
                "WHERE extract(%s FROM %s) IN (...)", accessor.part, source);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }

    @Issue("4393")
    @Test(
            dataProvider = "provideDatePartNameAndAccessor",
            description = "Test when using date part extraction function result in WHERE ... IN (...)"
    )
    public void testWhereIndividualExtractIn(String source, DateAccessor accessor) {
        String extractedValue = accessor.apply(dateA);

        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE %s(%s) IN (%s, 2018, 0, 'a')",
                METRIC_NAME, accessor.part, source, extractedValue
        );

        String[][] expectedRows = {
                {DATE_STRING_A, "1"}
        };

        String assertMessage = String.format("Wrong when using WHERE %s(%s) IN (...)", accessor.part, source);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }
}
