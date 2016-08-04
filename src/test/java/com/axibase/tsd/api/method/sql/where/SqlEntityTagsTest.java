package com.axibase.tsd.api.method.sql.where;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlEntityTagsTest extends SqlTest {
    private static final String TESTS_PREFIX = "sql-where-entity-tags-support-";
    private static final String TEST_ENTITY_NAME = TESTS_PREFIX + "entity";


    @BeforeClass
    public static void prepareDate() throws Exception {
        Series series = new Series(TESTS_PREFIX + "entity", TESTS_PREFIX + "metric");
        series.addData(new Sample("2016-06-19T11:00:00.000Z", 3));
        SeriesMethod.insertSeriesCheck(series);
        EntityMethod.updateEntity(TEST_ENTITY_NAME, new Entity() {{
            setTags(
                    Collections.unmodifiableMap(new HashMap<String, String>() {{
                        put("tag1", "val1");
                        put("tag2", "val2");
                        put("tag3", "v3");
                    }})
            );
        }});
    }


    /*
      Following tests related to #2926 issue.
     */

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

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
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

        List<List<String>> expectedRows = Collections.emptyList();

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

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
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

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
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

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("null")
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

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }
}
