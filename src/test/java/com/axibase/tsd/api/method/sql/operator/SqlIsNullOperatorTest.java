package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;


public class SqlIsNullOperatorTest extends SqlTest {
    private final static String TEST_PREFIX = "sql-operator-is-null-";
    private final static String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private final static String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private final static String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private final static String TEST_ENTITY3_NAME = TEST_PREFIX + "entity-3";
    private final static String TEST_ENTITY4_NAME = TEST_PREFIX + "entity-4";
    private final static String TEST_ENTITY5_NAME = TEST_PREFIX + "entity-5";

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        seriesList.add(new Series(TEST_ENTITY1_NAME, TEST_METRIC_NAME, "tag1", "val1") {{
            addSamples(Sample.ofDateInteger("2016-06-19T11:00:00.000Z", 1));
        }});


        seriesList.add(new Series(TEST_ENTITY2_NAME, TEST_METRIC_NAME, "tag1", "val2", "tag2", "val2") {{
            addSamples(Sample.ofDateInteger("2016-06-19T11:05:00.000Z", 2));
        }});

        seriesList.add(new Series(TEST_ENTITY3_NAME, TEST_METRIC_NAME, "tag2", "val3") {{
            addSamples(Sample.ofDateInteger("2016-06-19T11:10:00.000Z", 3));
        }});

        seriesList.add(new Series(TEST_ENTITY4_NAME, TEST_METRIC_NAME, "tag4", "val4") {{
            addSamples(Sample.ofDateInteger("2016-06-19T11:15:00.000Z", 4));
        }});

        seriesList.add(new Series(TEST_ENTITY5_NAME, TEST_METRIC_NAME) {{
            addSamples(Sample.ofDateInteger("2016-06-19T11:20:00.000Z", 5));
        }});

        EntityMethod.createOrReplaceEntityCheck(new Entity(TEST_ENTITY1_NAME, new HashMap<String, String>() {{
                    put("tag1", "val1");
                }}
                )
        );

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("2937")
    @Test
    public void testIsNullMetricSpecifiedTag() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags.* %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:11:00.000Z' %n" +
                        "AND tags.tag4 IS NULL %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);


        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2016-06-19T11:00:00.000Z", "1", "val1", "null"),
                Arrays.asList(TEST_ENTITY2_NAME, "2016-06-19T11:05:00.000Z", "2", "val2", "val2"),
                Arrays.asList(TEST_ENTITY3_NAME, "2016-06-19T11:10:00.000Z", "3", "null", "val3")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3515")
    @Test
    public void testIsNullMetricTags() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:16:00.000Z' and datetime < '2016-06-19T11:21:00.000Z' %n" +
                        "AND tags IS NULL %n",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY5_NAME, "2016-06-19T11:20:00.000Z", "5", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("2937")
    @Test
    public void testNotIsNullMetricSpecifiedTag() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags.* %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z' %n" +
                        "AND NOT tags.tag4 IS NULL %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY4_NAME, "2016-06-19T11:15:00.000Z", "4", "null", "null", "val4")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3515")
    @Test
    public void testNotIsNullMetricTags() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:21:00.000Z' %n" +
                        "AND NOT tags IS NULL %n",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME, "2016-06-19T11:00:00.000Z", "1", "tag1=val1"},
                {TEST_ENTITY2_NAME, "2016-06-19T11:05:00.000Z", "2", "tag1=val2;tag2=val2"},
                {TEST_ENTITY3_NAME, "2016-06-19T11:10:00.000Z", "3", "tag2=val3"},
                {TEST_ENTITY4_NAME, "2016-06-19T11:15:00.000Z", "4", "tag4=val4"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("2937")
    @Test
    public void testIsNotNullMetricSpecifiedTag() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags.* %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z' %n" +
                        "AND tags.tag4 IS NOT NULL %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY4_NAME, "2016-06-19T11:15:00.000Z", "4", "val4")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3515")
    @Test
    public void testIsNotNullMetricTags() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:21:00.000Z' %n" +
                        "AND tags IS NOT NULL %n",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME, "2016-06-19T11:00:00.000Z", "1", "tag1=val1"},
                {TEST_ENTITY2_NAME, "2016-06-19T11:05:00.000Z", "2", "tag1=val2;tag2=val2"},
                {TEST_ENTITY3_NAME, "2016-06-19T11:10:00.000Z", "3", "tag2=val3"},
                {TEST_ENTITY4_NAME, "2016-06-19T11:15:00.000Z", "4", "tag4=val4"}
        };
        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("2937")
    @Test
    public void testNotIsNotNullMetricSpecifiedTag() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags.* %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z' %n" +
                        "AND NOT tags.tag4 IS NOT NULL %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2016-06-19T11:00:00.000Z", "1", "val1", "null", "null"),
                Arrays.asList(TEST_ENTITY2_NAME, "2016-06-19T11:05:00.000Z", "2", "val2", "val2", "null"),
                Arrays.asList(TEST_ENTITY3_NAME, "2016-06-19T11:10:00.000Z", "3", "null", "val3", "null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3515")
    @Test
    public void testNotIsNotNullMetricTags() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:21:00.000Z' %n" +
                        "AND NOT tags IS NOT NULL %n",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY5_NAME, "2016-06-19T11:20:00.000Z", "5", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("2937")
    @Test
    public void testIsNotNullEntitySpecifiedTag() throws Exception {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags.* %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z' %n" +
                        "AND entity.tags.tag1 IS NOT NULL",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY1_NAME, "2016-06-19T11:00:00.000Z", "1", "val1", "null", "null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3515")
    @Test
    public void testIsNotNullEntityTags() throws Exception {
        String sqlQuery = String.format(
                "SELECT entity %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:16:00.000Z' %n" +
                        "AND entity.tags IS NOT NULL",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("2937")
    @Test
    public void testIsNullEntitySpecifiedTag() throws Exception {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value, tags.* %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:11:00.000Z' %n" +
                        "AND entity.tags.tag1 IS NULL %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY2_NAME, "2016-06-19T11:05:00.000Z", "2", "val2", "val2", "null"),
                Arrays.asList(TEST_ENTITY3_NAME, "2016-06-19T11:10:00.000Z", "3", "null", "val3", "null")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3515")
    @Test
    public void testIsNullEntityTags() throws Exception {
        String sqlQuery = String.format(
                "SELECT entity %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T11:21:00.000Z' %n" +
                        "AND entity.tags IS NULL %n",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY2_NAME},
                {TEST_ENTITY3_NAME},
                {TEST_ENTITY4_NAME},
                {TEST_ENTITY5_NAME}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3516")
    @Test
    public void testIsNullMetricLabel() throws Exception {
        Metric metric = new Metric("m-test-operator-is-null-metric-label");
        metric.setLabel(null);

        String entityName = "e-test-operator-is-null-metric-label";
        Series series = new Series(entityName, metric.getName());
        series.addSamples(Sample.ofDateInteger("2016-06-19T00:00:00.000Z", 1));

        MetricMethod.createOrReplaceMetricCheck(metric);
        SeriesMethod.insertSeriesCheck(series);

        String sqlQuery = String.format(
                "SELECT entity %n" +
                        "FROM \"m-test-operator-is-null-metric-label\" %n" +
                        "WHERE datetime >= '2016-06-19T00:00:00.000Z' and datetime < '2016-06-19T00:01:00.000Z' %n" +
                        "AND metric.label IS NULL %n"
        );

        String[][] expectedRows = {
                {entityName}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3516")
    @Test
    public void testIsNotNullMetricLabel() throws Exception {
        Metric metric = new Metric("m-test-operator-is-not-null-metric-label");
        metric.setLabel("foo");


        String entityName = "e-test-operator-is-not-null-metric-label";
        Series series = new Series(entityName, metric.getName());
        series.addSamples(Sample.ofDateInteger("2016-06-19T00:00:00.000Z", 2));

        MetricMethod.createOrReplaceMetricCheck(metric);
        SeriesMethod.insertSeriesCheck(series);

        String sqlQuery = String.format(
                "SELECT entity %n" +
                        "FROM \"m-test-operator-is-not-null-metric-label\" %n" +
                        "WHERE datetime >= '2016-06-19T00:00:00.000Z' and datetime < '2016-06-19T00:01:00.000Z' %n" +
                        "AND metric.label IS NOT NULL %n"
        );

        String[][] expectedRows = {
                {entityName}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4281")
    @Test
    public void testIsNullFunctionEntityTags() {
        String sqlQuery = String.format(
                "SELECT ISNULL(entity.tags, '-'), ISNULL(entity.tags.tag, '-') FROM \"%s\" WHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY5_NAME);

        String[][] expectedRows = {
                {"-", "-"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4281")
    @Test
    public void testIsNullFunctionMetricTags() {
        String sqlQuery = String.format(
                "SELECT ISNULL(metric.tags, '-'), ISNULL(metric.tags.tag, '-') FROM \"%s\" WHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY5_NAME);

        String[][] expectedRows = {
                {"-", "-"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
