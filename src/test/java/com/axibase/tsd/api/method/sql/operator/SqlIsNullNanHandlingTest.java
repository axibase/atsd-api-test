package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlIsNullNanHandlingTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-is-null-nan-handling-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Metric.register(TEST_METRIC1_NAME);
        Registry.Metric.register(TEST_METRIC2_NAME);
        Registry.Entity.register(TEST_ENTITY_NAME);

        List<Series> seriesList = new ArrayList<>();

        seriesList.add(
                new Series() {{
                    setEntity(TEST_ENTITY_NAME);
                    setMetric(TEST_METRIC1_NAME);
                    setData(Arrays.asList(
                            new Sample("2016-06-29T08:00:00.000Z", (BigDecimal) null),
                            new Sample("2016-06-29T08:00:01.000Z", 3)
                    ));
                }}
        );

        seriesList.add(
                new Series() {{
                    setMetric(TEST_METRIC2_NAME);
                    setEntity(TEST_ENTITY_NAME);
                    setData(Arrays.asList(
                            new Sample("2016-06-29T08:00:00.000Z", 0),
                            new Sample("2016-06-29T08:00:01.000Z", 1)
                    ));
                }}
        );
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /*
    #3077 issue
     */

    /**
     * ##3077
     */
    @Test
    public void testNanExcluding() {
        String sqlQuery = String.format(
                "SELECT t1.value + t2.value AS 'sum'  FROM '%s' t1 %n" +
                        "JOIN '%s' t2 %n" +
                        "WHERE t1.value IS NOT NULL AND t2.value IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
            Collections.singletonList("4")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * ##3077
     */
    @Test
    public void testNanIncluding() {
        String sqlQuery = String.format(
                "SELECT COUNT(value) AS 'nans'  FROM '%s' t1 %nWHERE entity = '%s' %n" +
                        "AND value IS NOT NULL",
                TEST_METRIC1_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = Collections.singletonList("1");

        assertTableContainsColumnValues(expectedColumn, resultTable, "nans");
    }

}
