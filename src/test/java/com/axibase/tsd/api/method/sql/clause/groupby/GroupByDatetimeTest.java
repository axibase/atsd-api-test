package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GroupByDatetimeTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-group-by-datetime-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TESTS_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TESTS_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private static final String TESTS_ENTITY3_NAME = TEST_PREFIX + "entity-3";


    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        seriesList.add(
                new Series(TESTS_ENTITY1_NAME, TEST_METRIC_NAME) {{
                    addSamples(
                            new Sample("2016-06-19T11:00:00.500Z", 0),
                            new Sample("2016-06-19T11:00:01.500Z", 1),
                            new Sample("2016-06-19T11:00:02.500Z", 2)
                    );
                }}
        );

        seriesList.add(
                new Series(TESTS_ENTITY2_NAME, TEST_METRIC_NAME) {{
                    addSamples(
                            new Sample("2016-06-19T11:00:00.500Z", 0),
                            new Sample("2016-06-19T11:00:01.500Z", 1)
                    );
                }}
        );

        seriesList.add(
                new Series(TESTS_ENTITY3_NAME, TEST_METRIC_NAME) {{
                    addSamples(
                            new Sample("2016-06-19T11:00:00.500Z", 0)
                    );
                }}
        );

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3102
     */
    @Test
    public void testGroupByDatetimeSyntax() {
        String sqlQuery = String.format(
                "SELECT datetime , entity, value FROM \"%s\" %nGROUP BY datetime, entity, value",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", TESTS_ENTITY1_NAME, "0.0"),
                Arrays.asList("2016-06-19T11:00:00.500Z", TESTS_ENTITY2_NAME, "0.0"),
                Arrays.asList("2016-06-19T11:00:00.500Z", TESTS_ENTITY3_NAME, "0.0"),
                Arrays.asList("2016-06-19T11:00:01.500Z", TESTS_ENTITY1_NAME, "1.0"),
                Arrays.asList("2016-06-19T11:00:01.500Z", TESTS_ENTITY2_NAME, "1.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", TESTS_ENTITY1_NAME, "2.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3102
     */
    @Test
    public void testGroupByDatetimeWithAggregateFunction() {
        String sqlQuery = String.format(
                "SELECT datetime, COUNT(value) FROM \"%s\" %nGROUP BY datetime, value",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", "3"),
                Arrays.asList("2016-06-19T11:00:01.500Z", "2"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }
}
