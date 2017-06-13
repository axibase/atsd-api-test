package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;


public class SqlSelectAllTagsOrderTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-select-all-tags-order-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private Map<String, String> tags;

    @BeforeClass
    public void prepareData() throws Exception {
        tags = new HashMap<>();
        tags.put("a", "b");
        tags.put("tag", "value");
        tags.put("T", "V");
        tags.put("Tags", "value");
        tags.put("1", "0");
        tags.put("Т", "З");
        tags.put("имя", "значение");

        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME, tags);
        series.addSamples(new Sample("2016-06-03T09:23:00.000Z", 7));

        MetricMethod.createOrReplaceMetric(new Metric(TEST_METRIC_NAME, tags));

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #3166
     */
    @Test
    public void testSelectAll() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(tags), "tags", true);

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }

    /**
     * #3166
     */
    @Test
    public void testSelectAllSeriesTags() {
        String sqlQuery = String.format(
                "SELECT tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(tags), "tags", false);

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * #3166
     */
    @Test
    public void testSelectAllTagsAndColumnBefore() {
        String sqlQuery = String.format(
                "SELECT entity, tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(tags), "tags", false);
        expectedColumnNames.add(0, "entity");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * #3166
     */
    @Test
    public void testSelectAllTagsAndColumnAfter() {
        String sqlQuery = String.format(
                "SELECT tags.*,entity FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(tags), "tags", false);
        expectedColumnNames.add("entity");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * #3166
     */
    @Test
    public void testSelectAllSeriesMetricTags() {
        String sqlQuery = String.format(
                "SELECT metric.tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(tags), "metric.tags", false);

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * #3166
     */
    @Test
    public void testSelectAllMetricTagsAndColumnBefore() {
        String sqlQuery = String.format(
                "SELECT entity, metric.tags.* FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(tags), "metric.tags", false);
        expectedColumnNames.add(0, "entity");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * #3166
     */
    @Test
    public void testSelectAllMetricTagsAndColumnAfter() {
        String sqlQuery = String.format(
                "SELECT metric.tags.*,entity FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = sortedColumnNames(sortedTagsKeys(tags), "metric.tags", false);
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
        List<String> sortedTags = new ArrayList<>(handleTags(tags.keySet()));
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
