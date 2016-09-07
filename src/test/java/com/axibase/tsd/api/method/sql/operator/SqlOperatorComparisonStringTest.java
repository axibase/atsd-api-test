package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.AtsdErrorMessage.SQL_SYNTAX_COMPARISON_TPL;

public class SqlOperatorComparisonStringTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-operator-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(),
                series2 = new Series();

        series1.setMetric(TEST_METRIC_NAME);
        series1.setEntity(TEST_ENTITY1_NAME);
        series1.addData(new Sample("2016-06-03T09:25:00.000Z", "0"));
        series1.addTag("key0", "value0");

        series2.setMetric(TEST_METRIC_NAME);
        series2.setEntity(TEST_ENTITY2_NAME);
        series2.addData(new Sample("2016-06-03T09:25:01.000Z", "1"));
        series2.addTag("key1", "value1");


        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    /**
     * #3172
     */
    @Test
    public void testEntityLess() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE entity < '%s'",
                TEST_METRIC_NAME, TEST_ENTITY2_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY1_NAME, "0")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testEntityLessOrEquals() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE entity <= '%s'",
                TEST_METRIC_NAME, TEST_ENTITY2_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "0"),
                Arrays.asList(TEST_ENTITY2_NAME, "1")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testEntityGreater() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE entity > '%s'",
                TEST_METRIC_NAME, TEST_ENTITY1_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY2_NAME, "1")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testEntityGreaterOrEquals() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE entity >= '%s'",
                TEST_METRIC_NAME, TEST_ENTITY1_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "0"),
                Arrays.asList(TEST_ENTITY2_NAME, "1")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testValueComparisonError() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE value >= '-1'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(response, String.format(SQL_SYNTAX_COMPARISON_TPL, '2', "19", "'-1'"));
    }

    /**
     * #3172
     */
    @Test
    public void testNullTagComparisonLess() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.t < '-1'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testNullTagComparisonLessEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.t <= '-1'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testNullTagComparisonGreater() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.t > '-1'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testNullTagComparisonGreaterEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.t >= '-1'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testTagComparisonLess() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.t < '-1'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testTagComparisonLessEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.key0 <= 'value'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testTagComparisonGreater() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.key0 > 'value'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY1_NAME, "0")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testTagComparisonGreaterEqual() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s' %nWHERE tags.key0 >= 'value'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY1_NAME, "0")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3172
     */
    @Test
    public void testMetricComparison() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM atsd_series  %nWHERE metric >= 'value'"
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(response, String.format(SQL_SYNTAX_COMPARISON_TPL, "2", "13", "metric >="));
    }

    /**
     * #3172
     */
    @Test
    public void testDatetimeComparison() {
        String sqlQuery = String.format(
                "SELECT entity,value FROM '%s'  %nWHERE datetime >= 'value'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(response, "Failed to parse date value");
    }

}