package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestNameGenerator;
import io.qameta.allure.Issue;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.axibase.tsd.api.util.Util.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryWildcardTest extends SeriesMethod {
    private final static String METRIC_FOR_ENTITY = Mocks.metric();
    private final static String ENTITY_FOR_TAGS = Mocks.entity();
    private final static String METRIC_FOR_TAGS = Mocks.metric();

    @BeforeClass
    public static void prepare() throws Exception {
        insertSeriesWithSimilarEntity();
        insertSeriesWithSimilarTags();
    }

    private static void insertSeriesWithSimilarEntity() throws Exception {
        Series series1 = new Series("e-wc-val1", METRIC_FOR_ENTITY);
        series1.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 0));

        Series series2 = new Series("e-wc-val2", METRIC_FOR_ENTITY);
        series2.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 1));

        Series series3 = new Series("e-wc-?al1", METRIC_FOR_ENTITY);
        series3.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 2));

        Series series4 = new Series("e-wc-Value2", METRIC_FOR_ENTITY);
        series4.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 3));

        Series series5 = new Series("e-wc-lu", METRIC_FOR_ENTITY);
        series5.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 4));

        insertSeriesCheck(series1, series2, series3, series4, series5);
    }

    private static void insertSeriesWithSimilarTags() throws Exception {
        Series series1 = new Series(ENTITY_FOR_TAGS, METRIC_FOR_TAGS);
        series1.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 0));
        series1.addTag("tag1", "val1");

        Series series2 = new Series(ENTITY_FOR_TAGS, METRIC_FOR_TAGS);
        series2.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 1));
        series2.addTag("tag2", "val2");

        Series series3 = new Series(ENTITY_FOR_TAGS, METRIC_FOR_TAGS);
        series3.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 2));
        series3.addTag("tag1", "Val1").addTag("tag2", "Value2");

        Series series4 = new Series(ENTITY_FOR_TAGS, METRIC_FOR_TAGS);
        series4.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 3));
        series4.addTag("tag1", "?al1");

        Series series5 = new Series(ENTITY_FOR_TAGS, METRIC_FOR_TAGS);
        series5.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 4));
        series5.addTag("tag1", "lu");
        series5.addTag("tag2", "lU");

        insertSeriesCheck(series1, series2, series3, series4, series5);
    }

    @Issue("3371")
    @Test
    public void testEntityWithWildcardExactMatchTrue() throws Exception {
        TestNameGenerator nameGenerator = new TestNameGenerator();
        String metricName = Mocks.metric();

        Series series1 = new Series(nameGenerator.newEntityName(), metricName);
        series1.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 7));

        Series series2 = new Series(nameGenerator.newEntityName(), metricName, "tag_key", "tag_value");
        series2.addSamples(
                Sample.ofTimeInteger(MIN_STORABLE_TIMESTAMP, 7),
                Sample.ofTimeInteger(MIN_STORABLE_TIMESTAMP + 1, 8)
        );

        insertSeriesCheck(series1, series2);

        SeriesQuery seriesQuery = new SeriesQuery(
                nameGenerator.getPrefix(TestNameGenerator.Key.ENTITY).concat("*"),
                series1.getMetric(),
                MIN_QUERYABLE_DATE,
                MAX_QUERYABLE_DATE
        );
        seriesQuery.setExactMatch(true);
        seriesQuery.setLimit(2);
        seriesQuery.setSeriesLimit(1);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();
        assertEquals("ExactMatch true with wildcard doesn't return series without tags", 1, data.size());

        seriesQuery.addTag("tag_key", "tag_value");
        data = querySeriesAsList(seriesQuery).get(0).getData();
        assertEquals("ExactMatch true with wildcard doesn't return series with tags", 2, data.size());
    }

    @Issue("2207")
    @Issue("4644")
    @Test(dataProvider = "entities")
    public void testWildcardInEntity(String entity, int seriesCount)  {
        SeriesQuery seriesQuery = new SeriesQuery(entity, METRIC_FOR_ENTITY);
        seriesQuery.setStartDate(MIN_QUERYABLE_DATE);
        seriesQuery.setEndDate(MAX_QUERYABLE_DATE);

        List<Series> seriesList = querySeriesAsList(seriesQuery);
        if (seriesCount != 0) {
            assertQueryResultSize(seriesCount, seriesList);
        } else {
            assertSeriesEmpty(seriesList);
        }
    }

    @DataProvider(name = "entities")
    public Object[][] provideEntities() {
        return new Object[][]{
                {"e-wc-?a", 0},
                {"e-wc-???", 0},
                {"e-wc-*???????", 0},
                {"e-wc-v?l?", 2},
                {"e-wc-?al1", 2},
                {"e-wc-\\?al1", 1},
                {"e-wc-\\?*", 1},
                {"e-wc-*?", 5},
                {"e-wc-?*", 5},
                {"e-wc-??????", 1},
                {"e-wc-*??????", 1},
                {"e-wc-*??*??*", 4},
                {"e-wc-*?????*", 1},
                {"e-wc-*2", 2},
                {"e-wc-*\\u", 1},
                {"e-wc-*\\U", 1},
        };
    }

    @Issue("2207")
    @Issue("4644")
    @Test(dataProvider = "tags")
    public void testWildcardInTagValue(String key, String value, int seriesWithNonEmptyDataCount) throws Exception {
        SeriesQuery seriesQuery = new SeriesQuery(ENTITY_FOR_TAGS, METRIC_FOR_TAGS,
                MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Series> seriesList;

        if (seriesWithNonEmptyDataCount == 0) {
            seriesList = requestSeriesWithTags(seriesQuery, key, value);
            assertSeriesEmpty(seriesList);
        } else {
            seriesList = requestSeriesWithTags(seriesQuery, key, value);
            assertQueryResultSize(seriesWithNonEmptyDataCount, seriesList);
        }
    }

    @DataProvider(name = "tags")
    public Object[][] provideTags() {
        return new Object[][]{
                {"tag1", "v?l?", 1},
                {"tag1", "?al1", 3},
                {"tag1", "\\?al1", 1},
                {"tag1", "\\?*", 1},
                {"tag1", "*?", 4},
                {"tag1", "?*", 4},
                {"tag2", "??????", 1},
                {"tag2", "*??????", 1},
                {"tag2", "*?", 3},
                {"tag2", "*??*??*", 2},
                {"tag2", "*?????*", 1},
                {"tag2", "*2", 2},
                {"tag1", "?a", 0},
                {"tag2", "???", 0},
                {"tag2", "*???????", 0},
                {"tag1", "*\\u", 1},
                {"tag2", "*\\U", 1}
        };
    }

    private void assertQueryResultSize(int requiredSeriesCount, List<Series> seriesList) {
        assertEquals("Required " + requiredSeriesCount + " series not found", requiredSeriesCount, seriesList.size());
        for (int i = 0; i < requiredSeriesCount; i++) {
            assertEquals("Required series empty", 1, seriesList.get(i).getData().size());
        }
    }

    private void assertSeriesEmpty(List<Series> seriesList) {
        assertEquals("Required series not found", 1, seriesList.size());
        assertEquals("Required series not empty", 0, seriesList.get(0).getData().size());
    }

    private List<Series> requestSeriesWithTags(SeriesQuery seriesQuery, final String key, final String value) throws Exception {
        seriesQuery.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put(key, value);
        }}));
        return querySeriesAsList(seriesQuery);
    }
}
