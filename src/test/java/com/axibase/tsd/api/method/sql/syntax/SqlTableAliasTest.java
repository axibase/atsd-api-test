package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;


public class SqlTableAliasTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-standard-table-alias";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    static void prepareData() throws Exception {

        final Map<String, String> tags = Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("a", "b");
            put("b", "c");
        }});
        Registry.Metric.register(TEST_METRIC1_NAME);
        Registry.Metric.register(TEST_METRIC2_NAME);
        Registry.Entity.register(TEST_ENTITY_NAME);
        SeriesMethod.insertSeriesCheck(
                Arrays.asList(
                        new Series() {{
                            setMetric(TEST_METRIC1_NAME);
                            setEntity(TEST_ENTITY_NAME);
                            addData(new Sample("2016-06-03T09:24:00.000Z", 0));
                            setTags(tags);
                        }},
                        new Series() {{
                            setMetric(TEST_METRIC2_NAME);
                            setEntity(TEST_ENTITY_NAME);
                            setTags(tags);
                            addData(new Sample("2016-06-03T09:24:00.000Z", 1));
                        }}
                )
        );
    }


    /*
    #3084
     */

    /**
     * #3084
     */
    @Test
    public void testSelectColumnWithoutJoinWithoutAlias() {
        String sqlQuery = String.format(
                "SELECT entity, value, tags FROM '%s'", TEST_METRIC1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList("entity", "value", "tags");

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }


    /**
     * #3084
     */
    @Test
    public void testSelectAllWithoutJoinWithoutAlias() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s'",
                TEST_METRIC1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList("entity", "value", "tags.a", "tags.b", "datetime");

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }

    /**
     * #3084
     */
    @Test
    public void testSelectAllWithoutJoinWithAlias() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' t1",
                TEST_METRIC1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList("entity", "value", "tags.a", "tags.b", "datetime");

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }

    /**
     * #3084
     */
    @Test
    public void testSelectColumnWithJoinWithAlias() {
        String sqlQuery = String.format(
                "SELECT '%s'.entity, '%s'.value, '%s'.entity, '%s'.value FROM '%s' t1 %n" +
                        "JOIN  '%s' t2 ",
                TEST_METRIC1_NAME, TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_METRIC2_NAME, TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(TEST_METRIC1_NAME + ".entity", TEST_METRIC1_NAME + ".value", TEST_METRIC2_NAME + ".entity", TEST_METRIC2_NAME + ".value");

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }

    /**
     * #3084
     */
    @Test
    public void testSelectColumnWithJoinWithoutAlias() {
        String sqlQuery = String.format(
                "SELECT '%s'.entity, '%s'.value, '%s'.entity, '%s'.value FROM '%s' %n" +
                        "JOIN  '%s'",
                TEST_METRIC1_NAME, TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_METRIC2_NAME, TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(TEST_METRIC1_NAME + ".entity", TEST_METRIC1_NAME + ".value", TEST_METRIC2_NAME + ".entity", TEST_METRIC2_NAME + ".value");

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }

    /**
     * #3084
     */
    @Test
    public void testSelectAllWithJoinWithoutAlias() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' %n" +
                        "JOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                TEST_METRIC1_NAME + ".entity",
                TEST_METRIC1_NAME + ".value",
                TEST_METRIC1_NAME + ".tags.a",
                TEST_METRIC1_NAME + ".tags.b",
                TEST_METRIC1_NAME + ".datetime",
                TEST_METRIC2_NAME + ".entity",
                TEST_METRIC2_NAME + ".value",
                TEST_METRIC2_NAME + ".tags.a",
                TEST_METRIC2_NAME + ".tags.b",
                TEST_METRIC2_NAME + ".datetime"
        );

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }

    /**
     * #3084
     */
    @Test
    public void testSelectAllWithJoinWithAlias() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' t1 %n" +
                        "JOIN '%s' t2",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                "t1.entity",
                "t1.value",
                "t1.tags.a",
                "t1.tags.b",
                "t1.datetime",
                "t2.entity",
                "t2.value",
                "t2.tags.a",
                "t2.tags.b",
                "t2.datetime"
        );

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }

}
