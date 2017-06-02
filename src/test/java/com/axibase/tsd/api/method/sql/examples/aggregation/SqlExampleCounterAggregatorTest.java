package com.axibase.tsd.api.method.sql.examples.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

public class SqlExampleCounterAggregatorTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-counter-aggregator-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.addSamples(
                new Sample("2015-09-30T09:00:05.869Z", new BigDecimal("2.0")),
                new Sample("2015-09-30T09:00:05.860Z", new BigDecimal("3.0")),
                new Sample("2015-09-30T09:00:05.195Z", new BigDecimal("1.0")),
                new Sample("2015-09-30T09:00:06.526Z", new BigDecimal("3.0")),
                new Sample("2015-09-30T09:00:06.858Z", new BigDecimal("3.0")),
                new Sample("2015-09-30T09:00:06.217Z", new BigDecimal("3.0")),
                new Sample("2015-09-30T09:00:06.211Z", new BigDecimal("3.0")),
                new Sample("2015-09-30T09:00:06.321Z", new BigDecimal("3.0"))
        );

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #3047
     * Test for alias documentation example.
     *
     * @see <a href="Counter Aggregator">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/aggregate-counter.md</a>
     */
    @Test
    public void testExample() {
        String sqlQuery = String.format("SELECT datetime, count(value), max(value), first(value), last(value), counter(value), delta(value) %n" +
                "FROM '%s' %n" +
                "WHERE datetime >= '2015-09-30T09:00:05Z' AND datetime < '2015-09-30T09:00:07Z'  %n" +
                "GROUP BY period(1 second)", TEST_METRIC_NAME);

        String[][] expectedRows = {
                {"2015-09-30T09:00:05.000Z", "3", "3.0", "1.0", "2.0", "4.0", "1.0"},
                {"2015-09-30T09:00:06.000Z", "5", "3.0", "3.0", "3.0", "1.0", "1.0"}
        };
        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
