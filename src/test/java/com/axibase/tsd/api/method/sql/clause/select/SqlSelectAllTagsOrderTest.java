package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlSelectAllTagsOrderTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-select-all-tags-order-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final Map<String, String> TAGS = Collections.unmodifiableMap(
            new HashMap<String, String>() {{
                put("a", "b");
                put("tag", "value");
                put("T", "V");
                put("Tag", "Value");
                put("1", "0");
                put("Т", "З");
                put("имя", "значение");
            }}
    );

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);


        Series series = new Series();
        series.setMetric(TEST_METRIC_NAME);
        series.setEntity(TEST_ENTITY_NAME);
        series.addData(new Sample("2016-06-03T09:23:00.000Z", "7"));
        series.setTags(TAGS);

        SeriesMethod.insertSeriesCheck(series);


        Metric metricUpdateQuery = new Metric();
        metricUpdateQuery.setTags(TAGS);
        MetricMethod.updateMetric(TEST_METRIC_NAME, metricUpdateQuery);
    }

    /**
     * Issue #3166
     */
    @Test
    public void testSelectAll() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(TAGS), "tags", true);

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }

    /**
     * Issue #3166
     */
    @Test
    public void testSelectAllSeriesTags() {
        String sqlQuery = String.format(
                "SELECT tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(TAGS), "tags", false);

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * Issue #3166
     */
    @Test
    public void testSelectAllTagsAndColumnBefore() {
        String sqlQuery = String.format(
                "SELECT entity, tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(TAGS), "tags", false);
        expectedColumnNames.add(0, "entity");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * Issue #3166
     */
    @Test
    public void testSelectAllTagsAndColumnAfter() {
        String sqlQuery = String.format(
                "SELECT tags.*,entity FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(TAGS), "tags", false);
        expectedColumnNames.add("entity");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * Issue #3166
     */
    @Test
    public void testSelectAllSeriesMetricTags() {
        String sqlQuery = String.format(
                "SELECT metric.tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(TAGS), "metric.tags", false);

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * Issue #3166
     */
    @Test
    public void testSelectAllMetricTagsAndColumnBefore() {
        String sqlQuery = String.format(
                "SELECT entity, metric.tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(TAGS), "metric.tags", false);
        expectedColumnNames.add(0, "entity");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * Issue #3166
     */
    @Test
    public void testSelectAllMetricTagsAndColumnAfter() {
        String sqlQuery = String.format(
                "SELECT metric.tags.*,entity FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(TAGS), "metric.tags", false);
        expectedColumnNames.add("entity");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    private Set<String> handleTags(Set<String> tagsKeys) {
        Set<String> resultSet = new HashSet<>();
        for (String key : tagsKeys) {
            resultSet.add(key.trim().toLowerCase());
        }
        return resultSet;
    }

    private List<String> sortedTagsKeys(Map<String, String> tags) {
        List<String> sortedTags = new ArrayList<String>(handleTags(tags.keySet()));
        Collections.sort(sortedTags);
        return sortedTags;
    }

    private List<String> sortedColumnNames(List<String> tagsKeys, String prefix, Boolean isSelectAll) {
        Stack<String> expectedColumnNames = new Stack<>();
        if (isSelectAll) {
            expectedColumnNames.push("entity");
            expectedColumnNames.push("datetime");
            expectedColumnNames.push("value");
        }
        for (String tagKey : tagsKeys) {
            expectedColumnNames.push(String.format("%s.%s", prefix, tagKey));
        }
        return expectedColumnNames;
    }
}
