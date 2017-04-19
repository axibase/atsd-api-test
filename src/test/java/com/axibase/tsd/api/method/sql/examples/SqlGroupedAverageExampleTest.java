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
public class SqlGroupedAverageExampleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-grouped-average-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        sendSamplesToSeries(series,
                new Sample(1446037200000L, "76.94454545454546"),
                new Sample(1446037500000L, "24.44388888888889"),
                new Sample(1446037800000L, "19.04421052631579"),
                new Sample(1446038100000L, "17.453157894736844"),
                new Sample(1446038400000L, "17.373157894736842"),
                new Sample(1446038700000L, "19.232222222222223"),
                new Sample(1446039000000L, "35.061052631578946"),
                new Sample(1446039300000L, "28.737894736842104"),
                new Sample(1446039600000L, "24.24578947368421"),
                new Sample(1446039900000L, "15.909444444444444"),
                new Sample(1446040200000L, "16.4")
        );
    }

    /**
     * Issue #3047
     * Test for Grouped Average  documentation example.
     *
     * @see <a href="Grouped Average">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/grouped-average.md</a>
     */
    @Test
    public void testGroupedAverage() {
        String sqlQuery =
                "SELECT entity, period(5 MINUTE) AS \"period\", avg(value)\n" +
                        "  FROM 'sql-example-grouped-average-metric' \n" +
                        "WHERE entity IN ('sql-example-grouped-average-entity') \n" +
                        "  AND time >= 1446037200000\n" +
                        "  GROUP BY entity, period(5 MINUTE)";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_PREFIX + "entity", "1446037200000", "76.94454545454546"),
                Arrays.asList(TEST_PREFIX + "entity", "1446037500000", "24.44388888888889"),
                Arrays.asList(TEST_PREFIX + "entity", "1446037800000", "19.04421052631579"),
                Arrays.asList(TEST_PREFIX + "entity", "1446038100000", "17.453157894736844"),
                Arrays.asList(TEST_PREFIX + "entity", "1446038400000", "17.373157894736842"),
                Arrays.asList(TEST_PREFIX + "entity", "1446038700000", "19.232222222222223"),
                Arrays.asList(TEST_PREFIX + "entity", "1446039000000", "35.061052631578946"),
                Arrays.asList(TEST_PREFIX + "entity", "1446039300000", "28.737894736842104"),
                Arrays.asList(TEST_PREFIX + "entity", "1446039600000", "24.24578947368421"),
                Arrays.asList(TEST_PREFIX + "entity", "1446039900000", "15.909444444444444"),
                Arrays.asList(TEST_PREFIX + "entity", "1446040200000", "16.4")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
