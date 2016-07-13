package com.axibase.tsd.api.method.sql.examples.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlExamplePercentilesTest extends SqlTest {
    private final static String TEST_PREFIX = "sql-example-percentiles-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:00.000Z", "11.1212"),
                new Sample("2016-06-19T11:01:00.000Z", "11.3232"),
                new Sample("2016-06-19T11:02:00.000Z", "11.123"),
                new Sample("2016-06-19T11:03:00.000Z", "11.4343"),
                new Sample("2016-06-19T11:04:00.000Z", "11.435"),
                new Sample("2016-06-19T11:05:00.000Z", "11.33"),
                new Sample("2016-06-19T11:06:00.000Z", "11.322"),
                new Sample("2016-06-19T11:07:00.000Z", "11.3232")
                )
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * Issue #3047
     * Test for alias documentation example.
     *
     * @see <a href="Aggregate Percentiles">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/aggregate-percentiles.md</a>
     */
    @Test
    public void testExample() {
        String sqlQuery = String.format(
                "SELECT percentile(25, value) AS \"p25\",\n" +
                        "  percentile(50, value) AS \"p50\",\n" +
                        "  median(value),\n" +
                        "  percentile(75, value) AS \"p75\",\n" +
                        "  percentile(90, value) AS \"p90\",\n" +
                        "  percentile(95, value) AS \"p95\",\n" +
                        "  percentile(97.5, value) AS \"p97.5\",\n" +
                        "  percentile(99, value) AS \"p99\",\n" +
                        "  percentile(99.5, value) AS \"p99.5\",\n" +
                        "  percentile(99.9, value) AS \"p99.9\",\n" +
                        "  percentile(99.99, value) AS \"p99.99\"\n" +
                        "  FROM '%s'\n" +
                        "WHERE entity = '%s' \n" +
                        " AND datetime >=\"2016-06-19T11:00:00.000Z\" AND datetime < \"2016-06-19T11:08:00.000Z\"",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("11.172749999999999", "11.3232", "11.3232", "11.408225", "11.435", "11.435", "11.435", "11.435", "11.435", "11.435", "11.435")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
