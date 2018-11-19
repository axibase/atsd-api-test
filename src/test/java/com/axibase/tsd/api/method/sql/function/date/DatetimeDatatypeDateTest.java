package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.testng.AssertJUnit.assertEquals;

public class DatetimeDatatypeDateTest extends SqlTest {
    private static final String TEST_PREFIX = "datetime-datatype-date-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_DATETIME_VALUE = "2018-11-07T09:30:06.000Z";
    private static ZonedDateTime zonedDateTime =
            ZonedDateTime.ofInstant(Instant.parse(TEST_DATETIME_VALUE), Util.getServerTimeZone().toZoneId());

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                Sample.ofDate(TEST_DATETIME_VALUE)
        );
        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public static Object[][] provideDateParts() {
        return new Object[][]{{"second", "2018-11-07T09:30:07.000Z"}, {"minute", "2018-11-07T09:31:06.000Z"},
                {"hour", "2018-11-07T10:30:06.000Z"}, {"day", "2018-11-08T09:30:06.000Z"},
                {"week", "2018-11-14T09:30:06.000Z"}, {"month", "2018-12-07T09:30:06.000Z"},
                {"quarter", "2019-02-07T09:30:06.000Z"}, {"year", "2019-11-07T09:30:06.000Z"}};
    }

    @DataProvider
    public static Object[][] provideExtractDateParts() {
        return new Object[][]{{"second", String.valueOf(zonedDateTime.getSecond())},
                {"minute", String.valueOf(zonedDateTime.getMinute())},
                {"hour", String.valueOf(zonedDateTime.getHour())},
                {"day", String.valueOf(zonedDateTime.getDayOfMonth())},
                {"dayofweek", String.valueOf(zonedDateTime.getDayOfWeek().getValue())},
                {"month", String.valueOf(zonedDateTime.getMonthValue())},
                {"quarter", String.valueOf(zonedDateTime.getMonth().ordinal() / 3 + 1)},
                {"year", String.valueOf(zonedDateTime.getYear())}};
    }

    @DataProvider
    public static Object[][] provideDateCheckFunctions() {
        return new Object[][]{{"is_workday", "true"}, {"is_weekday", "true"}};
    }

    @Issue("5757")
    @Test(dataProvider = "provideDateParts")
    public void testDateadd(String datePart, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT dateadd(%s, 1, datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s'",
                datePart,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Column has different datatype",
                "xsd:dateTimeStamp",
                resultTable.getColumnMetaData(0).getDataType());
        assertEquals(
                "Column has different data",
                expectedResult,
                resultTable.getRows().get(0).get(0));
    }

    @Issue("5757")
    @Test(dataProvider = "provideExtractDateParts")
    public void testExtractFunction(String functionName, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT extract(%s from datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s'",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Column has different datatype",
                "bigint",
                resultTable.getColumnMetaData(0).getDataType());
        assertEquals(
                "Column has different data",
                expectedResult,
                resultTable.getRows().get(0).get(0));
    }

    @Issue("5757")
    @Test(dataProvider = "provideExtractDateParts")
    public void testExtractionFunction(String functionName, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT %s (datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s'",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Column has different datatype",
                "bigint",
                resultTable.getColumnMetaData(0).getDataType());
        assertEquals(
                "Column has different data",
                expectedResult,
                resultTable.getRows().get(0).get(0));
    }

    @Issue("5757")
    @Test(dataProvider = "provideDateCheckFunctions")
    public void testDateCheckFunction(String functionName, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT %s (datetime, 'usa') %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s'",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Column has different datatype",
                "boolean",
                resultTable.getColumnMetaData(0).getDataType());
        assertEquals(
                "Column has different data",
                expectedResult,
                resultTable.getRows().get(0).get(0));
    }
}
