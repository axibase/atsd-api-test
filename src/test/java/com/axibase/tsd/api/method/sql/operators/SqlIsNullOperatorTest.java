package com.axibase.tsd.api.method.sql.operators;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
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
public class SqlIsNullOperatorTest extends SqlTest {
    private final static String TEST_PREFIX = "sql-operator-is-null-";
    private final static String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private final static String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private final static String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private final static String TEST_ENTITY3_NAME = TEST_PREFIX + "entity-3";
    private final static String TEST_ENTITY4_NAME = TEST_PREFIX + "entity-4";

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        seriesList.add(new Series() {{
            setMetric(TEST_METRIC_NAME);
            setEntity(TEST_ENTITY1_NAME);
            addData(new Sample("2016-06-19T11:00:00.000Z", 1));
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("tag1", "val1");
            }}));
        }});


        seriesList.add(new Series() {{
            setMetric(TEST_METRIC_NAME);
            setEntity(TEST_ENTITY2_NAME);
            addData(new Sample("2016-06-19T11:05:00.000Z", 2));
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("tag1", "val2");
                put("tag2", "val2");
            }}));
        }});

        seriesList.add(new Series() {{
            setMetric(TEST_METRIC_NAME);
            setEntity(TEST_ENTITY3_NAME);
            addData(new Sample("2016-06-19T11:10:00.000Z", 3));
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("tag2", "val3");
            }}));
        }});

        seriesList.add(new Series() {{
            setMetric(TEST_METRIC_NAME);
            setEntity(TEST_ENTITY4_NAME);
            addData(new Sample("2016-06-19T11:15:00.000Z", 4));
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("tag4", "val4");
            }}));
        }});

        SeriesMethod.insertSeriesCheck(seriesList);

        EntityMethod.updateEntity(TEST_ENTITY1_NAME, new Entity() {{
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                        put("tag1", "val1");
                    }})
            );
        }});
    }

    /*
      Following tests related to #2937 issue.
     */

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

        List<List<String>> expectedRows = Collections.singletonList(
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

        List<List<String>> expectedRows = Collections.singletonList(
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
