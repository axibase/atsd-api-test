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
public class SqlAvgValueExampleTest extends SqlTest {
    private final static String TEST_PREFIX = "sql-example-avg-value-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", "11.1"),
                new Sample("2016-06-19T11:15:00.000Z", "11.5")

        );
    }

    /**
     * Issue #3047
     * Test for query all tags documentation example.
     *
     * @see <a href="Average Value Query">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/average-value.md</a>
     */
    @Test
    public void testAvgValueExample() {
        String sqlQuery =
                "SELECT entity, avg(value)\n" +
                        "FROM 'sql-example-avg-value-metric' \n" +
                        "WHERE entity = 'sql-example-avg-value-entity' " +
                        "AND datetime >= '2016-06-19T11:00:00.000Z' AND datetime < '2016-06-19T11:16:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-example-avg-value-entity", "11.3")
        );

        assertTableRows(expectedRows, resultTable);

    }
}