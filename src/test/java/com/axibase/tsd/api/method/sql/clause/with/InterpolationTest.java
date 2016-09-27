package com.axibase.tsd.api.method.sql.clause.with;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InterpolationTest extends SqlTest {
    private static LinearInterpolator interpolator = new LinearInterpolator();
    private static TestNameGenerator testNameGenerator = testNames(InterpolationTest.class);

    @Test
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
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));//Add first value, because it not interpolated
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
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
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "1"));//Add first value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
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
        expectedRows.add(Arrays.asList("2016-06-19T11:08:00.000Z", "4"));//Add prior value, because it not interpolated
        expectedRows.addAll(Util.sampleListToTableView(interpolatedSamples));
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));//Add prior value
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
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
        Long endTime = Util.parseDate("2016-08-25T14:18:00Z").getTime();
        List<Sample> interpolatedSamples = interpolatedValues(
                interpolatingFunction(series.getData()),
                startTime,
                endTime + 1,
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
    public void testLinearInterpolationManualCalculation() throws Exception {
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
        String[][] expectedRows = {
                {"2016-08-24T23:21:59.000Z", "100"},
                {"2016-08-24T23:22:00.000Z", "150"},
                {"2016-08-24T23:22:01.000Z", "200"},
        };
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE  datetime " +
                        "BETWEEN '2016-08-24T23:21:59.000Z' AND '2016-08-24T23:22:01.000Z'" +
                        "%nWITH INTERPOLATE(1 SECOND, LINEAR)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    public void testExtendedInterpolationRawIntervalInRequestInterval() throws Exception {
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
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));//Add last value as previous raw
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, EXTEND)",
                testMetricName);

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    public void testExtendedInterpolationRequestIntervalInRawInterval() throws Exception {
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
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));//Add previous raw value, because it not interpolated

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, EXTEND)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    public void testExtendedInterpolationEndOfRequestIntervalInRawInterval() throws Exception {
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
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));//Add last value to extend, because it not interpolated

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, EXTEND)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    public void testExtendedInterpolationBeginOfRequestIntervalInRawInterval() throws Exception {
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
        expectedRows.add(Arrays.asList("2016-06-19T11:14:00.000Z", "34"));//Add first value, because it not interpolated

        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, EXTEND)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    public void testExtendedInterpolationWithoutData() throws Exception {
        //Create data for test
        String testMetricName = testNameGenerator.getMetricName();
        MetricMethod.createOrReplaceMetric(new Metric(testMetricName));
        //Generate expected expected rows
        String[][] expectedRows = {};
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, EXTEND)",
                testMetricName);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    private void testLinearJoin() throws Exception {
        //Create data for test
        String testMetricName1 = testNameGenerator.getMetricName();
        String testMetricName2 = testNameGenerator.getMetricName();
        String testEntityName = testNameGenerator.getEntityName();
        Series series1 = new Series();
        series1.setMetric(testMetricName1);
        series1.setEntity(testEntityName);
        series1.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
        ));
        Series series2 = new Series();
        series2.setMetric(testMetricName2);
        series2.setEntity(testEntityName);
        series2.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "2"),
                new Sample("2016-06-19T11:08:03.000Z", "3"),
                new Sample("2016-06-19T11:09:15.000Z", "4"),
                new Sample("2016-06-19T11:13:33.000Z", "7")
                )
        );
        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
        //Generate expected expected rows
        String[][] expectedRows = {
                {"2016-06-19T11:08:00.000Z", "3.98125", "2.99375"},
                {"2016-06-19T11:09:00.000Z", "4.791666666666667", "3.7916666666666665"},
                {"2016-06-19T11:10:00.000Z", "10.058139534883722", "4.523255813953488"},
                {"2016-06-19T11:11:00.000Z", "16.80232558139535", "5.220930232558139"},
                {"2016-06-19T11:12:00.000Z", "23.546511627906977", "5.9186046511627906"},
                {"2016-06-19T11:13:00.000Z", "30.290697674418606", "6.616279069767442"}

        };

        //Query data
        String sqlQuery = String.format("SELECT t1.datetime as 'datetime', t1.value, t2.value FROM '%s' t1%nJOIN '%s' t2\n" +
                        "WHERE t1.datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND t1.datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, LINEAR)",
                testMetricName1, testMetricName2);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    private void testNoneJoin() throws Exception {
        //Create data for test
        String testMetricName1 = testNameGenerator.getMetricName();
        String testMetricName2 = testNameGenerator.getMetricName();
        String testEntityName = testNameGenerator.getEntityName();
        Series series1 = new Series();
        series1.setMetric(testMetricName1);
        series1.setEntity(testEntityName);
        series1.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
        ));
        Series series2 = new Series();
        series2.setMetric(testMetricName2);
        series2.setEntity(testEntityName);
        series2.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "2"),
                new Sample("2016-06-19T11:08:03.000Z", "3"),
                new Sample("2016-06-19T11:09:15.000Z", "4"),
                new Sample("2016-06-19T11:13:33.000Z", "7")
                )
        );
        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
        //Generate expected expected rows
        String[][] expectedRows = {
                {"2016-06-19T11:08:00.000Z", "4", "3"},
                {"2016-06-19T11:09:00.000Z", "4.791666666666667", "3.7916666666666665"},
                {"2016-06-19T11:10:00.000Z", "10.058139534883722", "4.523255813953488"},
                {"2016-06-19T11:11:00.000Z", "16.80232558139535", "5.220930232558139"},
                {"2016-06-19T11:12:00.000Z", "23.546511627906977", "5.9186046511627906"},
                {"2016-06-19T11:13:00.000Z", "30.290697674418606", "6.616279069767442"}

        };

        //Query data
        String sqlQuery = String.format("SELECT t1.datetime as 'datetime', t1.value, t2.value FROM '%s' t1%nJOIN '%s' t2%n" +
                        "WHERE t1.datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND t1.datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE)",
                testMetricName1, testMetricName2);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    private void testExtendJoin() throws Exception {
        //Create data for test
        String testMetricName1 = testNameGenerator.getMetricName();
        String testMetricName2 = testNameGenerator.getMetricName();
        String testEntityName = testNameGenerator.getEntityName();
        Series series1 = new Series();
        series1.setMetric(testMetricName1);
        series1.setEntity(testEntityName);
        series1.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
        ));
        Series series2 = new Series();
        series2.setMetric(testMetricName2);
        series2.setEntity(testEntityName);
        series2.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "2"),
                new Sample("2016-06-19T11:08:03.000Z", "3"),
                new Sample("2016-06-19T11:09:15.000Z", "4"),
                new Sample("2016-06-19T11:13:33.000Z", "7")
                )
        );
        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
        //Generate expected expected rows
        String[][] expectedRows = {
                {"2016-06-19T11:08:00.000Z", "4", "3"},
                {"2016-06-19T11:09:00.000Z", "4.791666666666667", "3.7916666666666665"},
                {"2016-06-19T11:10:00.000Z", "10.058139534883722", "4.523255813953488"},
                {"2016-06-19T11:11:00.000Z", "16.80232558139535", "5.220930232558139"},
                {"2016-06-19T11:12:00.000Z", "23.546511627906977", "5.9186046511627906"},
                {"2016-06-19T11:13:00.000Z", "30.290697674418606", "6.616279069767442"},
                {"2016-06-19T11:14:00.000Z", "34", "7"}

        };

        //Query data
        String sqlQuery = String.format("SELECT t1.datetime as 'datetime', t1.value, t2.value FROM '%s' t1%nJOIN '%s' t2%n" +
                        "WHERE t1.datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND t1.datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, EXTEND)",
                testMetricName1, testMetricName2);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    private void testPriorJoin() throws Exception {
        //Create data for test
        String testMetricName1 = testNameGenerator.getMetricName();
        String testMetricName2 = testNameGenerator.getMetricName();
        String testEntityName = testNameGenerator.getEntityName();
        Series series1 = new Series();
        series1.setMetric(testMetricName1);
        series1.setEntity(testEntityName);
        series1.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "1"),
                new Sample("2016-06-19T11:08:03.000Z", "4"),
                new Sample("2016-06-19T11:09:15.000Z", "5"),
                new Sample("2016-06-19T11:13:33.000Z", "34")
        ));
        Series series2 = new Series();
        series2.setMetric(testMetricName2);
        series2.setEntity(testEntityName);
        series2.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:03.000Z", "2"),
                new Sample("2016-06-19T11:08:03.000Z", "3"),
                new Sample("2016-06-19T11:09:15.000Z", "4"),
                new Sample("2016-06-19T11:13:33.000Z", "7")
                )
        );
        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
        //Generate expected expected rows
        String[][] expectedRows = {
                {"2016-06-19T11:08:00.000Z", "1", "2"},
                {"2016-06-19T11:09:00.000Z", "4.791666666666667", "3.7916666666666665"},
                {"2016-06-19T11:10:00.000Z", "10.058139534883722", "4.523255813953488"},
                {"2016-06-19T11:11:00.000Z", "16.80232558139535", "5.220930232558139"},
                {"2016-06-19T11:12:00.000Z", "23.546511627906977", "5.9186046511627906"},
                {"2016-06-19T11:13:00.000Z", "30.290697674418606", "6.616279069767442"},
                {"2016-06-19T11:14:00.000Z", "34", "7"}

        };

        //Query data
        String sqlQuery = String.format("SELECT t1.datetime as 'datetime', t1.value, t2.value FROM '%s' t1%nJOIN '%s' t2%n" +
                        "WHERE t1.datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND t1.datetime < '2016-06-19T11:15:00.000Z'%nWITH INTERPOLATE(1 MINUTE, PRIOR)",
                testMetricName1, testMetricName2);
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
        //Assert
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Test
    private void testFiltering() throws Exception {
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
        String[][] expectedRows = {
                {"2016-06-19T11:08:00.000Z", "4"},
                {"2016-06-19T11:09:00.000Z", "4.791666666666667"}
        };
        //Query data
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '2016-06-19T11:08:00.000Z' " +
                        "AND datetime < '2016-06-19T11:15:00.000Z' AND value < 6%nWITH INTERPOLATE(1 MINUTE, EXTEND)",
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
