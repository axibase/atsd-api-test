package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class JoinWithTagsTest extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_METRIC3_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] metricNames = {TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_METRIC3_NAME};
        String[] tags = {"123", "123", "abc4"};

        Registry.Entity.checkExists(TEST_ENTITY_NAME);

        for (int i = 0; i < metricNames.length; i++) {
            Series series = new Series(TEST_ENTITY_NAME, metricNames[i], "tag", tags[i]);
            series.addSamples(new Sample("2016-06-03T09:20:00.000Z", i + 1));

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }


    /**
     * #3756
     */
    @Test
    public void testJoinWithEntityFilter() {
        String sqlQuery = String.format(
                    "SELECT t1.value, t2.value " +
                    "FROM \"%1$s\" t1 JOIN \"%2$s\" t2 " +
                    "WHERE t1.entity = '%3$s' AND t2.entity = '%3$s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Entity filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinWithEntityNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM \"%1$s\" t1 JOIN \"%2$s\" t2 " +
                        "WHERE t1.entity IS NOT NULL AND t2.entity IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Entity NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithEntityFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM \"%1$s\" t1 JOIN USING ENTITY \"%2$s\" t2 " +
                        "WHERE t1.entity = '%3$s' AND t2.entity = '%3$s'",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME, TEST_ENTITY_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Entity filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithEntityNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM \"%1$s\" t1 JOIN USING ENTITY \"%2$s\" t2 " +
                        "WHERE t1.entity IS NOT NULL AND t2.entity IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Entity NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinWithTagFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM \"%1$s\" t1 JOIN \"%2$s\" t2 " +
                        "WHERE t1.tags.tag = '123' AND t2.tags.tag = '123'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Tag filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinWithTagNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM \"%1$s\" t1 JOIN \"%2$s\" t2 " +
                        "WHERE t1.tags.tag IS NOT NULL AND t2.tags.tag IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Tag NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithTagFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM \"%1$s\" t1 JOIN USING ENTITY \"%2$s\" t2 " +
                        "WHERE t1.tags.tag = '123' AND t2.tags.tag = 'abc4'",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Tag filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithTagNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM \"%1$s\" t1 JOIN USING ENTITY \"%2$s\" t2 " +
                        "WHERE t1.tags.tag IS NOT NULL AND t2.tags.tag IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Tag NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3873
     */
    @Test
    public void testTagsAfterJoin() {
        String sqlQuery = String.format(
                "SELECT t1.tags, t2.tags " +
                        "FROM \"%1$s\" t1 JOIN \"%2$s\" t2 ",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"tag=123", "tag=123"}
        };

        assertSqlQueryRows("List of tags is malformed after JOIN", expectedRows, sqlQuery);
    }

    /**
     * #3873
     */
    @Test
    public void testSameTagsAfterJoinUsingEntity() {
        String sqlQuery = String.format(
                "SELECT t1.tags, t2.tags " +
                        "FROM \"%1$s\" t1 JOIN USING ENTITY \"%2$s\" t2 ",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"tag=123", "tag=123"}
        };

        assertSqlQueryRows("List of tags (with same value) is malformed after JOIN USING ENTITY",
                expectedRows, sqlQuery);
    }

    /**
     * #3873
     */
    @Test
    public void testDifferentTagsAfterJoinUsingEntity() {
        String sqlQuery = String.format(
                "SELECT t1.tags, t2.tags " +
                        "FROM \"%1$s\" t1 JOIN USING ENTITY \"%2$s\" t2 ",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"tag=123", "tag=abc4"}
        };

        assertSqlQueryRows("List of tags (with different value) is malformed after JOIN USING ENTITY",
                expectedRows, sqlQuery);
    }

    /**
     * #4027
     */
    @Test
    public void testJoinKeepsMetricTags() throws Exception {
        Series series = Mocks.series();
        Map<String, String> tags = new HashMap<>();
        tags.put("foobar", "foo");
        Metric metric = new Metric(series.getMetric(), tags);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series otherSeries = Mocks.series();
        otherSeries.setEntity(series.getEntity());
        SeriesMethod.insertSeriesCheck(series, otherSeries);

        String sql = String.format(
                "SELECT t1.metric.tags.foobar, t1.metric.tags%n" +
                "FROM \"%s\" as t1%n" +
                "JOIN \"%s\" as t2",
                metric.getName(), otherSeries.getMetric()
        );
        String[][] expected = {
                {"foo", "foobar=foo"}
        };

        assertSqlQueryRows("Metric tags are absent or corrupted in JOIN", expected, sql);
    }

    /**
     * #4027
     */
    @Test
    public void testJoinKeepsMetricFields() throws Exception {
        Series series = Mocks.series();
        Series otherSeries = Mocks.series();
        otherSeries.setEntity(series.getEntity());
        SeriesMethod.insertSeriesCheck(series, otherSeries);

        String sql = String.format(
                "SELECT t1.metric.interpolate, t2.metric.interpolate%n" +
                "FROM \"%s\" as t1%n" +
                "JOIN \"%s\" as t2",
                series.getMetric(), otherSeries.getMetric()
        );
        String[][] expected = {
                {"LINEAR", "LINEAR"}
        };

        assertSqlQueryRows("Metric interpolate field is absent or corrupted in JOIN", expected, sql);
    }

    /**
     * #3939
     */
    @Test
    public void testJoinSeriesWithChangedMetrics() throws Exception {
        String entity = entity();
        String[] metrics = { metric(), metric(), metric() };

        List<Series> initialSeries = new ArrayList<>(metrics.length);
        for (String metric : metrics) {
            Series series = new Series(entity, metric, "tag", "value");
            series.addSamples(Mocks.SAMPLE);

            initialSeries.add(series);
        }

        SeriesMethod.insertSeriesCheck(initialSeries);

        String sqlQuery = String.format(
                "SELECT t1.tags " +
                "FROM \"%s\" t1 " +
                "JOIN USING ENTITY \"%s\" t2 " +
                "JOIN USING ENTITY \"%s\" t3 " +
                "WHERE t1.tags.tag = 'value' AND " +
                "t2.tags.tag = 'value' AND " +
                "t3.tags.tag = 'value'",
                metrics[0], metrics[1], metrics[2]
        );

        String[][] expectedRows = {
                {"tag=value"}
        };

        assertSqlQueryRows("Initial series query gives wrong result",
                expectedRows, sqlQuery);

        List<Series> changedSeries = new ArrayList<>(metrics.length);
        for (String metric : metrics) {
            Series series = new Series();
            series.setEntity(entity);
            series.setMetric(metric);
            series.addTag("tag1", "value");
            series.addSamples(Mocks.SAMPLE);

            changedSeries.add(series);
        }

        SeriesMethod.insertSeriesCheck(changedSeries);

        assertSqlQueryRows("Query after series change gives wrong result",
                expectedRows, sqlQuery);
    }
}
