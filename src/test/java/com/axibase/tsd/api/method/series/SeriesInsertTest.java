package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.compaction.CompactionMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static junit.framework.Assert.*;


public class SeriesInsertTest extends SeriesMethod {
    /* #2871 */
    @Test
    public void testBigFloatOverflow() throws Exception {
        String entityName = "e-float-1";
        String metricName = "m-float-1";
        String largeNumber = "10.121212121212121212212121212121212121212121";
        final long t = 1465485524888L;

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(t, largeNumber));

        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.FLOAT);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertTrue("Failed to insert float series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored big float value rounded incorrect", new BigDecimal("10.12121212121212121"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testBigDecimalOverflow() throws Exception {
        String entityName = "e-decimal-1";
        String metricName = "m-decimal-1";
        String largeNumber = "10.121212121212121212212121212121212121212121";
        final long t = 1465485524888L;

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(t, largeNumber));

        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertFalse("Managed to insert large decimal series", insertSeries(series, 1000));
    }

    /* #2871 */
    @Test
    public void testBigDecimalAggregatePrecision() throws Exception {
        String entityName = "e-decimal-2";
        String metricName = "m-decimal-2";
        String number = "0.6083333332";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        for (int i = 0; i < 12; i++) {
            series.addData(new Sample(t + i * 5000, number));
        }
        assertTrue("Failed to insert small decimal series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1 + 11 * 5000);
        seriesQuery.addAggregateType("SUM");
        seriesQuery.setAggregatePeriod(1, "MINUTE");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored small decimal value incorrect", new BigDecimal("7.2999999984"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testDoubleAggregatePrecision() throws Exception {
        String entityName = "e-double-3";
        String metricName = "m-double-3";
        String number = "0.6083333332";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DOUBLE);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        for (int i = 0; i < 12; i++) {
            series.addData(new Sample(t + i * 5000, number));
        }
        assertTrue("Failed to insert small decimal series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1 + 11 * 5000);
        seriesQuery.addAggregateType("SUM");
        seriesQuery.setAggregatePeriod(1, "MINUTE");

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored small double value incorrect", new BigDecimal("7.299999998400001"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testDoublePrecisionAfterCompaction() throws Exception {
        String entityName = "e-double-4";
        String metricName = "m-double-4";
        String number = "90000000000000003.9";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DOUBLE);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        series.addData(new Sample(t, number));
        assertTrue("Failed to insert double series", insertSeries(series, 1000));

        CompactionMethod.performCompaction("2016-06-15", true);
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored float value precision incorrect", new BigDecimal("9.0E16"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testFloatPrecisionAfterCompaction() throws Exception {
        String entityName = "e-float-4";
        String metricName = "m-float-4";
        String number = "900000003.9";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.FLOAT);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        series.addData(new Sample(t, number));
        assertTrue("Failed to insert float series", insertSeries(series, 1000));

        CompactionMethod.performCompaction("2016-06-15", true);
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored float value precision incorrect", new BigDecimal("9.0E8"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testDecimalPrecisionAfterCompaction() throws Exception {
        String entityName = "e-decimal-4";
        String metricName = "m-decimal-4";
        String number = "90000000000000003.93";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        series.addData(new Sample(t, number));
        assertTrue("Failed to insert decimal series", insertSeries(series, 1000));

        CompactionMethod.performCompaction("2016-06-15", true);
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored decimal value precision incorrect", new BigDecimal("90000000000000003.93"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsZmsAbsent() throws Exception {
        String entityName = "e-iso-1";
        String metricName = "m-iso-1";
        String value = "0";

        String storedDate = "2016-06-09T17:08:09.000Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09Z";
        series.addData(new Sample(d, value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.001Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsZms() throws Exception {
        String entityName = "e-iso-2";
        String metricName = "m-iso-2";
        String value = "0";

        String storedDate = "2016-06-09T17:08:09.100Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09.100Z";
        series.addData(new Sample(d, value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.101Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsPlusHoursNoMS() throws Exception {
        String entityName = "e-iso-3";
        String metricName = "m-iso-3";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.000Z";
        series.addData(new Sample("2016-06-09T17:08:09+07:00", value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:09.100Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsPlusHoursMS() throws Exception {
        String entityName = "e-iso-4";
        String metricName = "m-iso-4";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.999Z";
        series.addData(new Sample("2016-06-09T17:08:09.999+07:00", value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:10Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }


    /* #2913 */
    @Test
    public void testUnderscoreSequence() throws Exception {
        final long t = 1465485524888L;

        Series series = new Series("e___underscore", "m___underscore");
        series.addData(new Sample(t, "0"));

        assertTrue("Failed to insert float series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored big float value rounded incorrect", new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
        assertEquals("Returned incorrect entity", series.getEntity(), seriesList.get(0).getEntity());
        assertEquals("Returned incorrect metric", series.getMetric(), seriesList.get(0).getMetric());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMinInMSSsaved() throws Exception {
        Long time = 0L;
        Long endTime = 1L;
        Series series = new Series("e-time-range-1", "m-time-range-1");
        series.addData(new Sample(0, "0"));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMinInISOSaved() throws Exception {
        String date = "1970-01-01T00:00:00.000Z";
        String endDate = "1970-01-01T00:00:00.001Z";
        Series series = new Series("e-time-range-2", "m-time-range-2");
        series.addData(new Sample(date, "0"));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), date, endDate);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeInMSTimeSaved() throws Exception {
        Long time = 1L;
        Long endTime = 2L;
        Series series = new Series("e-time-range-3", "m-time-range-3");
        series.addData(new Sample(time, "1"));

        boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals(new BigDecimal("1"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeInISOTimeSaved() throws Exception {
        final BigDecimal v = new BigDecimal("1");
        Series series = new Series("e-time-range-4", "m-time-range-4");
        series.addData(new Sample("1970-01-01T00:00:00.001Z", v));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), "1970-01-01T00:00:00.001Z", "1970-01-01T00:00:00.002Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals(v, seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInMSSaved() throws Exception {
        final long t = 4294970894999L;
        final BigDecimal v = new BigDecimal("4294970894999");

        Series series = new Series("e-time-range-5", "m-time-range-5");
        series.addData(new Sample(t, v));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals(v, seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInISOSaved() throws Exception {
        String d = "2106-02-07T07:28:14.999Z";
        final BigDecimal v = new BigDecimal("4294970894999");

        Series series = new Series("e-time-range-6", "m-time-range-6");
        series.addData(new Sample(d, v));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2106-02-07T07:28:15.000Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals(v, seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInMSOverflow() throws Exception {
        final long t = 4294970895000L;
        final BigDecimal v = new BigDecimal("4294970895000");

        Series series = new Series("e-time-range-7", "m-time-range-7");
        series.addData(new Sample(t, v));

        assertFalse("Managed to insert series with t out of range", insertSeries(series, 700));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals(0, seriesList.get(0).getData().size());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInISOOverflow() throws Exception {
        String d = "2106-02-07T07:28:15.000Z";
        final BigDecimal v = new BigDecimal("4294970895000");

        Series series = new Series("e-time-range-8", "m-time-range-8");
        series.addData(new Sample(d, v));

        assertFalse("Managed to insert series with t out of range", insertSeries(series, 700));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2106-02-07T07:28:15.001Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals(0, seriesList.get(0).getData().size());
    }
}
