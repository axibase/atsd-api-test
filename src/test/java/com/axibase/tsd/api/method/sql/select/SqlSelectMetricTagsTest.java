package com.axibase.tsd.api.method.sql.select;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlSelectMetricTagsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-select-metric-tags-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    private static void updateSeriesMetricTags(Series series, Map<String, String> tags) {
        Metric metric = new Metric()
                .setName(series.getMetric())
                .setTags(tags);
        try {
            MetricMethod.updateMetric(metric);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        sendSamplesToSeries(series,
                new Sample("2016-06-29T08:00:00.000Z", "0"));
        updateSeriesMetricTags(series, Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("a", "b");
            put("b", "c");
        }}));

    }

    /*
      Following tests related to issue #3056
     */


    /**
     * Issue #3056
     */
    @Test
    public void testSelectMetricTags() {
        String sqlQuery =
                "SELECT metric.tags\n" +
                        "FROM 'sql-select-metric-tags-metric'\n" +
                        "WHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='sql-select-metric-tags-entity'\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Arrays.asList("metric.tags"), resultTable);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("a=b;b=c")
        );
        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3056
     */
    @Test
    public void testSelectMetricMultipleTags() {
        String sqlQuery =
                "SELECT metric.tags.*\n" +
                        "FROM 'sql-select-metric-tags-metric'\n" +
                        "WHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='sql-select-metric-tags-entity'\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Arrays.asList(TEST_METRIC_NAME + ".metric.tags.a", TEST_METRIC_NAME + ".metric.tags.b"), resultTable);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("b", "c")
        );
        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3056
     */
    @Test
    public void testSelectMetricSpecifiedTag() {
        String sqlQuery =
                "SELECT metric.tags.a\n" +
                        "FROM 'sql-select-metric-tags-metric'\n" +
                        "WHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='sql-select-metric-tags-entity'\n";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Arrays.asList("metric.tags.a"), resultTable);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("b")
        );
        assertTableRows(expectedRows, resultTable);
    }
}
