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
public class SqlGroupByWithOrderByExampleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-group-by-with-order-by-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity-1", TEST_PREFIX + "metric");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", "46.547288888888865")
        );
        series.setEntity(TEST_PREFIX + "entity-2");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", "21.85551111111111")
        );

        series.setEntity(TEST_PREFIX + "entity-3");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", "13.554488888888887")
        );

        series.setEntity(TEST_PREFIX + "entity-4");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", "7.88160714285714")
        );

        series.setEntity(TEST_PREFIX + "entity-5");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", "2.8021973094170405")
        );
    }

    /**
     * Issue #3047
     * Test for GROUP BY Query with ORDER BY documentation example.
     *
     * @see <a href="GROUP BY Query with ORDER BY">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/group-by-query-with-order-by.md</a>
     */
    @Test
    public void testGroupByWithOrderBy() {
        String sqlQuery =
                "SELECT entity, avg(value) \n" +
                        "FROM 'sql-example-group-by-with-order-by-metric' \n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' \n" +
                        "GROUP BY entity \n" +
                        "ORDER BY avg(value) DESC";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_PREFIX + "entity-1", "46.547288888888865"),
                Arrays.asList(TEST_PREFIX + "entity-2", "21.85551111111111"),
                Arrays.asList(TEST_PREFIX + "entity-3", "13.554488888888887"),
                Arrays.asList(TEST_PREFIX + "entity-4", "7.88160714285714"),
                Arrays.asList(TEST_PREFIX + "entity-5", "2.8021973094170405")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
