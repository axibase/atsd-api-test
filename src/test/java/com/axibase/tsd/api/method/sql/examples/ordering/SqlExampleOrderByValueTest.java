package com.axibase.tsd.api.method.sql.examples.ordering;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;


public class SqlExampleOrderByValueTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-order-by-value-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private static final String TEST_ENTITY3_NAME = TEST_PREFIX + "entity-3";

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Entity.register(TEST_ENTITY3_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);

        Series series1 = new Series(),
                series2 = new Series(),
                series3 = new Series();
        series1.setMetric(TEST_METRIC_NAME);
        series1.setEntity(TEST_ENTITY1_NAME);
        series1.addData(new Sample("2016-07-27T22:41:52.000Z", "0"));
        series1.addData(new Sample("2016-07-27T22:41:51.000Z", "1"));
        series1.addData(new Sample("2016-07-27T22:41:50.000Z", "2"));

        series2.setMetric(TEST_METRIC_NAME);
        series2.setEntity(TEST_ENTITY2_NAME);
        series2.addData(new Sample("2016-07-27T22:41:52.000Z", "2"));
        series2.addData(new Sample("2016-07-27T22:41:51.000Z", "3"));
        series2.addData(new Sample("2016-07-27T22:41:50.000Z", "4"));

        series3.setMetric(TEST_METRIC_NAME);
        series3.setEntity(TEST_ENTITY3_NAME);
        series3.addData(new Sample("2016-07-27T22:41:52.000Z", "4"));
        series3.addData(new Sample("2016-07-27T22:41:51.000Z", "5"));
        series3.addData(new Sample("2016-07-27T22:41:50.000Z", "6"));

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2, series3));
    }

    /**
     * #3047
     * Test for alias documentation example.
     *
     * @see <a href="Computed Columns">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/order-by-value.md</a>
     */
    @Test
    public void test() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) FROM '%s'%nGROUP BY entity%nORDER BY AVG(value) DESC",
                TEST_METRIC_NAME
        );
        Response response = executeQuery(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY3_NAME, "5.0"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0"),
                Arrays.asList(TEST_ENTITY1_NAME, "1.0")
        );
        assertTableRows(expectedRows, resultTable);
    }
}
