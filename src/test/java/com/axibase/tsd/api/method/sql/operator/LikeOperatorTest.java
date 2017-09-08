package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class LikeOperatorTest extends SqlTest {
    private static final String TEST_METRIC_PREFIX = metric();
    private static final int METRICS_COUNT = 100;
    private static final ArrayList<String> TEST_METRICS = new ArrayList<>(METRICS_COUNT);

    @BeforeTest
    public static void prepareData() throws Exception {
        String entity = entity();

        for (int i = 0; i < METRICS_COUNT / 2; i++) {
            String metric = String.format("%s-first-%02d", TEST_METRIC_PREFIX, i);
            TEST_METRICS.add(metric);
        }

        for (int i = METRICS_COUNT / 2; i < METRICS_COUNT; i++) {
            String metric = String.format("%s-second-%02d", TEST_METRIC_PREFIX, i - METRICS_COUNT / 2);
            TEST_METRICS.add(metric);
        }

        ArrayList<Series> seriesList = new ArrayList<>(METRICS_COUNT);
        for (String metric : TEST_METRICS) {
            Series series = new Series(entity, metric);
            series.addSamples(Mocks.SAMPLE);

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #4030
     */
    @Test
    public void testLikeOperatorForMetricInWhereClause() throws Exception {
        final String uniquePrefix = "unique";

        Series series = Mocks.series();
        series.setMetric(uniquePrefix + series.getMetric());
        Registry.Metric.checkExists(series.getMetric());

        Series otherSeries = Mocks.series();
        SeriesMethod.insertSeriesCheck(series, otherSeries);

        String sql = String.format(
                "SELECT metric%n" +
                        "FROM atsd_series%n" +
                        "WHERE metric in ('%s', '%s')%n" +
                        "AND metric LIKE '%s%%'%n" +
                        "LIMIT 2",
                series.getMetric(), otherSeries.getMetric(), uniquePrefix
        );

        String[][] expected = {
                { series.getMetric() }
        };

        assertSqlQueryRows(expected, sql);
    }

    /**
     * #4083
     */
    @Test
    public void testLikeOperatorForMetricLimit() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s-first-%%' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(0, 50), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeOperatorForMetricLimitOverflow() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s-%%' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        Response response = SqlMethod.queryResponse(sqlQuery);
        assertBadRequest("Too many metrics found. Maximum: 50", response);
    }

    /**
     * #4083
     */
    @Test
    public void testLikeOperatorForNoMatchingMetric() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s-not-match-%%' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        Response response = SqlMethod.queryResponse(sqlQuery);
        assertBadRequest("No matching metrics found", response);
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorExactMatch() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s' " +
                        "ORDER BY metric ",
                TEST_METRICS.get(0));

        StringTable table = SqlMethod.queryTable(sqlQuery);

        assertTableContainsColumnValues(Collections.singletonList(TEST_METRICS.get(0)), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorWildcards() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s-%%-0_' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        ArrayList<String> result = new ArrayList<>(20);
        result.addAll(TEST_METRICS.subList(0, 10));
        result.addAll(TEST_METRICS.subList(50, 60));

        assertTableContainsColumnValues(result, table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorQuestionWildcardsNoMatch() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s-first-___' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        Response response = SqlMethod.queryResponse(sqlQuery);
        assertBadRequest("No matching metrics found", response);
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorQuestionWildcardsMatch() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s-first-__' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(0, 50), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorAsteriskWildcardsZeroLength() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%s-first-_%%_' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(0, 50), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testMultipleLikeMetricOperatorsOr() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-1%%' OR metric LIKE '%1$s-first-2%%'" +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(10, 30), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testMultipleLikeMetricOperatorsAnd() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-__' AND metric LIKE '%1$s-first-2%%'" +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(20, 30), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorOrEquals() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-1_' OR metric = '%1$s-first-20'" +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(10, 21), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorAndNotEquals() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-1_' AND metric != '%1$s-first-10'" +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(11, 20), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorAndNotNull() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-1_' AND text IS NOT NULL " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(Collections.<String>emptyList(), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorAndIn() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-1_' AND metric IN ('%1$s-first-10', '%1$s-first-11') " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(10, 12), table, "metric");
    }

    /**
     * #4083
     */
    @Test
    public void testLikeMetricOperatorOrIn() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-1_' OR metric IN ('%1$s-first-20', '%1$s-first-21') " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(10, 22), table, "metric");
    }

    /**
     * #4152
     */
    @Test
    public void testMultipleLikeMetricOperatorNoMetricFitsCondition() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-1_' AND metric LIKE '%1$s-non-existing-*?' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        Response response = SqlMethod.queryResponse(sqlQuery);
        assertBadRequest("No matching metrics found", response);
    }

    /**
     * #4152
     */
    @Test
    public void testLikeMetricOperatorAndLess() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-%%' AND metric < '%1$s-first-10' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(0, 10), table, "metric");
    }

    /**
     * #4152
     */
    @Test
    public void testLikeMetricOperatorAndGreaterOrEquals() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric LIKE '%1$s-first-%%' AND metric >= '%1$s-first-40' " +
                        "ORDER BY metric ",
                TEST_METRIC_PREFIX);

        StringTable table = SqlMethod.queryTable(sqlQuery);
        assertTableContainsColumnValues(TEST_METRICS.subList(40, 50), table, "metric");
    }
}
