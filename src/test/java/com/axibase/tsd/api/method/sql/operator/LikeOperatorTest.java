package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class LikeOperatorTest extends SqlTest {
    /**
     * #4030
     */
    @Test
    public void testLikeOperatorForMetricInWhereClause() throws Exception {
        final String uniquePrefix = "unique";

        Series series = Mocks.series();
        series.setMetric(uniquePrefix + series.getMetric());
        Registry.Metric.register(series.getMetric());

        Series otherSeries = Mocks.series();
        SeriesMethod.insertSeriesCheck(series, otherSeries);

        String sql = String.format(
                "SELECT metric%n" +
                "FROM atsd_series%n" +
                "WHERE metric in ('%s', '%s')%n" +
                      "AND metric LIKE '%s*'%n" +
                "LIMIT 2",
                series.getMetric(), otherSeries.getMetric(), uniquePrefix
        );

        String[][] expected = {
                { series.getMetric() }
        };

        assertSqlQueryRows(expected, sql);
    }

    /**
     * #4130
     */
    @Test
    public void testAggregationForSeveralMetrics() throws Exception {
        String entity = entity();
        Registry.Entity.register(entity);

        String[] metrics = new String[3];
        for (int i = 0; i < metrics.length; i++) {
            String metric = metric();
            metrics[i] = metric;
            Registry.Metric.register(metric);
        }

        Series[] seriesArray = new Series[3];
        for (int i = 0; i < seriesArray.length; i++) {
            Series series = new Series();
            series.setEntity(entity);
            series.setMetric(metrics[i]);
            series.addData(Mocks.SAMPLE);

            seriesArray[i] = series;
        }

        SeriesMethod.insertSeriesCheck(seriesArray);

        String sql = String.format(
                "SELECT " +
                    "COUNT(value), " +
                    "SUM(value), " +
                    "AVG(value) " +
                "FROM atsd_series " +
                "WHERE metric LIKE 'method-sql-operator-like-operator-test-test-aggregation-for-several-metrics-metric*'"
        );

        String[][] expected = {
                { "3", "370.3701", "123.4567" }
        };

        assertSqlQueryRows(expected, sql);
    }


}
