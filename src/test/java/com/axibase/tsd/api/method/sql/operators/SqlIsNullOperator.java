package com.axibase.tsd.api.method.sql.operators;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlIsNullOperator extends SqlTest {
    private final static String TEST_PREFIX = "sql-operator-is-null-";


    @BeforeClass
    public static void prepareData() {
        final Series series = new Series(TEST_PREFIX + "entity-1", TEST_PREFIX + "metric") {{
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("tag1", "val1");
            }}));
        }};

        //Update series' entity
        updateSeriesEntityTags(
                series,
                Collections.unmodifiableMap(new HashMap<String, String>() {{
                    put("tag1", "val1");
                }})
        );

        sendSamplesToSeries(series, new Sample("2016-06-19T11:00:00.000Z", 1));
        series.setEntity(TEST_PREFIX + "entity-2");
        series.setTags(Collections.
                unmodifiableMap(new HashMap<String, String>() {{
                                    put("tag1", "val2");
                                    put("tag2", "val2");
                                }}
                ));
        sendSamplesToSeries(series, new Sample("2016-06-19T11:05:00.000Z", 2));
        series.setEntity(TEST_PREFIX + "entity-3");
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("tag2", "val3");
            }
        }));
        sendSamplesToSeries(series, new Sample("2016-06-19T11:10:00.000Z", 3));
        series.setEntity(TEST_PREFIX + "entity-4");
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("tag4", "val4");
            }
        }));
        sendSamplesToSeries(series, new Sample("2016-06-19T11:15:00.000Z", 4));
    }

    /**
     * Following tests related to #2937 issue.
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
     * Issue #2937
     */
    @Test
    public void testIsNullMetricTags() {
        String sqlQuery =
                "SELECT entity, datetime, value, tags.*\n" +
                        "FROM 'sql-operator-is-null-metric'\n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:11:00.000Z'\n" +
                        "AND tags.tag4 IS NULL\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);


        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-operator-is-null-entity-1", "2016-06-19T11:00:00.000Z", "1", "null", "null", "val1"),
                Arrays.asList("sql-operator-is-null-entity-2", "2016-06-19T11:05:00.000Z", "2", "null", "val2", "val2"),
                Arrays.asList("sql-operator-is-null-entity-3", "2016-06-19T11:10:00.000Z", "3", "null", "val3", "null")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #2937
     */
    @Test
    public void testNotIsNullMetricTags() {
        String sqlQuery =
                "SELECT entity, datetime, value, tags.*\n" +
                        "FROM 'sql-operator-is-null-metric'\n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z'\n" +
                        "AND NOT tags.tag4 IS NULL\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-operator-is-null-entity-4", "2016-06-19T11:15:00.000Z", "4", "val4", "null", "null")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #2937
     */
    @Test
    public void testIsNotNullMetricTags() {
        String sqlQuery =
                "SELECT entity, datetime, value, tags.*\n" +
                        "FROM 'sql-operator-is-null-metric'\n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z'\n" +
                        "AND tags.tag4 IS NOT NULL\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-operator-is-null-entity-4", "2016-06-19T11:15:00.000Z", "4", "val4", "null", "null")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #2937
     */
    @Test
    public void testNotIsNotNullMetricTags() {
        String sqlQuery =
                "SELECT entity, datetime, value, tags.*\n" +
                        "FROM 'sql-operator-is-null-metric'\n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z'\n" +
                        "AND NOT tags.tag4 IS NOT NULL\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-operator-is-null-entity-1", "2016-06-19T11:00:00.000Z", "1", "null", "null", "val1"),
                Arrays.asList("sql-operator-is-null-entity-2", "2016-06-19T11:05:00.000Z", "2", "null", "val2", "val2"),
                Arrays.asList("sql-operator-is-null-entity-3", "2016-06-19T11:10:00.000Z", "3", "null", "val3", "null")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #2937
     */
    @Test
    public void testIsNotNullEntityTags() throws Exception {
        String sqlQuery =
                "SELECT entity, datetime, value, tags.*\n" +
                        "FROM 'sql-operator-is-null-metric'\n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z'\n" +
                        "AND entity.tags.tag1 IS NOT NULL\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-operator-is-null-entity-1", "2016-06-19T11:00:00.000Z", "1", "null", "null", "val1")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #2937
     */
    @Test
    public void testIsNullEntityTags() throws Exception {
        String sqlQuery =
                "SELECT entity, datetime, value, tags.*\n" +
                        "FROM 'sql-operator-is-null-metric'\n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:11:00.000Z'\n" +
                        "AND entity.tags.tag1 IS NULL\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-operator-is-null-entity-2", "2016-06-19T11:05:00.000Z", "2", "null", "val2", "val2"),
                Arrays.asList("sql-operator-is-null-entity-3", "2016-06-19T11:10:00.000Z", "3", "null", "val3", "null")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
