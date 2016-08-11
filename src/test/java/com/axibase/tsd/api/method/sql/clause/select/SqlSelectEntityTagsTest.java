package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
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
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Entity.register(TEST_ENTITY3_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);

        List<Series> seriesList = Arrays.asList(
                new Series() {{
                    setMetric(TEST_METRIC_NAME);
                    setEntity(TEST_ENTITY1_NAME);
                    addData(new Sample("2016-06-03T09:27:00.000Z", "0"));
                }},
                new Series() {{
                    setMetric(TEST_METRIC_NAME);
                    setEntity(TEST_ENTITY2_NAME);
                    addData(new Sample("2016-06-03T09:27:01.000Z", "1"));
                }},
                new Series() {{
                    setMetric(TEST_METRIC_NAME);
                    setEntity(TEST_ENTITY3_NAME);
                    addData(new Sample("2016-06-03T09:27:01.000Z", "2"));
                }}
        );
        SeriesMethod.insertSeriesCheck(seriesList);


        EntityMethod.updateEntity(TEST_ENTITY1_NAME, new Entity() {{
            addTag("a", "b");
            addTag("b", "c");
        }});
        EntityMethod.updateEntity(TEST_ENTITY2_NAME, new Entity() {{
            addTag("c", "d");
            addTag("d", "e");
        }});
    }


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

        List<String> expectedColumn = Collections.singletonList("a=b;b=c");

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

        List<String> expectedColumn = Collections.singletonList("null");

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

        List<String> expectedColumn = Collections.singletonList("null");

        assertTableContainsColumnValues(expectedColumn, resultTable, "entity.tags");
    }
}
