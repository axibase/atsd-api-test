package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Issue("5757")
    @Test(dataProvider = "provideCombinationAggregateResource", dataProviderClass = DatetimeDatatypeProvider.class)
    public void testAggregationFunction(String functionName, String resourceName) {
        String sqlQuery = String.format(
                "SELECT %s(%s) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
                resourceName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Table has different datatype",
                new HashSet<>(Collections.singletonList("xsd:dateTimeStamp")),
                new HashSet<>(extractColumnTypes(resultTable.getColumnsMetaData())));
    }

    @Issue("5757")
    @Test(dataProvider = "provideCombinationOtherResource", dataProviderClass = DatetimeDatatypeProvider.class)
    public void testOtherFunction(String functionName, String resourceName) {
        String sqlQuery = String.format(
                "SELECT %s(%s, %s) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n" +
                        "LIMIT 1",
                functionName,
                resourceName,
                resourceName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Table has different data",
                Collections.singletonList(TEST_DATETIME_VALUE),
                new ArrayList<>(extractRowContant(resultTable)));
    }

    @Issue("5757")
    @Test(dataProvider = "provideCombinationFunctions", dataProviderClass = DatetimeDatatypeProvider.class)
    public void testAggregationOtherFunction(String aggregateFunctionName, String otherFunctionName, String resourceName) {
        String sqlQuery = String.format(
                "SELECT %s(%s(%s, %s)) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                aggregateFunctionName,
                otherFunctionName,
                resourceName,
                resourceName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertEquals(
                "Table has different datatype",
                new HashSet<>(Collections.singletonList("xsd:dateTimeStamp")),
                new HashSet<>(extractColumnTypes(resultTable.getColumnsMetaData())));
    }

    private List<String> extractColumnTypes(ColumnMetaData[] columnMetaData) {
        return Arrays.stream(columnMetaData).
                map(ColumnMetaData::getDataType).
                collect(Collectors.toList());
    }

    private List<String> extractRowContant(StringTable stringTable) {
        return stringTable.getRows().stream().
                flatMap(Collection::stream).
                collect(Collectors.toList());
    }
}
