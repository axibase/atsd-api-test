package com.axibase.tsd.api.method.sql.where;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.util.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlEntityTagsTest extends SqlTest {
    private static final String TESTS_PREFIX = "sql-where-entity-tags-support-";


    @BeforeClass
    public static void prepareDate() {
        Series series = new Series(TESTS_PREFIX + "entity", TESTS_PREFIX + "metric");
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.000Z", 3));
        updateSeriesEntityTags(series, Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("tag1", "val1");
            put("tag2", "val2");
            put("tag3", "v3");
        }}));
    }


    /**
     * Following tests related to #2926 issue.
     */


    private static void updateSeriesEntityTags(final Series series, final Map<String, String> newTags) {
        try {
            EntityMethod.updateEntity(new Entity() {{
                setName(series.getEntity());
                setTags(newTags);
            }});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update Entity tags");
        }
    }

    /**
     * Issue #2926
     */
    @Test
    public void testLikeOperator() {
        String sqlQuery =
                "SELECT entity.tags.tag1\n" +
                        "FROM 'sql-where-entity-tags-support-metric'\n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag1 LIKE 'val*'\n" +
                        "AND entity = 'sql-where-entity-tags-support-entity'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("val1")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #2926
     */
    @Test
    public void testNotLikeOperator() {
        String sqlQuery =
                "SELECT entity.tags.tag1\n" +
                        "FROM 'sql-where-entity-tags-support-metric'\n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag1 NOT LIKE 'val*'\n" +
                        "AND entity = 'sql-where-entity-tags-support-entity'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #2926
     */
    @Test
    public void testEqualsOperator() {
        String sqlQuery =
                "SELECT entity.tags.tag1\n" +
                        "FROM 'sql-where-entity-tags-support-metric'\n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag1 ='val1'\n" +
                        "AND entity = 'sql-where-entity-tags-support-entity'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("val1")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #2926
     */
    @Test
    public void testNotEqualsOperator() {
        String sqlQuery =
                "SELECT entity.tags.tag1\n" +
                        "FROM 'sql-where-entity-tags-support-metric'\n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag1 <> 'val2'\n" +
                        "AND entity = 'sql-where-entity-tags-support-entity'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("val1")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #2926
     */
    @Test
    public void testIsNullOperator() {
        String sqlQuery =
                "SELECT entity.tags.tag4\n" +
                        "FROM 'sql-where-entity-tags-support-metric'\n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag4 IS NULL\n" +
                        "AND entity = 'sql-where-entity-tags-support-entity'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("null")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #2926
     */
    @Test
    public void testIsNotNullOperator() {
        String sqlQuery =
                "SELECT entity.tags.tag4\n" +
                        "FROM 'sql-where-entity-tags-support-metric'\n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag4 IS NOT NULL\n" +
                        "AND entity = 'sql-where-entity-tags-support-entity'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList();

        assertTableRows(expectedRows, resultTable);
    }
}
