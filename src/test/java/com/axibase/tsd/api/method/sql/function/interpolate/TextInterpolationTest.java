package com.axibase.tsd.api.method.sql.function.interpolate;


import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.model.sql.TimePeriod;
import com.axibase.tsd.api.model.sql.function.interpolate.Boundary;
import com.axibase.tsd.api.model.sql.function.interpolate.FillMode;
import com.axibase.tsd.api.model.sql.function.interpolate.InterpolateFunction;
import com.axibase.tsd.api.model.sql.function.interpolate.InterpolationParams;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.Util.TestNames;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;


public class TextInterpolationTest extends SqlTest {
    private final static String[] TEXTS = {"a", null, "4", "random", "just_text", "stop"};

    public List<Series> multiJoinSeries() {
        List<Series> seriesList = new ArrayList<>();
        String entityName = TestNames.entity();
        Registry.Entity.register(entityName);
        for (int i = 0; i < TEXTS.length; i++) {
            String metricName = TestNames.metric();
            Registry.Metric.register(metricName);

            Sample sample = new TextSample(Mocks.ISO_TIME, TEXTS[i]);

            Series series = new Series();
            series.setEntity(entityName);
            series.setMetric(metricName);

            series.addData(sample);
            seriesList.add(series);
        }
        return seriesList;
    }


    /**
     * #3463
     */
    @Test
    public void testOnlyNullInterpolation() throws Exception {
        Series series = Mocks.series();
        series.setData(Arrays.asList(
                new Sample("2016-06-03T09:23:01.000Z", 1),
                new Sample("2016-06-03T09:23:02.000Z", 2),
                new Sample("2016-06-03T09:23:03.000Z", 3),
                new Sample("2016-06-03T09:23:04.000Z", 4)
        ));
        SeriesMethod.insertSeriesCheck(series);
        String sqlQuery = String.format(
                "SELECT text FROM '%s'%nWHERE datetime BETWEEN '2016-06-03T09:23:00.000Z' AND '2016-06-03T09:23:05.000Z'" +
                        "WITH INTERPOLATE(500 MILLISECOND, AUTO, OUTER, EXTEND)",
                series.getMetric()
        );

        String[][] expectedRows = {
                {"null"}, {"null"}, {"null"}, {"null"}, {"null"}, {"null"}, {"null"},
                {"null"}, {"null"}, {"null"}, {"null"}
        };


        assertSqlQueryRows(expectedRows, sqlQuery);
    }


    @DataProvider(name = "interpolationDataProvider")
    public Object[][] provideInterpolationData() {
        return new Object[][]{
                {
                        Collections.singletonList(
                                new TextSample("2016-06-03T09:23:01.000Z", "text")
                        ),
                        new TimePeriod(
                                "2016-06-03T09:23:00.000Z",
                                "2016-06-03T09:23:02.000Z"
                        ),
                        new InterpolationParams(
                                new Interval(1, TimeUnit.SECOND),
                                InterpolateFunction.AUTO,
                                Boundary.OUTER,
                                FillMode.EXTEND
                        ),
                        Arrays.asList(
                                new TextSample("2016-06-03T09:23:00.000Z", "text"),
                                new TextSample("2016-06-03T09:23:01.000Z", "text"),
                                new TextSample("2016-06-03T09:23:02.000Z", "text")
                        )

                },
                {
                        Arrays.asList(
                                new TextSample("2016-06-03T09:23:01.000Z", "text"),
                                new Sample("2016-06-03T09:23:00.000Z", 1)
                        ),
                        new TimePeriod(
                                "2016-06-03T09:23:00.000Z",
                                "2016-06-03T09:23:02.000Z"
                        ),
                        new InterpolationParams(
                                new Interval(500, TimeUnit.MILLISECOND),
                                InterpolateFunction.AUTO,
                                Boundary.OUTER,
                                FillMode.EXTEND
                        ),
                        Arrays.asList(
                                new TextSample("2016-06-03T09:23:00.000Z", "null"),
                                new TextSample("2016-06-03T09:23:00.500Z", "null"),
                                new TextSample("2016-06-03T09:23:01.000Z", "text"),
                                new TextSample("2016-06-03T09:23:01.500Z", "text"),
                                new TextSample("2016-06-03T09:23:02.000Z", "text")
                        )

                },
                {
                        Arrays.asList(
                                new TextSample("2016-06-03T09:23:01.000Z", "first"),
                                new TextSample("2016-06-03T09:23:02.000Z", "second"),
                                new Sample("2016-06-03T09:23:03.000Z", 3),
                                new TextSample("2016-06-03T09:23:04.000Z", "fourth")
                        ),
                        new TimePeriod(
                                "2016-06-03T09:23:00.000Z",
                                "2016-06-03T09:23:05.000Z"
                        ),
                        new InterpolationParams(
                                new Interval(500, TimeUnit.MILLISECOND),
                                InterpolateFunction.AUTO,
                                Boundary.OUTER,
                                FillMode.EXTEND
                        ),
                        Arrays.asList(
                                new TextSample("2016-06-03T09:23:00.000Z", "first"),
                                new TextSample("2016-06-03T09:23:00.500Z", "first"),
                                new TextSample("2016-06-03T09:23:01.000Z", "first"),
                                new TextSample("2016-06-03T09:23:01.500Z", "first"),
                                new TextSample("2016-06-03T09:23:02.000Z", "second"),
                                new TextSample("2016-06-03T09:23:02.500Z", "second"),
                                new TextSample("2016-06-03T09:23:03.000Z", "null"),
                                new TextSample("2016-06-03T09:23:03.500Z", "null"),
                                new TextSample("2016-06-03T09:23:04.000Z", "fourth"),
                                new TextSample("2016-06-03T09:23:04.500Z", "fourth"),
                                new TextSample("2016-06-03T09:23:05.000Z", "fourth")
                        )

                },
                {
                        Arrays.asList(
                                new Sample("2016-06-03T09:23:01.000Z", 1),
                                new Sample("2016-06-03T09:23:02.000Z", 2),
                                new Sample("2016-06-03T09:23:03.000Z", 3),
                                new Sample("2016-06-03T09:23:04.000Z", 4)
                        ),
                        new TimePeriod(
                                "2016-06-03T09:23:00.000Z",
                                "2016-06-03T09:23:05.000Z"
                        ),
                        new InterpolationParams(
                                new Interval(1, TimeUnit.SECOND),
                                InterpolateFunction.AUTO,
                                Boundary.OUTER,
                                FillMode.EXTEND
                        ),
                        Arrays.asList(
                                new TextSample("2016-06-03T09:23:00.000Z", "null"),
                                new TextSample("2016-06-03T09:23:01.000Z", "null"),
                                new TextSample("2016-06-03T09:23:02.000Z", "null"),
                                new TextSample("2016-06-03T09:23:03.000Z", "null"),
                                new TextSample("2016-06-03T09:23:04.000Z", "null"),
                                new TextSample("2016-06-03T09:23:05.000Z", "null")
                        )

                }
        };
    }

    /**
     * #3463
     */
    @Test(dataProvider = "interpolationDataProvider")
    public void testInterpolation(Collection<Sample> sourcePoints,
                                  TimePeriod period,
                                  InterpolationParams interpolationParams,
                                  List<Sample> resultPoints) throws Exception {
        Series series = new Series(TestNames.entity(), TestNames.metric());
        series.setData(sourcePoints);
        SeriesMethod.insertSeriesCheck(series);

        String sqlQuery = String.format(
                "SELECT datetime, text FROM '%s'%nWHERE time BETWEEN %s AND %s%nWITH INTERPOLATE(%s)",
                series.getMetric(), period.getStartTime(), period.getEndTime(), interpolationParams
        );

        String[][] expectedRows = textSamplesToStringRows(resultPoints);
        String assertMessage = String.format(
                "Wrong interpolated points.%n%nSource points: %s%n%nTime Period: %s%n%nInterpolation Params: %s%n",
                sourcePoints, period, interpolationParams
        );
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }

    /**
     * #3463
     */
    @Test
    public void testMultiJoinInterpolation() throws Exception {
        List<Series> seriesList = multiJoinSeries();
        SeriesMethod.insertSeriesCheck(seriesList);
        String sqlQuery = buildMultiJoinQuery(seriesList);
        String[][] expectedRows = {
                TEXTS,
                TEXTS,
                TEXTS
        };
        String assertMessage = String.format(
                "Incorrect Text value of interpolated join series.%nQuery: %s",
                sqlQuery
        );

        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }


    private String buildMultiJoinQuery(List<Series> seriesList) {
        StringBuilder queryBuilder = new StringBuilder("SELECT ");
        int i = 0;
        int seriesCount = seriesList.size();
        for (Series series : seriesList) {
            i++;
            if (seriesCount > i) {
                queryBuilder.append(String.format("t%d.text, ", i));
            } else {
                queryBuilder.append(String.format("t%d.text", i));
            }
        }
        queryBuilder.append(" FROM ");
        i = 0;
        for (Series series : seriesList) {
            String fromTable = String.format(" '%s' t%d%n", series.getMetric(), i + 1);
            if (i == 0) {
                queryBuilder.append(fromTable);
            } else {
                queryBuilder.append(String.format("\tJOIN %s", fromTable));
            }
            i++;
        }
        Long delta = 60000L;
        Long startTime = Mocks.MILLS_TIME - delta;
        Long endTime = Mocks.MILLS_TIME + delta;
        queryBuilder.append(String.format("WHERE t1.datetime BETWEEN '%s' AND '%s' %n", Util.ISOFormat(startTime), Util.ISOFormat(endTime)));
        queryBuilder.append("WITH INTERPOLATE(1 MINUTE, AUTO, OUTER, EXTEND, START_TIME)");
        return queryBuilder.toString();
    }

    private String[][] textSamplesToStringRows(List<Sample> samples) {
        String[][] result = new String[samples.size()][2];
        int i = 0;
        for (Sample sample : samples) {
            result[i][0] = sample.getD();
            result[i][1] = sample.getText();
            i++;
        }
        return result;
    }
}
