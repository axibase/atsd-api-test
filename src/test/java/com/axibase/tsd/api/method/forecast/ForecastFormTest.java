package com.axibase.tsd.api.method.forecast;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static com.axibase.tsd.api.method.series.SeriesMethod.querySeriesAsList;
import static com.axibase.tsd.api.model.series.SeriesType.FORECAST;
import static com.axibase.tsd.api.model.series.SeriesType.HISTORY;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class ForecastFormTest extends ForecastMethod {
    private final String startDate = "2021-04-01T00:00:00Z";
    private final String endDate   = "2021-04-05T00:00:00Z";
    private final String forecastEndDate   = "2021-04-06T00:00:00Z";
    private final String entityPrefix= Mocks.entity();
    private final String entityA= entityPrefix + "_a";
    private final String entityB = entityPrefix + "_b";
    private final String tagNameA = "tag-name-1";
    private final String tagNameB = "tag-name-2";
    private final String tagValueA = "tag-value-1";
    private final String tagValueB = "tag-value-2";
    private final String forecastSettingsName = "forecast-settings-name";
    private final String specialTagName = "forecast-name";

    @DataProvider(parallel = false)
    public Object[][] testCases() {
        Object[][] testCases = new Object[6][];
        addTestCases(testCases, 0, "METRIC_ENTITY_ALL_TAGS", null, Arrays.asList(
                seriesKey(entityA, tagNameA, tagValueA),
                seriesKey(entityA, tagNameA, tagValueB),
                seriesKey(entityA, tagNameA, tagValueA, tagNameB, tagValueB),
                seriesKey(entityB)
        ));
        addTestCases(testCases, 2, "METRIC_ENTITY", null, Arrays.asList(
                seriesKey(entityA),
                seriesKey(entityB)
        ));
        addTestCases(testCases, 4, "METRIC_ENTITY_DEFINED_TAGS", tagNameA, Arrays.asList(
                seriesKey(entityA, tagNameA, tagValueA),
                seriesKey(entityA, tagNameA, tagValueB),
                seriesKey(entityB)
        ));
        return testCases;
    }

    /**
     * @param testCases          Collection used to store all test cases.
     * @param testCaseId         Id to use in assert messages.
     * @param grouping           How to group series before forecasting.
     * @param groupingTag        Use as part of a grouping key, to group series with the same value of this tag.
     * @param expectedSeriesKeys Expected series keys of stored forecasts.
     */
    private void addTestCases(Object[][] testCases,
                              int testCaseId,
                              String grouping,
                              String groupingTag,
                              List<SeriesKey> expectedSeriesKeys) {
        int id = testCaseId;
        testCases[id] = new Object[] {id, grouping, groupingTag, true, expectedSeriesKeys};
        id++;
        testCases[id] = new Object[] {id, grouping, groupingTag, false, expectedSeriesKeys};
    }

    // Ignore because this test is unstable, and fails unexpectedly.
    @Test(enabled = false, dataProvider = "testCases")
    public void testFormSubmission(int testCaseId,
                                   @NotNull String grouping,
                                   @Nullable String groupingTag,
                                   /* true - store forecasts under another metric in the series table
                                    * false - store forecasts under original metric in the forecasts table */
                                   boolean storeUnderAnotherMetric,
                                   @NotNull List<SeriesKey> expectedSeriesKeys
    ) throws Exception {
        String caseId = "Test case " + testCaseId + ". ";

        /* In each test case we do forecasts for the same set of series,
           but each time with a new metric,
           otherwise forecasts from later test cases will be mixed
           with forecasts stored during previous test cases. */
        String metric = Mocks.metric();
        insertSeries(metric);

        /* Calculate and store forecasts. */
        String producedMetric = storeUnderAnotherMetric ? "mock-" + metric : null;
        Response forecastResponse = sendForecastFormData(formData(metric, grouping, groupingTag, producedMetric));
        int statusCode = forecastResponse.getStatus();
        assertEquals(caseId + "Test case Forecast request failed with status code " + statusCode, Response.Status.OK.getStatusCode(), statusCode);

        /* Get stored forecasts, and wait a bit if stored forecasts count differs from expected,
           because ATSD may need some time to actually store forecasts.
           Also if forecasts are not stored the response can contains empty series,
           so need check that series in response are not empty.
        */
        List<Series> actualSeriesList = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 40; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            actualSeriesList = loadStoredForecasts(metric, producedMetric);
            if (expectedSeriesKeys.size() == actualSeriesList.size() &&
                    actualSeriesList.stream().noneMatch(series -> series.getData().isEmpty())) {
                break;
            }
        }
        long duration = System.currentTimeMillis() - start;
        String msg =caseId + "Duration: " + duration + "ms. Unexpected series count.";
        assertNotNull(actualSeriesList, msg);
        assertEquals(msg, expectedSeriesKeys.size(), actualSeriesList.size());

        /* Sort actual series by metric, entity, and tags to be able to compare them with expected in correct order. */
        Collections.sort(actualSeriesList);

        /* Convert expected series keys to expected series appending suitable metric and tag to each key. */
        SeriesType type = storeUnderAnotherMetric ? HISTORY : FORECAST;
        List<Series> expectedSeriesList = expectedSeriesKeys.stream()
                .map(key -> toExpectedSeries(metric, producedMetric, type, key))
                .sorted()
                .collect(Collectors.toList());

        int size = expectedSeriesList.size();
        for (int i = 0; i < size; i++) {
            String seriesIndex = "Series index = " + i + ". ";
            Series expectedSeries = expectedSeriesList.get(i);
            Series actualSeries = actualSeriesList.get(i);
            // check metric, entity and tags
            msg = caseId + seriesIndex +
                    "Expected series key: " + expectedSeries.toString() +
                    "Actual series: " + actualSeries.toString();
            assertTrue(expectedSeries.compareTo(actualSeries) == 0, msg);
            assertEquals(caseId + seriesIndex, expectedSeries.getType(), actualSeries.getType());
            if (expectedSeries.getType() == FORECAST) {
                assertEquals(caseId + seriesIndex, forecastSettingsName, actualSeries.getForecastName());
            }
            assertEquals(caseId + seriesIndex, 6, actualSeries.getData().size());
        }
    }

    private void insertSeries(String metric) throws Exception {
        List<Series> seriesList = Arrays.asList(
                new Series(entityA, metric, false, tagNameA, tagValueA),
                new Series(entityA, metric, false, tagNameA, tagValueB),
                new Series(entityA, metric, false, tagNameA, tagValueA, tagNameB, tagValueB),
                new Series(entityB, metric, false)
        );

        /*
           Add the same samples to each of the series.
           Each series has a sample every minute from startDate until endDate.
         */
        List<Sample> samples = new ArrayList<>();
        long millis = TimeUtil.epochMillis(startDate);
        long endMillis = TimeUtil.epochMillis(endDate);
        int value = 1;
        while (millis < endMillis) {
            samples.add(Sample.ofTimeInteger(millis, value++));
            millis += 60_000;
        }
        seriesList.forEach(series -> series.addSamples(samples));

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    private MultivaluedMap<String, String> formData(@NotNull String metric,
                                                    @NotNull String grouping,
                                                    @Nullable String groupingTag,
                                                    @Nullable String producedMetric) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("settings.startTime", startDate);
        formData.add("settings.endTime", endDate);
        formData.add("settings.metric", metric);
        formData.add("settings.entity", entityPrefix + "*"); // request all 4 series
        formData.add("groupingType", grouping);
        formData.add("settings.requiredTagKeys", groupingTag);
        formData.add("settings.averagingInterval.intervalCount", "10");
        formData.add("settings.averagingInterval.intervalUnit", "MINUTE");
        formData.add("settings.aggregateStatistics", "SUM");
        formData.add("settings.algorithm", "SSA");
        formData.add("settings.ssaComponentCount", "3");
        formData.add("settings.scoreInterval.intervalCount", "60");
        formData.add("settings.scoreInterval.intervalUnit", "MINUTE");
        formData.add("_settings.autoPeriod", "on");
        formData.add("settings.autoPeriod", "on");
        formData.add("_settings.autoParameters", "on");
        formData.add("settings.autoParameters", "on");
        formData.add("settings.name", forecastSettingsName);
        formData.add("settings.storeInterval.intervalCount", "60");   // forecast horizon
        formData.add("settings.storeInterval.intervalUnit", "MINUTE");
        formData.add("settings.producedMetric", producedMetric);
        formData.add("forecast", "Run");
        return formData;
    }

    private List<Series> loadStoredForecasts(@NotNull String metric, @Nullable String producedMetric) {
        List<Series> seriesList = new ArrayList<>(4);
        seriesList.addAll(querySeriesAsList(query(metric, entityA, producedMetric)));
        seriesList.addAll(querySeriesAsList(query(metric, entityB, producedMetric)));
        return seriesList;
    }

    private SeriesQuery query(@NotNull String metric, @NotNull String entity, @Nullable String producedMetric) {
        SeriesQuery query = new SeriesQuery();
        query.setEntity(entity);
        query.setStartDate(startDate);
        query.setEndDate(forecastEndDate);
        if (producedMetric == null) {
            query.setType(FORECAST);
            query.setForecastName(forecastSettingsName);
            query.setMetric(metric);
        } else {
            query.setType(HISTORY);
            query.setMetric(producedMetric);
        }
        return query;
    }

    private SeriesKey seriesKey(String entity, String... tags) {
        Map<String, String> tagsMap = new HashMap<>();
        for (int i = 0; i < tags.length / 2; i++) {
            tagsMap.put(tags[2 * i], tags[2 * i + 1]);
        }
        return new SeriesKey(entity, tagsMap);
    }

    @RequiredArgsConstructor
    private final class SeriesKey {
        private final String entity;
        private final Map<String, String> tags;
    }

    private Series toExpectedSeries(String metric, String producedMetric, SeriesType seriesType, SeriesKey seriesKey) {
        String actualMetric = seriesType == FORECAST ? metric : producedMetric;
        Series series = new Series(seriesKey.entity, actualMetric, false);
        series.setTags(new HashMap<>(seriesKey.tags));
        if (seriesType == HISTORY) {
            series.addTag(specialTagName, forecastSettingsName);
        } else {
            series.setType(FORECAST);
        }
        return series;
    }
}
