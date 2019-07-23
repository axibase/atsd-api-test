package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.checks.SeriesQueryDataSizeCheck;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Check reading of stored forecasts.
 * Several combinations of tags and exactMatch parameter are tested.
 */
public class SeriesQueryReadStoredForecastTest extends SeriesMethod {
    private static final String START_DATE = "2019-04-20T00:00:00Z";
    private static final String END_DATE = "2019-04-21T00:00:00Z";
    private static final String METRIC = Mocks.metric();
    private static final String ENTITY = Mocks.entity();
    private static final String TAG_NAME_1 = "tag-name-1";
    private static final String TAG_VALUE_1 = "tag-value-1";
    private static final String TAG_NAME_2 = "tag-name-2";
    private static final String TAG_VALUE_2 = "tag-value-2";
    private static final String FORECAST_NAME = "forecast";

    private static final Series SERIES_1 = new Series(ENTITY, METRIC);
    private static final Series SERIES_2 = new Series(ENTITY, METRIC, TAG_NAME_1, TAG_VALUE_1);
    private static final Series SERIES_3 = new Series(ENTITY, METRIC, TAG_NAME_1, TAG_VALUE_2);
    private static final Series SERIES_4 = new Series(ENTITY, METRIC, TAG_NAME_2, TAG_VALUE_1);
    private static final Series SERIES_5 = new Series(ENTITY, METRIC, TAG_NAME_2, TAG_VALUE_2);
    private static final Series SERIES_6 = new Series(ENTITY, METRIC, TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_2);
    private static final Series SERIES_7 = new Series(ENTITY, METRIC, TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, TAG_VALUE_1);
    private static final Series SERIES_8 = new Series(ENTITY, METRIC, TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_1);
    private static final Series SERIES_9 = new Series(ENTITY, METRIC, TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, TAG_VALUE_2);

    private static final SeriesQuery QUERY = new SeriesQuery(ENTITY, METRIC, START_DATE, END_DATE)
            .setForecastName(FORECAST_NAME)
            .setType(SeriesType.FORECAST)
            .setTimeFormat("iso");

    @BeforeClass
    private void prepareData() throws Exception {
        List<Series> seriesList = Arrays.asList(SERIES_1, SERIES_2, SERIES_3, SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9);
        Sample sample = Sample.ofDateInteger(START_DATE, 1);
        for (Series series : seriesList) {
            series
                    .setType(SeriesType.FORECAST)
                    .setForecastName(FORECAST_NAME)
                    .addSamples(sample);
        }
        insertSeriesCheck(seriesList, new SeriesQueryDataSizeCheck(
                new SeriesQuery(ENTITY, METRIC, START_DATE, END_DATE)
                        .setType(SeriesType.FORECAST)
                        .setForecastName(FORECAST_NAME), 9));
    }

    @DataProvider(name = "settings_and_expected_series_data", parallel = true)
    Object[][] settingsAndExpectedSeriesData() {
        return new Object[][]{
                {null, true, new Series[]{SERIES_1}},
                {null, false, new Series[]{SERIES_1, SERIES_2, SERIES_3, SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {ImmutableMap.of(), true, new Series[]{SERIES_1}},
                {ImmutableMap.of(), false, new Series[]{SERIES_1, SERIES_2, SERIES_3, SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1), true, new Series[]{SERIES_2}},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1), false, new Series[]{SERIES_2, SERIES_6, SERIES_8}},

                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_2), true, new Series[]{SERIES_3}},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_2), false, new Series[]{SERIES_3, SERIES_7, SERIES_9}},

                {ImmutableMap.of(TAG_NAME_1, "*"), true, new Series[]{SERIES_2, SERIES_3}},
                {ImmutableMap.of(TAG_NAME_1, "*"), false, new Series[]{SERIES_2, SERIES_3, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {ImmutableMap.of(TAG_NAME_2, TAG_VALUE_1), true, new Series[]{SERIES_4}},
                {ImmutableMap.of(TAG_NAME_2, TAG_VALUE_1), false, new Series[]{SERIES_4, SERIES_7, SERIES_8}},

                {ImmutableMap.of(TAG_NAME_2, TAG_VALUE_2), true, new Series[]{SERIES_5}},
                {ImmutableMap.of(TAG_NAME_2, TAG_VALUE_2), false, new Series[]{SERIES_5, SERIES_6, SERIES_9}},

                {ImmutableMap.of(TAG_NAME_2, "*"), true, new Series[]{SERIES_4, SERIES_5}},
                {ImmutableMap.of(TAG_NAME_2, "*"), false, new Series[]{SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_2), true, new Series[]{SERIES_6}},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_2), false, new Series[]{SERIES_6}},

                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_1), true, new Series[]{SERIES_8}},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_1), false, new Series[]{SERIES_8}},

                {ImmutableMap.of(TAG_NAME_1, "*", TAG_NAME_2, TAG_VALUE_1), true, new Series[]{SERIES_7, SERIES_8}},
                {ImmutableMap.of(TAG_NAME_1, "*", TAG_NAME_2, TAG_VALUE_1), false, new Series[]{SERIES_7, SERIES_8}},

                {ImmutableMap.of(TAG_NAME_1, "*", TAG_NAME_2, "*"), true, new Series[]{SERIES_6, SERIES_7, SERIES_8, SERIES_9}},
                {ImmutableMap.of(TAG_NAME_1, "*", TAG_NAME_2, "*"), false, new Series[]{SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, "*"), true, new Series[]{SERIES_6, SERIES_8}},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, TAG_VALUE_1), true, new Series[]{SERIES_7}},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, TAG_VALUE_2), true, new Series[]{SERIES_9}},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, "*"), true, new Series[]{SERIES_7, SERIES_9}},
                {ImmutableMap.of(TAG_NAME_1, "*", TAG_NAME_2, TAG_VALUE_2), true, new Series[]{SERIES_6, SERIES_9}},
        };
    }

    @DataProvider(name = "series_and_empty_expected_data", parallel = true)
    Object[][] seriesAndEmptyExpectedData() {
        return new Object[][]{
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1 + TAG_VALUE_2), true},
                {ImmutableMap.of(TAG_NAME_1, TAG_VALUE_1 + TAG_VALUE_2), false}
        };
    }

    @Test(dataProvider = "settings_and_expected_series_data", description = "Check that series in response is matched to requested")
    public void testSeriesIsMatchRequested(Map<String, String> queryTags, boolean exactMatch, Series[] expectedSeries) {
        SeriesQuery query = QUERY
                .withTags(queryTags)
                .withExactMatch(exactMatch);
        List<Series> actual = querySeriesAsList(query);
        assertEqualsNoOrder(getTags(actual), getTags(Arrays.asList(expectedSeries)), "Unexpected set of tags in response.");
    }

    @Test(dataProvider = "series_and_empty_expected_data", description = "Check that series in response is matched to requested empty")
    public void testSeriesMatchedToEmpty(Map<String, String> queryTags, boolean exactMatch) {
        SeriesQuery query = QUERY
                .withTags(queryTags)
                .withExactMatch(exactMatch);
        List<Series> actual = querySeriesAsList(query);
        assertEquals(actual.size(), 1, "Wrong count of series");
        assertTrue(actual.get(0).getData().isEmpty(), "Series contains data");
    }

    /**
     * A series tags map is an element of the set.
     */
    private Object[] getTags(List<Series> seriesList) {
        return seriesList.stream().map(Series::getTags).toArray();
    }
}
