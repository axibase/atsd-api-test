package com.axibase.tsd.api.method.sql.groupby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlGroupByDatetime extends SqlTest {
    private static final String TEST_PREFIX = "sql-group-by-datetime-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TESTS_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TESTS_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private static final String TESTS_ENTITY3_NAME = TEST_PREFIX + "entity-3";


    @BeforeClass
    public static void prepareData() throws IOException {


        List<Series> seriesList = new ArrayList<>();
        seriesList.add(
                new Series() {{
                    setEntity(TESTS_ENTITY1_NAME);
                    setMetric(TEST_METRIC_NAME);
                    setData(Arrays.asList(
                            new Sample("2016-06-19T11:00:00.500Z", "0"),
                            new Sample("2016-06-19T11:00:01.500Z", "1"),
                            new Sample("2016-06-19T11:00:02.500Z", "2")
                    ));
                }}
        );

        seriesList.add(
                new Series() {{
                    setEntity(TESTS_ENTITY2_NAME);
                    setMetric(TEST_METRIC_NAME);
                    setData(Arrays.asList(
                            new Sample("2016-06-19T11:00:00.500Z", "0"),
                            new Sample("2016-06-19T11:00:01.500Z", "1")
                    ));
                }}
        );

        seriesList.add(
                new Series() {{
                    setEntity(TESTS_ENTITY3_NAME);
                    setMetric(TEST_METRIC_NAME);
                    setData(Collections.singletonList(
                            new Sample("2016-06-19T11:00:00.500Z", "0")
                    ));
                }}
        );

        SeriesMethod.insertSeriesCheck(seriesList);
    }
    /*
    Following tests related to #3102 issue
     */

    /**
     * Issue #3102
     */
    @Test
    public void testGroupByDatetimeSyntax() {
        String sqlQuery =
                "SELECT datetime , entity, value FROM '" + TEST_METRIC_NAME + "'\n" +
                        "GROUP BY datetime, entity, value";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", TESTS_ENTITY1_NAME, "0.0"),
                Arrays.asList("2016-06-19T11:00:00.500Z", TESTS_ENTITY2_NAME, "0.0"),
                Arrays.asList("2016-06-19T11:00:00.500Z", TESTS_ENTITY3_NAME, "0.0"),
                Arrays.asList("2016-06-19T11:00:01.500Z", TESTS_ENTITY1_NAME, "1.0"),
                Arrays.asList("2016-06-19T11:00:01.500Z", TESTS_ENTITY2_NAME, "1.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", TESTS_ENTITY1_NAME, "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3102
     */
    @Test
    public void testGroupByDatetimeWithAggregateFunction() {
        String sqlQuery =
                "SELECT datetime, COUNT(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                        "GROUP BY datetime,value";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", "3"),
                Arrays.asList("2016-06-19T11:00:01.500Z", "2"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "1")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
