package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class DatetimeDatatypeTest extends SqlTest {
    private static final String TEST_PREFIX = "datetime-datatype-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_DATETIME_VALUE = "2018-11-07T09:30:06.000Z";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                Sample.ofDate(TEST_DATETIME_VALUE)
        );
        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public static Object[][] provideAggregateFunctions() {
        return new Object[][]{{"avg"}, {"first"}, {"lag"}, {"last"}, {"lead"}, {"max"}, {"MAX_VALUE_TIME"},
                {"median"}, {"min"}, {"MIN_VALUE_TIME"}, {"sum"}};
    }

    @DataProvider
    public static Object[][] provideOtherFunctions() {
        return new Object[][]{{"isnull"}, {"coalesce"}};
    }

    @DataProvider
    public static Object[][] provideOtherFunctionAlternativeParameter() {
        return new Object[][]{{"isnull", "null"}, {"isnull", "value"}, {"isnull", "tags.ok"},
                {"coalesce", "null"}, {"coalesce", "value"}, {"coalesce", "tags.ok"}};
    }

    @Issue("5757")
    @Test(dataProvider = "provideAggregateFunctions")
    public void testAggregationFunction(String functionName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Column has different datatype",
                "xsd:dateTimeStamp",
                resultTable.getColumnsMetaData()[0].getDataType());
    }

    @Issue("5757")
    @Test(dataProvider = "provideOtherFunctions")
    public void testOtherFunctionSameParameters(String functionName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime, datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n" +
                        "LIMIT 1",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Column has different datatype",
                "xsd:dateTimeStamp",
                resultTable.getColumnsMetaData()[0].getDataType());
    }

    @Issue("5757")
    @Test(dataProvider = "provideOtherFunctionAlternativeParameter")
    public void testOtherFunctionDifferentParameters(String functionName, String parameterName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime, %s) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n" +
                        "LIMIT 1",
                functionName,
                parameterName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Column has different data",
                TEST_DATETIME_VALUE,
                resultTable.getRows().get(0).get(0));
    }
}
