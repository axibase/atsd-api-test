package com.axibase.tsd.api.method.sql.examples.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlExamplePerPeriodTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-period-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.addSamples(
                Sample.ofDateInteger("2015-09-30T09:00:05.869Z", 2),
                Sample.ofDateInteger("2015-09-30T09:00:05.860Z", 3),
                Sample.ofDateInteger("2015-09-30T09:00:05.195Z", 1),
                Sample.ofDateInteger("2015-09-30T09:00:06.526Z", 3),
                Sample.ofDateInteger("2015-09-30T09:00:06.858Z", 3),
                Sample.ofDateInteger("2015-09-30T09:00:06.217Z", 3),
                Sample.ofDateInteger("2015-09-30T09:00:06.211Z", 3),
                Sample.ofDateInteger("2015-09-30T09:00:06.321Z", 3)
        );

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }


    /**
     * #3047
     * Test for alias documentation example.
     *
     * @see <a href="Aggregate Per Period">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/aggregate-period.md</a>
     */
    @Test
    public void testExample() {
        String sqlQuery = String.format("SELECT datetime, avg(value), max(value), last(value), count(*) %n" +
                "FROM \"%s\" %n" +
                "WHERE datetime >= '2015-09-30T09:00:05Z' AND datetime < '2015-09-30T09:00:07Z'  %n" +
                "GROUP BY period(1 second)", TEST_METRIC_NAME);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2015-09-30T09:00:05.000Z", "2.0", "3.0", "2.0", "3"),
                Arrays.asList("2015-09-30T09:00:06.000Z", "3.0", "3.0", "3.0", "5")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }
}
