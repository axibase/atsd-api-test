package com.axibase.tsd.api.method.sql.examples;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlCounterAggregatorExampleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-counter-aggregator-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        series.addTag("level", "ERROR");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", "11.1"),
                new Sample("2016-06-19T11:15:00.000Z", "11.5"),
                new Sample("2015-09-30T09:00:05.869Z", "2.0"),
                new Sample("2015-09-30T09:00:17.860Z", "3.0"),
                new Sample("2015-09-30T09:00:28.195Z", "3.0"),
                new Sample("2015-09-30T09:00:33.526Z", "3.0"),
                new Sample("2015-09-30T09:00:38.858Z", "3.0"),
                new Sample("2015-09-30T09:05:32.217Z", "3.0"),
                new Sample("2015-09-30T09:06:00.211Z", "3.0"),
                new Sample("2015-09-30T09:07:00.321Z", "3.0"),
                new Sample("2015-09-30T09:08:00.353Z", "3.0"),
                new Sample("2015-09-30T09:10:36.214Z", "3.0"),
                new Sample("2015-09-30T09:11:36.503Z", "3.0"),
                new Sample("2015-09-30T09:12:36.836Z", "3.0"),
                new Sample("2015-09-30T09:13:36.901Z", "3.0"),
                new Sample("2015-09-30T09:15:01.917Z", "2.0"),
                new Sample("2015-09-30T09:15:30.948Z", "3.0"),
                new Sample("2015-09-30T09:15:36.279Z", "3.0"),
                new Sample("2015-09-30T09:16:36.369Z", "3.0"),
                new Sample("2015-09-30T09:17:36.454Z", "3.0"),
                new Sample("2015-09-30T09:19:36.559Z", "5.0"),
                new Sample("2015-09-30T09:20:05.540Z", "7.0"),
                new Sample("2015-09-30T09:20:10.547Z", "8.0"),
                new Sample("2015-09-30T09:20:15.565Z", "8.0"),
                new Sample("2015-09-30T09:20:20.571Z", "8.0"),
                new Sample("2015-09-30T09:20:25.578Z", "8.0"),
                new Sample("2015-09-30T09:25:32.833Z", "3.0"),
                new Sample("2015-09-30T09:26:03.818Z", "3.0"),
                new Sample("2015-09-30T09:27:04.143Z", "3.0"),
                new Sample("2015-09-30T09:28:04.438Z", "3.0"),
                new Sample("2015-09-30T09:30:13.153Z", "3.0"),
                new Sample("2015-09-30T09:30:34.830Z", "3.0"),
                new Sample("2015-09-30T09:31:34.965Z", "3.0"),
                new Sample("2015-09-30T09:32:35.065Z", "3.0"),
                new Sample("2015-09-30T09:35:32.089Z", "3.0"),
                new Sample("2015-09-30T09:36:00.095Z", "3.0"),
                new Sample("2015-09-30T09:37:00.125Z", "3.0"),
                new Sample("2015-09-30T09:38:00.437Z", "3.0"),
                new Sample("2015-09-30T09:40:06.418Z", "3.0"),
                new Sample("2015-09-30T09:40:11.748Z", "3.0"),
                new Sample("2015-09-30T09:40:16.778Z", "3.0"),
                new Sample("2015-09-30T09:40:22.108Z", "3.0"),
                new Sample("2015-09-30T09:43:35.007Z", "93.0"),
                new Sample("2015-09-30T09:43:40.023Z", "453.0"),
                new Sample("2015-09-30T09:43:45.044Z", "903.0"),
                new Sample("2015-09-30T09:43:50.375Z", "1399.0"),
                new Sample("2015-09-30T09:43:55.707Z", "1803.0"),
                new Sample("2015-09-30T09:44:55.744Z", "1803.0"),
                new Sample("2015-09-30T09:45:01.407Z", "1803.0"),
                new Sample("2015-09-30T09:45:06.740Z", "1806.0"),
                new Sample("2015-09-30T09:45:34.740Z", "1806.0"),
                new Sample("2015-09-30T09:46:35.064Z", "1806.0"),
                new Sample("2015-09-30T09:47:35.398Z", "1806.0"),
                new Sample("2015-09-30T09:50:33.322Z", "3.0"),
                new Sample("2015-09-30T09:51:03.995Z", "3.0"),
                new Sample("2015-09-30T09:52:04.000Z", "3.0"),
                new Sample("2015-09-30T09:53:04.009Z", "3.0"),
                new Sample("2015-09-30T09:55:04.351Z", "2.0"),
                new Sample("2015-09-30T09:55:34.683Z", "3.0"),
                new Sample("2015-09-30T09:56:34.718Z", "3.0"),
                new Sample("2015-09-30T09:57:35.040Z", "3.0"),
                new Sample("2015-09-30T09:58:35.257Z", "3.0")
        );
    }

    /**
     * Issue #3047
     * Test for counter aggregator documentation example.
     *
     * @see <a href="Counter Aggregator">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/counter-aggregator.md</a>
     */
    @Test
    public void testCounterAggregator() {
        String sqlQuery =
                "SELECT datetime, count(value), max(value), first(value), last(value), counter(value), delta(value)\n" +
                        "FROM 'sql-example-counter-aggregator-metric' \n" +
                        "WHERE entity = 'sql-example-counter-aggregator-entity' AND tags.level = 'ERROR' \n" +
                        "AND datetime >= '2015-09-30T09:00:00Z' AND datetime < '2015-09-30T10:00:00Z' \n" +
                        "GROUP BY period(5 minute)";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2015-09-30T09:00:00.000Z", "5", "3.0", "2.0", "3.0", "1.0", "1.0"),
                Arrays.asList("2015-09-30T09:05:00.000Z", "4", "3.0", "3.0", "3.0", "0.0", "0.0"),
                Arrays.asList("2015-09-30T09:10:00.000Z", "4", "3.0", "3.0", "3.0", "0.0", "0.0"),
                Arrays.asList("2015-09-30T09:15:00.000Z", "6", "5.0", "2.0", "5.0", "5.0", "2.0"),
                Arrays.asList("2015-09-30T09:20:00.000Z", "5", "8.0", "7.0", "8.0", "3.0", "3.0"),
                Arrays.asList("2015-09-30T09:25:00.000Z", "4", "3.0", "3.0", "3.0", "3.0", "-5.0"),
                Arrays.asList("2015-09-30T09:30:00.000Z", "4", "3.0", "3.0", "3.0", "0.0", "0.0"),
                Arrays.asList("2015-09-30T09:35:00.000Z", "4", "3.0", "3.0", "3.0", "0.0", "0.0"),
                Arrays.asList("2015-09-30T09:40:00.000Z", "10", "1803.0", "3.0", "1803.0", "1800.0", "1800.0"),
                Arrays.asList("2015-09-30T09:45:00.000Z", "5", "1806.0", "1803.0", "1806.0", "3.0", "3.0"),
                Arrays.asList("2015-09-30T09:50:00.000Z", "4", "3.0", "3.0", "3.0", "3.0", "-1803.0"),
                Arrays.asList("2015-09-30T09:55:00.000Z", "5", "3.0", "2.0", "3.0", "3.0", "0.0")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
