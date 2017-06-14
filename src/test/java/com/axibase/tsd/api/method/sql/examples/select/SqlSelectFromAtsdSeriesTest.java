package com.axibase.tsd.api.method.sql.examples.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
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
                new Sample("2016-06-17T19:16:01.000Z", 1),
                new Sample("2016-06-17T19:16:02.000Z", 2)
        );

        Series series2 = new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME);
        series2.addSamples(
                new Sample("2016-06-17T19:16:03.000Z", 3),
                new Sample("2016-06-17T19:16:04.000Z", 4)
        );

        List<Series> seriesList = Arrays.asList(series1, series2);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3177
     * Test for alias documentation example.
     *
     * @see <a href="Aggregate Percentiles">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/aggregate-percentiles.md</a>
     */
    @Test
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

    /**
     * #3177
     */
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

    /**
     * #3177
     */
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

    /**
     * #4259
     */
    @Test
    public void testErrorOnComplexMetricFilter() {
        String sqlQuery = String.format(
                "SELECT entity, metric, datetime, value " +
                        "FROM atsd_series " +
                        "WHERE (metric = '%s') OR (metric = '%s' AND entity = '%s')",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME);

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Invalid metric expression", response);
    }
}
