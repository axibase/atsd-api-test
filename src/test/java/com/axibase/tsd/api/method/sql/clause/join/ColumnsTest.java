package com.axibase.tsd.api.method.sql.clause.join;


import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Test;

import java.util.Arrays;


public class ColumnsTest extends SqlTest {
    @Test
    public void testJoinedLabels() throws Exception {
        Series series1 = Mocks.series();
        Series series2 = Mocks.series();
        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));

        String label1 = "label1";
        String label2 = "label2";

        updateMetricLabel(series1.getMetric(), label1);
        updateMetricLabel(series2.getMetric(), label2);

        String sqlQuery = String.format(
                "SELECT t1.metric.label, t2.metric.label FROM '%s' t1%nOUTER JOIN '%s' t2",
                series1.getMetric(), series2.getMetric()
        );

        String[][] expectedRows = {
                {label1, label2},
                {label1, label2}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Failed to select metric.label from joined table");
    }

    @Test
    public void testJoinedTimeZones() throws Exception {
        Series series1 = Mocks.series();
        Series series2 = Mocks.series();
        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));

        String timeZone1 = "GMT0";
        String timeZone2 = "Africa/Algiers";

        updateMetricTimeZone(series1.getMetric(), timeZone1);
        updateMetricTimeZone(series2.getMetric(), timeZone2);

        String sqlQuery = String.format(
                "SELECT t1.metric.timezone, t2.metric.timezone FROM '%s' t1%nOUTER JOIN '%s' t2",
                series1.getMetric(), series2.getMetric()
        );

        String[][] expectedRows = {
                {timeZone1, timeZone2},
                {timeZone1, timeZone2}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Failed to select metric.timezone from joined table");
    }

    @Test
    public void testJoinedInterpolationMode() throws Exception {
        Series series1 = Mocks.series();
        Series series2 = Mocks.series();
        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));

        InterpolationMode interpolate1 = InterpolationMode.LINEAR;
        InterpolationMode interpolate2 = InterpolationMode.PREVIOUS;

        updateMetricInterpolate(series1.getMetric(), interpolate1);
        updateMetricInterpolate(series2.getMetric(), interpolate2);

        String sqlQuery = String.format(
                "SELECT t1.metric.interpolate, t2.metric.interpolate FROM '%s' t1%nOUTER JOIN '%s' t2",
                series1.getMetric(), series2.getMetric()
        );

        String[][] expectedRows = {
                {interpolate1.toString(), interpolate2.toString()},
                {interpolate1.toString(), interpolate2.toString()}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Failed to select metric.label from joined table");
    }


    private void updateMetricLabel(String metricName, String label) throws Exception {
        Metric updateMetric = new Metric();
        updateMetric.setLabel(label);
        updateMetric.setName(metricName);
        MetricMethod.updateMetric(updateMetric);
    }


    private void updateMetricTimeZone(String metricName, String timeZoneId) throws Exception {
        Metric updateMetric = new Metric();
        updateMetric.setTimeZoneID(timeZoneId);
        updateMetric.setName(metricName);
        MetricMethod.updateMetric(updateMetric);
    }


    private void updateMetricInterpolate(String metricName, InterpolationMode interpolate) throws Exception {
        Metric updateMetric = new Metric();
        updateMetric.setInterpolate(interpolate);
        updateMetric.setName(metricName);
        MetricMethod.updateMetric(updateMetric);
    }
}
