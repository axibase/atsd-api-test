package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class SqlSelectMetricTagsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-select-metric-tags-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            addData(new Sample("2016-06-29T08:00:00.000Z", "0"));
        }};
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

        MetricMethod.updateMetric(TEST_METRIC_NAME, new Metric() {{
            setTags(
                    Collections.unmodifiableMap(new HashMap<String, String>() {{
                        put("a", "b");
                        put("b", "c");
                        put("a-b", "b-c");
                        put("Tag", "V");
                    }})
            );
        }});

    }

    /**
     * #3056
     */
    @Test
    public void testSelectMetricTags() {
        String sqlQuery = String.format(
                "SELECT metric.tags %nFROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='%s' %n",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Collections.singletonList("metric.tags"), resultTable);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("a=b;a-b=b-c;b=c;tag=V")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #3056
     */
    @Test
    public void testSelectMetricMultipleTags() {
        String sqlQuery = String.format(
                "SELECT metric.tags.* %n FROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='%s' %n",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Arrays.asList(
                "metric.tags.tag",
                "metric.tags.a",
                "metric.tags.a-b",
                "metric.tags.b"), resultTable);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("b", "b-c", "c", "V")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3056
     */
    @Test
    public void testSelectMetricSpecifiedTag() {
        String sqlQuery = String.format(
                "SELECT metric.tags.a %nFROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Collections.singletonList("metric.tags.a"), resultTable);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("b")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3056
     */
    @Test
    public void testSelectMetricSpecifiedTagWithDash() {
        String sqlQuery = String.format(
                "SELECT metric.tags.'a-b' %nFROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Collections.singletonList("metric.tags.a-b"), resultTable);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("b-c")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3056
     */
    @Test
    public void testSelectMetricSpecifiedTagCaseSensitivityFalse() {
        String sqlQuery = String.format(
                "SELECT metric.tags.tag %nFROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Collections.singletonList("metric.tags.tag"), resultTable);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("V")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3056
     */
    @Test
    public void testSelectMetricSpecifiedTagCaseSensitivityTrue() {
        String sqlQuery = String.format(
                "SELECT metric.tags.Tag %nFROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z'AND entity='%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        assertTableColumnsNames(Collections.singletonList("metric.tags.Tag"), resultTable);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("null")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }
}
