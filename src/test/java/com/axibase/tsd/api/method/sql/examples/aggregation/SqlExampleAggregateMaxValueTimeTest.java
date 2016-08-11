package com.axibase.tsd.api.method.sql.examples.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlExampleAggregateMaxValueTimeTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-aggregate-max-value-time-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public void prepareData() throws IOException {
        List<Series> seriesList = Arrays.asList(
                new Series() {{
                    setMetric(TEST_METRIC_NAME);
                    setEntity(TEST_ENTITY1_NAME);
                    addData(new Sample("2016-06-17T19:16:01.000Z", "1"));
                    addData(new Sample("2016-06-17T19:16:02.000Z", "2"));
                }},
                new Series() {{
                    setMetric(TEST_METRIC_NAME);
                    setEntity(TEST_ENTITY2_NAME);
                    addData(new Sample("2016-06-17T19:16:03.000Z", "3"));
                    addData(new Sample("2016-06-17T19:16:04.000Z", "4"));
                }}
        );
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * Issue #3047
     */
    @Test
    void testExample() {
        String sqlQuery = String.format(
                "SELECT entity, MAX(value), date_format(MAX_VALUE_TIME(value), 'yyyy-MM-dd HH:mm:ss') AS \"Max Time\"\n" +
                        "FROM '%s'\n" +
                        "WHERE datetime BETWEEN '2016-06-17T19:16:01.000Z' AND '2016-06-17T19:16:05.000Z' \n" +
                        "GROUP BY entity", TEST_METRIC_NAME);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-17 19:16:01"),
                Arrays.asList(TEST_ENTITY2_NAME, "4.0", "2016-06-17 19:16:04")
        );
    }
}
