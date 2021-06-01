package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class SqlEntityTagsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-where-entity-tags-support-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";


    @BeforeClass
    public static void prepareData() throws Exception {

        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            addSamples(Sample.ofDateInteger("2016-06-19T11:00:00.000Z", 3));
        }};

        EntityMethod.createOrReplaceEntityCheck(
                new Entity(
                        TEST_ENTITY_NAME,
                        new HashMap<String, String>() {{
                            put("step", "0.5");
                            put("tag1", "val1");
                            put("tag2", "val2");
                            put("tag3", "v3");
                        }}
                )
        );

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @Issue("2926")
    @Test
    public void testLikeOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 %nFROM \"%s\" %nWHERE datetime='2016-06-19T11:00:00.000Z' AND " +
                        "entity.tags.tag1 LIKE 'val%%' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("2926")
    @Test
    public void testNotLikeOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 %nFROM \"%s\" %nWHERE datetime='2016-06-19T11:00:00.000Z' AND " +
                        "entity.tags.tag1 NOT LIKE 'val%%' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("2926")
    @Test
    public void testEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 %nFROM \"%s\" %nWHERE datetime='2016-06-19T11:00:00.000Z' " +
                        "AND entity.tags.tag1 ='val1' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("2926")
    @Test
    public void testNotEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag1 FROM \"%s\" %n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag1 <> 'val2' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("val1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("2926")
    @Test
    public void testIsNullOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag4 FROM \"%s\"%nWHERE datetime='2016-06-19T11:00:00.000Z' " +
                        "AND entity.tags.tag4 IS NULL AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("2926")
    @Test
    public void testIsNotNullOperator() {
        String sqlQuery = String.format(
                "SELECT entity.tags.tag4 FROM \"%s\" %n" +
                        "WHERE datetime='2016-06-19T11:00:00.000Z' AND entity.tags.tag4 IS NOT NULL %n" +
                        "AND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    public void testReservedKeywords() {
        String sqlQuery = String.format(
                "SELECT entity.tags.step, entity.tags.\"step\"  %nFROM \"%s\" %nWHERE datetime='2016-06-19T11:00:00.000Z' AND " +
                        "entity.tags.tag1 LIKE 'val%%' %nAND entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("0.5", "0.5")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }
}
