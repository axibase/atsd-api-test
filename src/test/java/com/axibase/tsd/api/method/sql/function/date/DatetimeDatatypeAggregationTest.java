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

public class DatetimeDatatypeAggregationTest extends SqlTest {
    private static final String TEST_PREFIX = "datetime-datatype-aggregation-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_METRIC_NAME_LEAD_LAG = TEST_PREFIX + "metric-lead-lag";
    private static final String TEST_DATETIME_VALUE = "2018-11-07T09:30:06.000Z";
    private static final String TEST_DATETIME_VALUE_2 = "2018-11-08T09:30:06.000Z";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        Series series2 = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME_LEAD_LAG);
        series.addSamples(
                Sample.ofDate(TEST_DATETIME_VALUE)
        );
        series2.addSamples(
                Sample.ofDate(TEST_DATETIME_VALUE),
                Sample.ofDate(TEST_DATETIME_VALUE_2)
        );
        SeriesMethod.insertSeriesCheck(series, series2);
    }

    @DataProvider
    public static Object[][] provideAggregateFunctions() {
        return new Object[][]{{"first"}, {"last"}, {"max"}, {"min"}};
    }

    @DataProvider
    public static Object[][] provideLeadLagWithResultRowValue() {
        return new Object[][]{{"lag", 1, TEST_DATETIME_VALUE}, {"lead", 0, TEST_DATETIME_VALUE_2}};
    }

    @DataProvider
    public static Object[][] provideOtherFunctionsForSameDatetimeParameters() {
        return new Object[][]{{"isnull"}, {"coalesce"}};
    }

    @DataProvider
    public static Object[][] provideOtherFunctionAndArgumentDifferentType() {
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
                resultTable.getColumnMetaData(0).getDataType());
        assertEquals(
                "Column has different data",
                TEST_DATETIME_VALUE,
                resultTable.getRows().get(0).get(0));
    }

    @Issue("5757")
    @Test(dataProvider = "provideLeadLagWithResultRowValue")
    public void testLeadLagFunction(String functionName, int offset, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT %s(datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s'",
                functionName,
                TEST_METRIC_NAME_LEAD_LAG,
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
                resultTable.getRows().get(offset).get(0));
    }

    @Issue("5757")
    @Test(dataProvider = "provideOtherFunctionsForSameDatetimeParameters")
    public void testOtherFunctionSameParameters(String functionName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime, datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s'",
                functionName,
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
                TEST_DATETIME_VALUE,
                resultTable.getRows().get(0).get(0));
    }

    @Issue("5757")
    @Test(dataProvider = "provideOtherFunctionAndArgumentDifferentType")
    public void testOtherFunctionDifferentParameters(String functionName, String parameterName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime, %s) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s'",
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
