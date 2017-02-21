package com.axibase.tsd.api.method.sql.examples.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;


public class SqlExampleAggregateMaxValueTimeTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-aggregate-max-value-time-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);

        Series series1 = new Series(),
                series2 = new Series();

        series1.setMetric(TEST_METRIC_NAME);
        series1.setEntity(TEST_ENTITY1_NAME);
        series1.addData(new Sample("2016-06-17T19:16:01.000Z", "1"));
        series1.addData(new Sample("2016-06-17T19:16:02.000Z", "2"));

        series2.setMetric(TEST_METRIC_NAME);
        series2.setEntity(TEST_ENTITY2_NAME);
        series2.addData(new Sample("2016-06-17T19:16:03.000Z", "3"));
        series2.addData(new Sample("2016-06-17T19:16:04.000Z", "4"));

        List<Series> seriesList = Arrays.asList(series1, series2);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3047
     */
    @Test
    public void testExample() {
        String sqlQuery = String.format(
                "SELECT entity, MAX(value), date_format(MAX_VALUE_TIME(value), 'yyyy-MM-dd HH:mm:ss') AS \"Max Time\" %n" +
                        "FROM '%s' %n" +
                        "WHERE datetime BETWEEN '2016-06-17T19:16:01.000Z' AND '2016-06-17T19:16:05.000Z'  %n" +
                        "GROUP BY entity", TEST_METRIC_NAME);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(
                        TEST_ENTITY1_NAME,
                        "2.0",
                        TestUtil.formatDate(
                                TestUtil.parseDate("2016-06-17T19:16:02.000Z"),
                                "yyyy-MM-dd HH:mm:ss"
                        )
                ),
                Arrays.asList(
                        TEST_ENTITY2_NAME,
                        "4.0",
                        TestUtil.formatDate(
                                TestUtil.parseDate("2016-06-17T19:16:04.000Z"),
                                "yyyy-MM-dd HH:mm:ss"
                        )
                )

        );

        assertTableRowsExist(expectedRows, resultTable);
    }
}
