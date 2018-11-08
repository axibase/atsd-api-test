package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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

    @DataProvider
    public static Object[][] provideFunctions() {
        return new Object[][]{{"avg"}, {"first"}, {"lag"}, {"last"}, {"lead"}, {"max"}, {"MAX_VALUE_TIME"},
                {"median"}, {"min"}, {"MIN_VALUE_TIME"}, {"sum"}, {"wavg"}, {"wtavg"}};
    }

    @DataProvider
    public static Object[][] provideFunctionsTwo() {
        return new Object[][]{{"isnull"}, {"coalesce"}};
    }

    @DataProvider
    public static Object[][] provideFunctionsThree() {
        Object[][] one = provideFunctions();
        Object[][] two = provideFunctionsTwo();
        Object[][] result = new Object[one.length * two.length][2];
        int i = 0;
        for (Object[] objects : one)
            for (Object[] objects1 : two) {
                result[i][0] = objects[0];
                result[i++][1] = objects1[0];
            }
        return result;
    }

    @Issue("5757")
    @Test(dataProvider = "provideFunctions")
    public void testFunctionDatetimeArgument(String functionName) {
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
                "Table has different datatype",
                new HashSet<>(Collections.singletonList("xsd:dateTimeStamp")),
                new HashSet<>(extractColumnTypes(resultTable.getColumnsMetaData())));
    }

    @Issue("5757")
    @Test(dataProvider = "provideFunctions")
    public void testFunctionCurrentTimeStampArgument(String functionName) {
        String sqlQuery = String.format(
                "SELECT %s(CURRENT_TIMESTAMP) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
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
    @Test(dataProvider = "provideFunctionsTwo")
    public void testObjectFunction(String functionName) {
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
                "Table has different data",
                Collections.singletonList(TEST_DATETIME_VALUE),
                new ArrayList<>(extractRowContant(resultTable)));
    }

    @Issue("5757")
    @Test(dataProvider = "provideFunctionsThree")
    public void testTwoFunctionsDatetimeArgument(String functionName, String functionName2) {
        String sqlQuery = String.format(
                "SELECT %s(%s(datetime, datetime)) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
                functionName2,
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
    @Test(dataProvider = "provideFunctionsThree")
    public void testTwoFunctionsCurrentTimeStampArgument(String functionName, String functionName2) {
        String sqlQuery = String.format(
                "SELECT %s(%s(CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
                functionName2,
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
