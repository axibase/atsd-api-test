package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.checks.SeriesQueryDataSizeCheck;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
    private void insertSeries() throws Exception {
        List<Series> seriesList = Arrays.asList(SERIES_1, SERIES_2, SERIES_3, SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9);
        Sample sample = Sample.ofDateInteger(START_DATE, 1);
        for(Series series: seriesList) {
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
                {null, true, new Series[] {SERIES_1}},
                {null, false, new Series[] {SERIES_1, SERIES_2, SERIES_3, SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {new String[]{}, true, new Series[] {SERIES_1}},
                {new String[]{}, false, new Series[] {SERIES_1, SERIES_2, SERIES_3, SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {new String[] {TAG_NAME_1, TAG_VALUE_1}, true, new Series[] {SERIES_2}},
                {new String[] {TAG_NAME_1, TAG_VALUE_1}, false, new Series[] {SERIES_2, SERIES_6, SERIES_8}},

                {new String[] {TAG_NAME_1, TAG_VALUE_2}, true, new Series[] {SERIES_3}},
                {new String[] {TAG_NAME_1, TAG_VALUE_2}, false, new Series[] {SERIES_3, SERIES_7, SERIES_9}},

                {new String[] {TAG_NAME_1, "*"}, true, new Series[] {SERIES_2, SERIES_3}},
                {new String[] {TAG_NAME_1, "*"}, false, new Series[] {SERIES_2, SERIES_3, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                /* Nonexistent value of tag. */
                {new String[] {TAG_NAME_1, TAG_VALUE_1 + TAG_VALUE_2}, true, new Series[] {}},
                {new String[] {TAG_NAME_1, TAG_VALUE_1 + TAG_VALUE_2}, false, new Series[] {}},

                {new String[] {TAG_NAME_2, TAG_VALUE_1}, true, new Series[] {SERIES_4}},
                {new String[] {TAG_NAME_2, TAG_VALUE_1}, false, new Series[] {SERIES_4, SERIES_7, SERIES_8}},

                {new String[] {TAG_NAME_2, TAG_VALUE_2}, true, new Series[] {SERIES_5}},
                {new String[] {TAG_NAME_2, TAG_VALUE_2}, false, new Series[] {SERIES_5, SERIES_6, SERIES_9}},

                {new String[] {TAG_NAME_2, "*"}, true, new Series[] {SERIES_4, SERIES_5}},
                {new String[] {TAG_NAME_2, "*"}, false, new Series[] {SERIES_4, SERIES_5, SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {new String[] {TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_2}, true, new Series[] {SERIES_6}},
                {new String[] {TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_2}, false, new Series[] {SERIES_6}},

                {new String[] {TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_1}, true, new Series[] {SERIES_8}},
                {new String[] {TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_1}, false, new Series[] {SERIES_8}},

                {new String[] {TAG_NAME_1, "*", TAG_NAME_2, TAG_VALUE_1}, true, new Series[] {SERIES_7, SERIES_8}},
                {new String[] {TAG_NAME_1, "*", TAG_NAME_2, TAG_VALUE_1}, false, new Series[] {SERIES_7, SERIES_8}},

                {new String[] {TAG_NAME_1, "*", TAG_NAME_2, "*"}, true, new Series[] {SERIES_6, SERIES_7, SERIES_8, SERIES_9}},
                {new String[] {TAG_NAME_1, "*", TAG_NAME_2, "*"}, false, new Series[] {SERIES_6, SERIES_7, SERIES_8, SERIES_9}},

                {new String[] {TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, "*"}, true, new Series[] {SERIES_6, SERIES_8}},
                {new String[] {TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, TAG_VALUE_1}, true, new Series[] {SERIES_7}},
                {new String[] {TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, TAG_VALUE_2}, true, new Series[] {SERIES_9}},
                {new String[] {TAG_NAME_1, TAG_VALUE_2, TAG_NAME_2, "*"}, true, new Series[] {SERIES_7, SERIES_9}},
                {new String[] {TAG_NAME_1, "*", TAG_NAME_2, TAG_VALUE_2}, true, new Series[] {SERIES_6, SERIES_9}},
        };
    }

    @Test(dataProvider = "settings_and_expected_series_data", description = "Check that series in response is match requested")
    public void testSeriesIsMatchRequested(String[] queryTags, boolean exactMatch, Series[] expectedSeries) {
        SeriesQuery query = QUERY
                .withTags(toMap(queryTags))
                .withExactMatch(exactMatch);
        checkTags(query, expectedSeries);
    }

    private Map<String, String> toMap(String[] queryTags) {
        if (queryTags == null) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
            for (int i = 0; i < queryTags.length; i += 2) {
                map.put(queryTags[i], queryTags[i + 1]);
            }

        return map;
    }

    private void checkTags(SeriesQuery query, Series... expected) {
        List<Series> actual = querySeriesAsList(query);

        if (expected.length == 0) {
            assertTrue(actual.size() == 1, "Wrong count of series");
            assertTrue(actual.get(0).getData().isEmpty(), "Series contains data");
        } else {
            assertEquals(getTags(actual), getTags(Arrays.asList(expected)), "Unexpected set of tags in response.");
        }
    }

    /** A series tags map is an element of the set. */
    private Set<Map<String, String>> getTags(List<Series> seriesList) {
        return seriesList.stream().map(Series::getTags).collect(Collectors.toSet());
    }
}
