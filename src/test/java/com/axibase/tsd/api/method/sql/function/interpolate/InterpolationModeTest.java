package com.axibase.tsd.api.method.sql.function.interpolate;


import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.function.interpolate.InterpolateFunction;
import com.axibase.tsd.api.util.Util.TestNames;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class InterpolationModeTest extends SqlTest {
    private final List<Sample> DEFAULT_SAMPLES = Arrays.asList(
            new Sample("2016-06-19T11:00:00.000Z", "1"),
            new Sample("2016-06-19T11:02:00.000Z", "2"),
            new Sample("2016-06-19T11:06:00.000Z", "4"),
            new Sample("2016-06-19T11:14:00.000Z", "8")
    );
    private Series DEFAULT_SERIES;

    @BeforeClass
    public void prepareData() throws Exception {
        DEFAULT_SERIES = new Series(TestNames.entity(), TestNames.metric());
        DEFAULT_SERIES.setData(DEFAULT_SAMPLES);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(DEFAULT_SERIES));
    }

    @DataProvider(name = "interpolationModeResultsProvider")
    public Object[][] provideInterpolationModeResults() {

        return new Object[][]{
                {
                        new Interval(2, TimeUnit.MINUTE),
                        InterpolateFunction.LINEAR,
                        "2016-06-19T10:00:00.000Z",
                        "2016-06-19T11:16:00.000Z",
                        Arrays.asList(
                                new Sample("2016-06-19T11:00:00.000Z", "1"),
                                new Sample("2016-06-19T11:02:00.000Z", "2"),
                                new Sample("2016-06-19T11:04:00.000Z", "3"),
                                new Sample("2016-06-19T11:06:00.000Z", "4"),
                                new Sample("2016-06-19T11:08:00.000Z", "5"),
                                new Sample("2016-06-19T11:10:00.000Z", "6"),
                                new Sample("2016-06-19T11:12:00.000Z", "7"),
                                new Sample("2016-06-19T11:14:00.000Z", "8")
                        )
                },
                {
                        new Interval(2, TimeUnit.MINUTE),
                        InterpolateFunction.LINEAR,
                        "2016-06-19T11:04:00.000Z",
                        "2016-06-19T11:08:00.000Z",
                        Arrays.asList(
                                new Sample("2016-06-19T11:06:00.000Z", "4"),
                                new Sample("2016-06-19T11:08:00.000Z", "5")
                        )
                }
        };
    }

    @Test(dataProvider = "interpolationModeResultsProvider")
    public void testSpecifiedInterpolationMode(Interval interval,
                                               InterpolateFunction interpolateFunction,
                                               String startDate,
                                               String endDate,
                                               List<Sample> results) throws Exception {
        String sqlQuery = String.format("SELECT datetime, value FROM '%s'%nWHERE datetime >= '%s' AND datetime < '%s'%n" +
                        "WITH INTERPOLATE(%s, %s)",
                DEFAULT_SERIES.getMetric(), startDate, endDate, interval, interpolateFunction
        );

        List<List<String>> expectedRows = rowsFromSamples(results);

        String assertMessage = String.format("Wrong interpolated sample in the %s mode.%n\tSource points: %s%n" +
                        "\tStart date: %s%n\tEnd Date: %s%n\tPeriod: %s%n",
                interpolateFunction.toString(), DEFAULT_SAMPLES.toString(), startDate, endDate, interval.toString()
        );

        assertSqlQueryRows(sqlQuery, expectedRows, assertMessage);

    }

    private List<List<String>> rowsFromSamples(List<Sample> results) {
        List<List<String>> resultRows = new ArrayList<>();
        for (Sample s : results) {
            resultRows.add(Arrays.asList(s.getD(), s.getV().toString()));
        }
        return resultRows;
    }
}
