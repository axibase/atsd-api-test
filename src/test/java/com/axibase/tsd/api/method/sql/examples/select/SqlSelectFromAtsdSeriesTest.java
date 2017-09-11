package com.axibase.tsd.api.method.sql.examples.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

public class SqlSelectFromAtsdSeriesTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-select-from-atsd-series-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY_NAME, TEST_METRIC1_NAME);
        series1.addSamples(
                Sample.ofDateInteger("2016-06-17T19:16:01.000Z", 1),
                Sample.ofDateInteger("2016-06-17T19:16:02.000Z", 2)
        );

        Series series2 = new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME);
        series2.addSamples(
                Sample.ofDateInteger("2016-06-17T19:16:03.000Z", 3),
                Sample.ofDateInteger("2016-06-17T19:16:04.000Z", 4)
        );

        List<Series> seriesList = Arrays.asList(series1, series2);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3177")
    @Test(description = "Test for alias documentation example. " +
            "https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/aggregate-percentiles.md")
    public void testExample1() {
        String sqlQuery = String.format(
                "SELECT entity, metric, datetime, value  %nFROM atsd_series  %nWHERE metric = '%s' %n", TEST_METRIC1_NAME);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "2016-06-17T19:16:01.000Z", "1"),
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "2016-06-17T19:16:02.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3177")
    @Test
    public void testExample2() {
        String sqlQuery = String.format(
                "SELECT entity, metric, datetime, value  %nFROM atsd_series  %nWHERE metric IN ('%s','%s') order by datetime %n",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "2016-06-17T19:16:01.000Z", "1"),
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "2016-06-17T19:16:02.000Z", "2"),
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC2_NAME, "2016-06-17T19:16:03.000Z", "3"),
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC2_NAME, "2016-06-17T19:16:04.000Z", "4")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3177")
    @Test
    public void testExample3() {
        String sqlQuery = String.format(
                "SELECT entity, metric, datetime, value  %nFROM atsd_series  %nWHERE metric = '%s' OR metric = '%s' ORDER BY METRIC %n",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "2016-06-17T19:16:01.000Z", "1.0"),
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "2016-06-17T19:16:02.000Z", "2.0"),
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC2_NAME, "2016-06-17T19:16:03.000Z", "3.0"),
                Arrays.asList(TEST_ENTITY_NAME, TEST_METRIC2_NAME, "2016-06-17T19:16:04.000Z", "4.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("4259")
    @Test
    public void testErrorOnComplexMetricAndEntityFilter() {
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM atsd_series " +
                        "WHERE (metric = '%s') OR (metric = '%s' AND entity = '%s')",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME);

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest(
                "Invalid response with AND entity filter",
                "Invalid metric expression",
                response);
    }

    @Issue("4259")
    @Test
    public void testErrorOnComplexMetricOrEntityFilter() {
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM atsd_series " +
                        "WHERE (metric = '%s') OR (metric = '%s' OR entity = '%s')",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME);

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest(
                "Invalid response with OR entity filter",
                "Invalid metric expression",
                response);
    }

    @Issue("4259")
    @Test
    public void testErrorOnComplexMetricOrEntityFilterWithoutParentheses() {
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM atsd_series " +
                        "WHERE metric = '%s' OR metric = '%s' AND value = 1",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME);

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest(
                "Invalid response with complex metric filter without parentheses",
                "Invalid metric expression",
                response);
    }

    @Issue("4259")
    @Test
    public void testErrorOnComplexMetricOrEntityFilterLike() {
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s' OR metric = '%s' AND value = 1",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME);

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest(
                "Invalid response with complex LIKE metric filter",
                "Invalid metric expression",
                response);
    }

    @Issue("4259")
    @Test
    public void testErrorOnComplexMetricOrEntityFilterLikeRegex() {
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s' OR metric REGEX '%s' AND value = 1",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME);

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest(
                "Invalid response with complex LIKE and REGEX metric filter",
                "Invalid metric expression",
                response);
    }
}
