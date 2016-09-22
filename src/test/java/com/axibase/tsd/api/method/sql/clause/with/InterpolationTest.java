package com.axibase.tsd.api.method.sql.clause.with;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.annotations.AtsdRule;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.ProductVersion;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InterpolationTest extends SqlTest {
    private static LinearInterpolator interpolator = new LinearInterpolator();
    private static TestNameGenerator testNameGenerator = testNames(InterpolationTest.class);


    @BeforeTest
    public static void prepareData() throws Exception {
    }

    private static Double interpolate(Double x1, Double y1, Double x2, Double y2, Double time) {
        return y1 + (y2 - y1) / (x2 - x1) * (time - x1);
    }

    public static void main(String[] args) {
        double d = interpolate((double) Util.parseDate("2016-08-24T23:22:09.000Z").getTime(), 5692.0d, (double) Util.parseDate("2016-08-25T14:17:07.000Z").getTime(), 6544.0d, (double) Util.parseDate("2016-08-24T23:23:00.000Z").getTime());
        System.out.println(d);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testNoneInterpolationRawIntervalInRequestInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(series.getData()),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE)",
                testMetricName);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testNoneInterpolationRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34"),
                new Sample("2016-06-19T11:15:03.000Z", "1")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();

        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testNoneInterpolationEndOfRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34"),
                new Sample("2016-06-19T11:15:03.000Z", "1")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testNoneInterpolationBeginOfRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testNoneInterpolationWithoutData() throws Exception {
        //Create data for test
        String testMetricName = testNameGenerator.getMetricName();
        MetricMethod.createOrReplaceMetric(new Metric(testMetricName));
        //Generate expected expected rows
        String[][] expectedRows = {};
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testPriorInterpolationRawIntervalInRequestInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(series.getData()),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testPriorInterpolationRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34"),
                new Sample("2016-06-19T11:15:03.000Z", "1")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();

        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testPriorInterpolationEndOfRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34"),
                new Sample("2016-06-19T11:15:03.000Z", "1")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testPriorInterpolationBeginOfRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "1"));//Add prior value, out of raw values
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));//Add prior value, out of raw values

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testPriorInterpolationWithoutData() throws Exception {
        //Create data for test
        String testMetricName = testNameGenerator.getMetricName();
        MetricMethod.createOrReplaceMetric(new Metric(testMetricName));
        //Generate expected expected rows
        String[][] expectedRows = {};
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    public void testLinearInterpolationRawIntervalInRequestInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(series.getData()),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testLinearInterpolationRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34"),
                new Sample("2016-06-19T11:15:03.000Z", "1")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();

        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testLinearInterpolationEndOfRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34"),
                new Sample("2016-06-19T11:15:03.000Z", "1")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(interpolatingNodes),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testLinearInterpolationBeginOfRequestIntervalInRawInterval() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-06-19T11:08:00.000Z").getTime();
        Long endTime = Util.parseDate("2016-06-19T11:15:00.000Z").getTime();
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(series.getData()),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testLinearInterpolationWithoutData() throws Exception {
        //Create data for test
        String testMetricName = testNameGenerator.getMetricName();
        MetricMethod.createOrReplaceMetric(new Metric(testMetricName));
        //Generate expected expected rows
        String[][] expectedRows = {};
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testLinearInterpolation() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        MetricMethod.deleteMetric(testMetricName);
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-08-24T23:21:59.000Z", "5499"),
                new Sample("2016-08-24T23:22:04.000Z", "5499"),
                new Sample("2016-08-24T23:22:09.000Z", "5692"),
                new Sample("2016-08-25T14:17:07.000Z", "6544"),
                new Sample("2016-08-25T14:18:01.000Z", "6955")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-08-24T23:22:00Z").getTime();
        Long endTime = Util.parseDate("2016-08-25T14:18:00.00Z").getTime();
        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(series.getData()),
                startTime,
                endTime,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE  datetime BETWEEN '2016-08-24T23:22:00Z' AND '2016-08-25T14:18:00Z'" +
                        "%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    @AtsdRule(version = ProductVersion.ENTERPRISE)
    public void testLinearInterpolation1() throws Exception {
        //Create data for test
        String testEntityName = testNameGenerator.getEntityName();
        String testMetricName = testNameGenerator.getMetricName();
        Series series = new Series(testEntityName, testMetricName);
        series.setData(Arrays.asList(
                new Sample("2016-08-24T23:21:59.000Z", "100"),
                new Sample("2016-08-24T23:22:01.000Z", "200")
                )
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        //Generate expected expected rows
        Long startTime = Util.parseDate("2016-08-24T23:21:59.000Z").getTime();
        Long endTime = Util.parseDate("2016-08-24T23:22:01.000Z").getTime();
        List<Sample> interpolatingNodes = Util.filterSamples(
                series.getData(),
                startTime,
                endTime
        );
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(series.getData()),
                startTime,
                endTime + 1,
                60000L
        );
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE  datetime BETWEEN '2016-08-24T23:21:59.000Z' AND '2016-08-24T23:22:01.000Z'" +
                        "%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    private List<Sample> interpolatedValues(PolynomialSplineFunction inteterpolateFunction, Long startTime, Long endTime, Long period) {
        List<Sample> result = new ArrayList<>();
        double[] knots = inteterpolateFunction.getKnots();
        double lowLimit = knots[0];
        double upLimit = knots[knots.length - 1];
        for (Long t = startTime; t < endTime; t += period) {
            if (t >= lowLimit && t <= upLimit) {
                Double value = inteterpolateFunction.value(t);
                result.add(new Sample(
                                Util.ISOFormat(t),
                                value.toString()
                        )
                );
            }
        }
        return result;
    }

    private PolynomialSplineFunction interpolatingFunction(List<Sample> samples) {
        int size = samples.size();
        double[] x = new double[size];
        double[] y = new double[size];
        for (int i = 0; i < size; i++) {
            Sample sample = samples.get(i);
            x[i] = Util.parseDate(sample.getD()).getTime();
            y[i] = sample.getV().doubleValue();
        }
        return interpolator.interpolate(x, y);
    }
}
