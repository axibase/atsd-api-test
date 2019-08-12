package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.SeriesSettings;
import com.axibase.tsd.api.model.series.query.transformation.Transformation;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.evaluate.Evaluate;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Forecast;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Horizon;
import com.axibase.tsd.api.model.series.query.transformation.forecast.SSASettings;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.Interpolate;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.InterpolateFunction;
import com.axibase.tsd.api.util.CommonAssertions;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.axibase.tsd.api.model.series.SeriesType.HISTORY;
import static com.axibase.tsd.api.model.series.SeriesType.RECONSTRUCTED;
import static com.axibase.tsd.api.model.series.query.transformation.Transformation.*;
import static com.axibase.tsd.api.model.series.query.transformation.group.GroupType.*;
import static com.axibase.tsd.api.util.CommonAssertions.checkValues;
import static org.testng.Assert.assertEquals;


/**
 * Test that evaluation is properly built in series processing pipeline requested in API series query.
 * Test that input series of the evaluation stage
 * are properly preprocessed by pre-evaluation series transformations.
 *
 * We need this test because ATSD uses special code to organize series pre-/post- processing
 * before/after evaluation of user defined expression.
 */
public class PreEvaluationTest extends SeriesMethod {

    /*
     Series under test, i.e. raw input series.

     Date: 2019-07-01

     series | series key        |          |          |          |          |          |
            | metric:entity:tags| 00:00:00 | 00:05:00 | 00:09:00 | 00:15:00 | 00:19:00 |
     -------|-------------------|----------|----------|----------|----------|----------|
          0 | m1:e1:            |       1  |       1  |       1  |       1  |       1  |
          1 | m1:e1:tn=tv1      |       2  |       2  |       2  |       2  |       2  |
          2 | m1:e1:tn=tv2      |       3  |       3  |       3  |       3  |       3  |
          3 | m1:e2:            |       4  |       4  |       4  |       4  |       4  |
          4 | m1:e2:tn=tv1      |       5  |       5  |       5  |       5  |       5  |
          5 | m1:e2:tn=tv2      |       6  |       6  |       6  |       6  |       6  |
          6 | m2:e1:tn=tv1      |       7  |       7  |       7  |       7  |       7  |
          7 | m2:e1:tn=tv2      |       8  |       8  |       8  |       8  |       8  |
     */

    private final String m1 = "a_" + Mocks.metric(); // Use prefix to fix lexicographic order.
    private final String m2 = "b_" + Mocks.metric();
    private final String e1 = "a_" + Mocks.entity();
    private final String e2 = "b_" + Mocks.entity();
    private final List<Series> series = new ArrayList<>();

    /** Invariant part of the test query. Each test method appends required transformations to it.
    [{
        "startDate": "2019-07-01T00:00:00Z",
        "endDate":   "2019-07-02T00:00:00Z",
        "series": [{
            "name": "A",
            "metric": "m1",
            "entity": "*"
            }, {
            "name": "B",
            "metric": "m2",
            "entity": "*"
        }],
        "evaluate": {"expression": "A.union(B)"}
    }]
    */
    private SeriesQuery baseQuery_1;

    /** Anther form of base query - define one of requested series collections on top level.
     [{
         "startDate": "2019-07-01T00:00:00Z",
         "endDate":   "2019-07-02T00:00:00Z",
         "name": "A",
         "metric": "m1",
         "entity": "*"
         "series": [{
             "name": "B",
             "metric": "m2",
             "entity": "*"
         }],
         "evaluate": { "expression": "A.union(B)"}
     }]
     */
    private SeriesQuery baseQuery_2;

    @DataProvider()
    public Object[][] queriesProvider() {
        return new Object[][] {{baseQuery_1}, {baseQuery_2}};
    }


    @BeforeClass
    public void prepare() throws Exception {
        series.add(new Series(e1, m1).addSamples(generateSamples(1)));
        String tn = "tag-name";
        String tv1 = "tag-value-1";
        series.add(new Series(e1, m1, tn, tv1).addSamples(generateSamples(2)));
        String tv2 = "tag-value-2";
        series.add(new Series(e1, m1, tn, tv2).addSamples(generateSamples(3)));
        series.add(new Series(e2, m1).addSamples(generateSamples(4)));
        series.add(new Series(e2, m1, tn, tv1).addSamples(generateSamples(5)));
        series.add(new Series(e2, m1, tn, tv2).addSamples(generateSamples(6)));
        series.add(new Series(e1, m2, tn, tv1).addSamples(generateSamples(7)));
        series.add(new Series(e1, m2, tn, tv2).addSamples(generateSamples(8)));
        insertSeriesCheck(series);

        String startDate = "2019-07-01T00:00:00Z";
        String endDate = "2019-07-02T00:00:00Z";
        Evaluate evaluate = new Evaluate("A.union(B)");
        baseQuery_1 = new SeriesQuery(null, null, startDate, endDate)
                .addSeries(SeriesSettings.of("A", m1, "*"))
                .addSeries(SeriesSettings.of("B", m2, "*"))
                .setEvaluate(evaluate);
        baseQuery_2 = new SeriesQuery("*", m1, startDate, endDate)
                .withName("A")
                .addSeries(SeriesSettings.of("B", m2, "*"))
                .setEvaluate(evaluate);
    }


    @Test(description = "Test that all input series are collected for evaluation.", dataProvider = "queriesProvider")
    public void testBaseQueries(SeriesQuery baseQuery) {
        List<Series> seriesList = querySeriesAsList(baseQuery);
        CommonAssertions.assertEqualLists(seriesList, series);
    }

    @Test(description = "Test single function aggregation.", dataProvider = "queriesProvider")
    public void testSingleAggregation(SeriesQuery baseQuery) {
        SeriesQuery query = baseQuery
                .withAggregate(new Aggregate(AggregationType.COUNT, period(10)))
                .withTransformationOrder(Arrays.asList(AGGREGATE, EVALUATE));
        List<Series> actualList = querySeriesAsList(query);
        Collections.sort(actualList);
        assertEquals(actualList.size(), series.size());
        Series firstSeries = actualList.get(0);
        assertEquals(firstSeries.getMetric(), m1);
        assertEquals(firstSeries.getEntity(), e1);
        actualList.forEach(s -> checkValues(s, "3", "2"));
    }

    @Test(description = "Test aggregation with 2 functions.", dataProvider = "queriesProvider")
    public void testAggregation(SeriesQuery baseQuery) {
        Aggregate aggregationSettings = new Aggregate(AggregationType.AVG, period(10)).addType(AggregationType.SUM);
        SeriesQuery query = baseQuery
                .withAggregate(aggregationSettings)
                .withTransformationOrder(Arrays.asList(AGGREGATE, EVALUATE));
        List<Series> actualList = querySeriesAsList(query);
        assertEquals(actualList.size(), 2 * series.size());
        CommonAssertions.assertSeriesSize(actualList, 2);
        CommonAssertions.checkValues(actualList, new String[]{"3", "2"}, new String[]{"6", "4"}, new String[]{"9", "6"},
                new String[]{"12", "8"}, new String[]{"15", "10"}, new String[]{"18", "12"}, new String[]{"21", "14"},
                new String[]{"24", "16"}, array("1", 2), array("2", 2), array("3", 2), array("4", 2), array("5", 2),
                array("6", 2), array("7", 2), array("8", 2)
        );
    }

    @Test(description = "Test grouping.", dataProvider = "queriesProvider")
    public void testGrouping(SeriesQuery baseQuery) {
        SeriesQuery query = baseQuery
                .withGroup(new Group(SUM))
                .withTransformationOrder(Arrays.asList(GROUP, EVALUATE));
        List<Series> actualList = querySeriesAsList(query);
        assertEquals(actualList.size(), 2);
        CommonAssertions.assertSeriesSize(actualList, 5);
        CommonAssertions.checkValues(actualList, array("21", 5), array("15", 5));
    }

    @Test(description = "Test group by entity.", dataProvider = "queriesProvider")
    public void testGroupByEntity(SeriesQuery baseQuery) {
        Group groupSettings = new Group()
                .addType(SUM)
                .addType(MIN)
                .setGroupByEntityAndTags(Collections.emptyList());
        SeriesQuery query = baseQuery
                .withGroup(groupSettings)
                .withTransformationOrder(Arrays.asList(GROUP, EVALUATE));
        List<Series> actualList = querySeriesAsList(query);
        assertEquals(actualList.size(), 6);
        CommonAssertions.assertSeriesSize(actualList, 5);
        CommonAssertions.checkValues(actualList, array("6", 5), array("15", 5), array("1", 5), array("4", 5), array("7", 5));
    }

    @Test(description = "Test forecast.", dataProvider = "queriesProvider")
    public void testForecasting(SeriesQuery baseQuery) {
        SeriesQuery query = baseQuery
                .withInterpolate(interpolationSettings(1))
                .withForecast(forecastSettings())
                .withTransformationOrder(Arrays.asList(INTERPOLATE, Transformation.FORECAST, EVALUATE));
        List<Series> actualList = querySeriesAsList(query);
        assertEquals(actualList.size(), 3 * series.size());
        String[] values = {"1", "2", "3", "4", "5", "6", "7", "8"};
        checkForecasts(actualList, HISTORY, 20, values);
        checkForecasts(actualList, RECONSTRUCTED, 20, values);
        checkForecasts(actualList, SeriesType.FORECAST, 7, values);
    }

    @Test(description = "Test forecast of grouped series.", dataProvider = "queriesProvider")
    public void testGroupThenForecast(SeriesQuery baseQuery) {
        Group groupSettings = new Group().addType(SUM).addType(MAX).setPeriod(period(2));
        SeriesQuery query = baseQuery
                .withInterpolate(interpolationSettings(1))
                .withGroup(groupSettings)
                .withForecast(forecastSettings())
                .withTransformationOrder(Arrays.asList(INTERPOLATE, GROUP, Transformation.FORECAST, EVALUATE));
        List<Series> actualList = querySeriesAsList(query);
        assertEquals(actualList.size(), 12);
        String[] values = {"6", "8", "30", "42"};
        checkForecasts(actualList, HISTORY, 10, values);
        checkForecasts(actualList, RECONSTRUCTED, 10, values);
        checkForecasts(actualList, SeriesType.FORECAST, 7, values);
    }

    private Interpolate interpolationSettings(int minutes) {
        return new Interpolate(InterpolateFunction.LINEAR, period(minutes));
    }

    private Forecast forecastSettings() {
        return new Forecast()
                .setSsa(new SSASettings())
                .setHorizon(new Horizon().setLength(7))
                .include(HISTORY)
                .include(SeriesType.FORECAST)
                .include(RECONSTRUCTED);
    }

    private Period period(int minutes) {
        return new Period(minutes, TimeUnit.MINUTE, PeriodAlignment.CALENDAR, "UTC");
    }

    /** Check that series of specified type from the list have expected values and samples count. */
    private void checkForecasts(List<Series> actualList, SeriesType type, int count, String... values) {
        List<Series> seriesToCheck = actualList.stream()
                .filter(series -> series.getType() == type)
                .collect(Collectors.toList());
        for (String value : values) {
            CommonAssertions.checkValues(seriesToCheck, array(value, count));
        }
    }

    private String[] array(String value, int length) {
        String[] arr = new String[length];
        Arrays.fill(arr, value);
        return arr;
    }

    /**
     * Generate samples all with the same value, and following timestamps:
     * 2019-07-01T00:00:00Z
     * 2019-07-01T00:05:00Z
     * 2019-07-01T00:09:00Z
     * 2019-07-01T00:15:00Z
     * 2019-07-01T00:19:00Z
     */
    private List<Sample> generateSamples(int value) {
        BigDecimal number = BigDecimal.valueOf(value);
        return Arrays.asList(
                Sample.ofDateDecimal("2019-07-01T00:00:00Z", number),
                Sample.ofDateDecimal("2019-07-01T00:05:00Z", number),
                Sample.ofDateDecimal("2019-07-01T00:09:00Z", number),
                Sample.ofDateDecimal("2019-07-01T00:15:00Z", number),
                Sample.ofDateDecimal("2019-07-01T00:19:00Z", number)
        );
    }

}
