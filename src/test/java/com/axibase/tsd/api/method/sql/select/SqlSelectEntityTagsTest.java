package com.axibase.tsd.api.method.sql.select;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlSelectEntityTagsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-select-entity-tags-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private static final String TEST_ENTITY3_NAME = TEST_PREFIX + "entity-3";


    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_ENTITY1_NAME, TEST_METRIC_NAME);
        sendSamplesToSeries(series,
                new Sample("2016-06-03T09:27:00.000Z", "0")
        );
        updateSeriesEntityTags(series, Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("a", "b");
            put("b", "c");
        }}));
        series.setEntity(TEST_ENTITY2_NAME);
        sendSamplesToSeries(series,
                new Sample("2016-06-03T09:27:01.000Z", "1")
        );
        updateSeriesEntityTags(series, Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("c", "d");
            put("d", "e");
        }}));
        series.setEntity(TEST_ENTITY3_NAME);
        sendSamplesToSeries(series,
                new Sample("2016-06-03T09:27:02.000Z", "2")
        );
    }

    /*
    Following tests related to #3062
     */

    /**
     * Issue #3062
     */
    @Test
    public void testSelectEntityTags() {
        String sqlQuery =
                "SELECT entity, value, entity.tags FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE datetime >= '2016-06-03T09:27:00.000Z' AND datetime < '2016-06-03T09:27:02.001Z'\n" +
                        "AND entity = '" + TEST_ENTITY1_NAME + "'\n" +
                        "ORDER BY datetime";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumn = Arrays.asList("a=b;b=c");

        assertTableContainsColumnValues(expectedColumn, resultTable, "entity.tags");
    }

    /**
     * Issue #3062
     */
    @Test
    public void testSelectEmptyEntityTags() {
        String sqlQuery =
                "SELECT entity, value, entity.tags FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE datetime >= '2016-06-03T09:27:00.000Z' AND datetime < '2016-06-03T09:27:02.001Z'\n" +
                        "AND entity = '" + TEST_ENTITY3_NAME + "'\n" +
                        "ORDER BY datetime";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumn = Arrays.asList("null");

        assertTableContainsColumnValues(expectedColumn, resultTable, "entity.tags");
    }


    /**
     * Issue #3062
     */
    @Test
    public void testSelectTagsWithGroupByEntityTags() {
        String sqlQuery =
                "SELECT entity, COUNT(value), entity.tags FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE datetime >= '2016-06-03T09:27:00.000Z' AND datetime < '2016-06-03T09:27:02.001Z'\n" +
                        "AND entity = '" + TEST_ENTITY3_NAME + "'\n" +
                        "GROUP BY entity, value";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumn = Arrays.asList("null");

        assertTableContainsColumnValues(expectedColumn, resultTable, "entity.tags");
    }
}
