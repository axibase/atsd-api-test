package com.axibase.tsd.api.method.sql.examples.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlExampleAvgValueTest extends SqlTest {
    private final static String TEST_PREFIX = "sql-example-avg-value-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                new Sample("2016-06-19T11:00:00.000Z", new BigDecimal("11.1")),
                new Sample("2016-06-19T11:15:00.000Z", new BigDecimal("11.5"))
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #3047
     * Test for query all tags documentation example.
     *
     * @see <a href="Average Value Query">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/average-value.md</a>
     */
    @Test
    public void testExample1() {
        String sqlQuery =
                String.format(
                        "SELECT avg(value) %n" +
                                "FROM '%s'  %n" +
                                "WHERE entity = '%s'" +
                                "AND datetime >= '2016-06-19T11:00:00.000Z' AND datetime < '2016-06-19T11:16:00.000Z'",
                        TEST_METRIC_NAME, TEST_ENTITY_NAME
                );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("11.3")
        );

        assertTableRowsExist(expectedRows, resultTable);

    }

    /**
     * Issues #3047
     */
    @Test
    public void testExample2() {
        String sqlQuery =
                String.format(
                        "SELECT avg(value), max(value), last(value), count(*) %n" +
                                "FROM '%s'  %n" +
                                "WHERE entity = '%s'" +
                                "AND datetime >= '2016-06-19T11:00:00.000Z' AND datetime < '2016-06-19T11:16:00.000Z'",
                        TEST_METRIC_NAME, TEST_ENTITY_NAME
                );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("11.3", "11.5", "11.5", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);

    }
}