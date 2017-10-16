package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.ErrorTemplate;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SqlOperatorComparisonStringTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-operator-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY1_NAME, TEST_METRIC_NAME, "key0", "value0");
        series1.addSamples(Sample.ofDateInteger("2016-06-03T09:25:00.000Z", 0));

        Series series2 = new Series(TEST_ENTITY2_NAME, TEST_METRIC_NAME, "key1", "value1");
        series2.addSamples(Sample.ofDateInteger("2016-06-03T09:25:01.000Z", 1));

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    @Issue("3172")
    @Test
    public void testEntityLess() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE entity < '%s'",
                TEST_METRIC_NAME, TEST_ENTITY2_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY1_NAME, "0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testEntityLessOrEquals() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE entity <= '%s'",
                TEST_METRIC_NAME, TEST_ENTITY2_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "0"),
                Arrays.asList(TEST_ENTITY2_NAME, "1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testEntityGreater() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE entity > '%s'",
                TEST_METRIC_NAME, TEST_ENTITY1_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY2_NAME, "1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testEntityGreaterOrEquals() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE entity >= '%s'",
                TEST_METRIC_NAME, TEST_ENTITY1_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "0"),
                Arrays.asList(TEST_ENTITY2_NAME, "1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testCastComparison() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE value >= '-1'",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {{"sql-operator-entity-1", "0"}, {"sql-operator-entity-2", "1"}};
        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3172")
    @Test
    public void testNullTagComparisonLess() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.t < '-1'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testNullTagComparisonLessEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.t <= '-1'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testNullTagComparisonGreater() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.t > '-1'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testNullTagComparisonGreaterEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.t >= '-1'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testTagComparisonLess() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.t < '-1'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testTagComparisonLessEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.key0 <= 'value'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testTagComparisonGreater() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.key0 > 'value'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY1_NAME, "0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3172")
    @Test
    public void testTagComparisonGreaterEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\" %nWHERE tags.key0 >= 'value'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY1_NAME, "0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3172")
    @Test
    public void testDatetimeComparison() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM \"%s\"  %nWHERE datetime >= 'value'",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String expectedErrorMessage = ErrorTemplate.Sql.invalidDateValue("value");
        assertBadRequest(expectedErrorMessage, response);
    }

}
