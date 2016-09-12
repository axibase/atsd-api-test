package com.axibase.tsd.api.method.sql.examples.aggregation;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;


public class SqlExampleSlidingWindowsTest extends SqlTest {
    private final static String TEST_PREFIX = "sql-example-sliding-windows-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);
        Series series1 = new Series(),
                series2 = new Series(),
                series3 = new Series(),
                series4 = new Series();

        series1.setEntity(TEST_ENTITY1_NAME);
        series1.setMetric(TEST_METRIC_NAME);
        series1.addTag("a", "b");
        series1.addData(new Sample("2016-06-19T11:00:00.000Z", "1"));
        series1.addData(new Sample("2016-06-19T11:00:01.000Z", "2"));

        series2.setEntity(TEST_ENTITY1_NAME);
        series2.setMetric(TEST_METRIC_NAME);
        series2.addTag("b", "c");
        series2.addData(new Sample("2016-06-19T11:00:03.000Z", "3"));

        series3.setEntity(TEST_ENTITY2_NAME);
        series3.setMetric(TEST_METRIC_NAME);
        series3.addTag("a", "b");
        series3.addData(new Sample("2016-06-19T11:00:04.000Z", "4"));

        series4.setEntity(TEST_ENTITY2_NAME);
        series4.setMetric(TEST_METRIC_NAME);
        series4.addTag("b", "c");
        series4.addData(new Sample("2016-06-19T11:00:05.000Z", "5"));

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2, series3, series4));
    }

    /**
     * #3047
     * Test for query all tags documentation example.
     *
     * @see <a href="Aggregate - Sliding Window">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/aggregate-sliding-window.md</a>
     */
    @Test
    public void testExample1() {
        String sqlQuery = String.format(
                "SELECT entity, avg(value), max(value), last(value), count(*) %n" +
                        "FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' AND datetime < '2016-06-19T11:00:06.000Z'  %n" +
                        "GROUP BY entity",
                TEST_METRIC_NAME);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "3.0", "3.0", "3"),
                Arrays.asList(TEST_ENTITY2_NAME, "4.5", "5.0", "5.0", "2")

        );

        assertTableRowsExist(expectedRows, resultTable);

    }


    /**
     * #3047
     */
    @Test
    public void testExample2() {
        String sqlQuery = String.format(
                "SELECT entity,tags, avg(value), max(value), last(value), count(*) %n" +
                        "FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' AND datetime < '2016-06-19T11:00:06.000Z'  %n" +
                        "GROUP BY entity, tags",
                TEST_METRIC_NAME);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "a=b", "1.5", "2.0", "2.0", "2"),
                Arrays.asList(TEST_ENTITY2_NAME, "a=b", "4.0", "4.0", "4.0", "1"),
                Arrays.asList(TEST_ENTITY1_NAME, "b=c", "3.0", "3.0", "3.0", "1"),
                Arrays.asList(TEST_ENTITY2_NAME, "b=c", "5.0", "5.0", "5.0", "1")

        );

        assertTableRowsExist(expectedRows, resultTable);

    }
}
