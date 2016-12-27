package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util.TestNames;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;


public class ColumnsTest extends SqlTest {
    private static final Sample DEFAULT_SAMPLE = new Sample("2016-10-16T17:10:00.000Z", "0");

    @DataProvider(name = "metricLabelValues")
    public Object[][] provideMetricLabels() {
        return new Object[][]{
                {"label"},
                {"br eak"},
                {"sai !nts"},
                {"Кириллица"}
        };
    }

    /**
     * 3474
     */
    @Test(dataProvider = "metricLabelValues")
    public void testSelectMetricLabel(String value) throws Exception {
        Metric metric = new Metric(TestNames.metric());
        metric.setEnabled(true);
        metric.setLabel(value);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series();
        series.addData(DEFAULT_SAMPLE);
        series.setMetric(metric.getName());
        series.setEntity(TestNames.entity());
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

        String sqlQuery = String.format("SELECT metric.label FROM '%s'", metric.getName());
        List<List<String>> expectedRows = (value != null) ?
                Collections.singletonList(Collections.singletonList(value)) :
                Collections.<List<String>>emptyList();
        assertSqlQueryRows(sqlQuery, expectedRows, String.format("Failed to select metric.label with value %s", value));
    }

    @DataProvider(name = "metricInterpolateValues")
    public Object[][] provideMetricInterpolateValues() {
        InterpolationMode[] values = InterpolationMode.values();
        Object[][] result = new Object[values.length][1];
        for (int i = 0; i < values.length; i++) {
            result[i][0] = values[i];
        }
        return result;
    }

    /**
     * 3474
     */
    @Test(dataProvider = "metricInterpolateValues")
    public void testSelectMetricInterpolate(InterpolationMode value) throws Exception {
        Metric metric = new Metric(TestNames.metric());
        metric.setEnabled(true);
        metric.setInterpolate(value);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series();
        series.addData(DEFAULT_SAMPLE);
        series.setMetric(metric.getName());
        series.setEntity(TestNames.entity());
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

        String sqlQuery = String.format("SELECT metric.interpolate FROM '%s'", metric.getName());
        InterpolationMode actualInterpolate = InterpolationMode.valueOf(
                queryTable(sqlQuery).getValueAt(0, 0)
        );
        assertEquals(String.format("Failed to select metric.label with value %s", value), value, actualInterpolate);
    }

    @DataProvider(name = "metricTimeZoneValues")
    public Object[][] provideMetricTimeZoneValues() {
        return new Object[][]{
                {"GMT0"},
                {"Africa/Accra"},
                {"Iceland"},
                {"Zulu"}
        };
    }

    /**
     * 3474
     */
    @Test(dataProvider = "metricTimeZoneValues")
    public void testSelectMetricTimeZone(String timezone) throws Exception {
        Metric metric = new Metric(TestNames.metric());
        metric.setEnabled(true);
        metric.setTimeZoneID(timezone);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series();
        series.addData(DEFAULT_SAMPLE);
        series.setMetric(metric.getName());
        series.setEntity(TestNames.entity());
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

        String sqlQuery = String.format("SELECT metric.timezone FROM '%s'", metric.getName());
        String[][] expectedRows = {
                {timezone}
        };
        assertSqlQueryRows(sqlQuery, expectedRows, String.format("Failed to get inserted timezone: %s", timezone));
    }

}

