package com.axibase.tsd.api.method.sql.clause.where;

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


public class SqlEntityTagsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-where-entity-tags-support-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";


    @BeforeClass
    public static void prepareDate() throws Exception {

        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            addData(new Sample("2016-06-19T11:00:00.000Z", 3));
        }};
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

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
      #2926 issue.
     */

    /**
     * #2926
     */
    @Test
    public void testLikeOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 %nFROM '%s' %nWHERE datetime='2016-06-19T11:00:00.000Z' AND " +
                        "entity.tags.tag1 LIKE 'val*' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #2926
     */
    @Test
    public void testNotLikeOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 %nFROM '%s' %nWHERE datetime='2016-06-19T11:00:00.000Z' AND " +
                        "entity.tags.tag1 NOT LIKE 'val*' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2926
     */
    @Test
    public void testEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 %nFROM '%s' %nWHERE datetime='2016-06-19T11:00:00.000Z' " +
                        "AND entity.tags.tag1 ='val1' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2926
     */
    @Test
    public void testNotEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 FROM '%s' %n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag1 <> 'val2' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2926
     */
    @Test
    public void testIsNullOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag4 FROM '%s'%nWHERE datetime='2016-06-19T11:00:00.000Z' " +
                        "AND entity.tags.tag4 IS NULL AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #2926
     */
    @Test
    public void testIsNotNullOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag4 FROM '%s' %n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag4 IS NOT NULL %n" +
                        "AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }
}
