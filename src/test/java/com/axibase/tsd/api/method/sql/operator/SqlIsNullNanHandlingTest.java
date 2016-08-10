package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlIsNullNanHandlingTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-is-null-nan-handling-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void prepareData() throws IOException {
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
    Following tests related to #3077 issue
     */

    /**
     * #Issue #3077
     */
    @Test
    public void testNanExcluding() {
        String sqlQuery =
                "SELECT t1.value + t2.value AS 'sum'  FROM '" + TEST_METRIC1_NAME + "' t1\n" +
                        "JOIN '" + TEST_METRIC2_NAME + "' t2\n" +
                        "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                        "AND t1.value IS NOT NULL AND t2.value IS NOT NULL";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = Collections.singletonList("4.0");

        assertTableContainsColumnValues(expectedColumn, resultTable, "sum");
    }

    /**
     * #Issue #3077
     */
    @Test
    public void testNanIncluding() {
        String sqlQuery =
                "SELECT COUNT(value) AS 'nans'  FROM '" + TEST_METRIC1_NAME + "' t1\n" +
                        "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                        "AND value IS NOT NULL";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = Collections.singletonList("1");

        assertTableContainsColumnValues(expectedColumn, resultTable, "nans");
    }

}
