package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Registry;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.*;
import static com.axibase.tsd.api.util.Util.addOneMS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.*;

public class SeriesQueryWildcardTest extends SeriesMethod {
    private final static String METRIC_FOR_ENTITY = "m-wc-0";
    private final static String ENTITY_FOR_TAGS = "e-wc-1";
    private final static String METRIC_FOR_TAGS = "m-wc-1";

    @BeforeClass
    public static void prepare() throws Exception {
        insertSeriesWithSimilarEntity();
        insertSeriesWithSimilarTags();
    }

    private static void insertSeriesWithSimilarEntity() throws FileNotFoundException {
        Series seriesA = new Series("e-wc-val1", METRIC_FOR_ENTITY);
        seriesA.addData(new Sample(MIN_STORABLE_DATE, "0"));

        Series seriesB = new Series("e-wc-val2", null);
        seriesB.setMetric(METRIC_FOR_ENTITY);
        seriesB.addData(new Sample(MIN_STORABLE_DATE, "1"));

        Series seriesC = new Series("e-wc-?al1", null);
        seriesC.setMetric(METRIC_FOR_ENTITY);
        seriesC.addData(new Sample(MIN_STORABLE_DATE, "2"));

        Series seriesD = new Series("e-wc-Value2", null);
        seriesD.setMetric(METRIC_FOR_ENTITY);
        seriesD.addData(new Sample(MIN_STORABLE_DATE, "3"));

        Response response = insertSeries(Arrays.asList(seriesA, seriesB, seriesC, seriesD));
        if (OK.getStatusCode() != response.getStatus()) {
            fail("Series insert failed");
        }
    }

    private static void insertSeriesWithSimilarTags() throws Exception {
        Series series = new Series(ENTITY_FOR_TAGS, METRIC_FOR_TAGS);

        series.addData(new Sample(MIN_STORABLE_DATE, "0"));
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("tag1", "val1");
        }}));
        insertSeriesCheck(Collections.singletonList(series));

        series.setData(Collections.singletonList(new Sample(MIN_STORABLE_DATE, "1")));
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("tag2", "val2");
        }}));
        insertSeriesCheck(Collections.singletonList(series));

        series.setData(Collections.singletonList(new Sample(MIN_STORABLE_DATE, "2")));
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("tag1", "Val1");
            put("tag2", "Value2");
        }}));
        insertSeriesCheck(Collections.singletonList(series));

        series.setData(Collections.singletonList(new Sample(MIN_STORABLE_DATE, "3")));
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("tag1", "?al1");
        }}));
        Response response = insertSeries(Collections.singletonList(series));
        if (OK.getStatusCode() != response.getStatus()) {
            fail("Series insert failed");
        }
    }

    /*
    * #3371
    */
    @Test
    public void testEntityWithWildcardExactMatchTrue() throws Exception {
        String entityNameBase = "series-query-limit-entity-";
        String metricName = "series-query-limit-metric";

        Series series = new Series(entityNameBase.concat("1"), metricName);
        series.addData(new Sample(MIN_STORABLE_DATE, "7"));
        insertSeriesCheck(Collections.singletonList(series));

        String entity = entityNameBase.concat("2");
        Registry.Entity.register(entity);
        series.setEntity(entity);
        series.addTag("tag_key", "tag_value");
        series.addData(new Sample(addOneMS(MIN_STORABLE_DATE), "8"));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(entityNameBase.concat("*"), series.getMetric(),
                MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        seriesQuery.setExactMatch(true);
        seriesQuery.setLimit(2);
        seriesQuery.setSeriesLimit(1);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();
        assertEquals("ExactMatch true with wildcard doesn't return series without tags", 1, data.size());

        seriesQuery.addTags("tag_key", "tag_value");
        data = executeQueryReturnSeries(seriesQuery).get(0).getData();
        assertEquals("ExactMatch true with wildcard doesn't return series with tags", 2, data.size());
    }

    /* #2207 */
    @Test(dataProvider = "entities")
    public void testWildcardInEntity(String entity, int seriesCount) throws Exception {
        SeriesQuery seriesQuery = new SeriesQuery(entity, METRIC_FOR_ENTITY);
        seriesQuery.setStartDate(MIN_QUERYABLE_DATE);
        seriesQuery.setEndDate(MAX_QUERYABLE_DATE);

        if (seriesCount == 0) {
            assertEntityNotFound(seriesQuery);
        } else {
            List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
            assertQueryResultSize(seriesCount, seriesList);
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
                {"e-wc-*?", 4},
                {"e-wc-?*", 4},
                {"e-wc-??????", 1},
                {"e-wc-*??????", 1},
                {"e-wc-*??*??*", 4},
                {"e-wc-*?????*", 1},
                {"e-wc-*2", 2}
        };
    }

    /* #2207 */
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
                {"tag1", "*?", 3},
                {"tag1", "?*", 3},
                {"tag2", "??????", 1},
                {"tag2", "*??????", 1},
                {"tag2", "*?", 2},
                {"tag2", "*??*??*", 2},
                {"tag2", "*?????*", 1},
                {"tag2", "*2", 2},
                {"tag1", "?a", 0},
                {"tag2", "???", 0},
                {"tag2", "*???????", 0}
        };
    }

    private void assertQueryResultSize(int requiredSeriesCount, List<Series> seriesList) {
        assertEquals("Required " + requiredSeriesCount + " series not found", requiredSeriesCount, seriesList.size());
        for (int i = 0; i < requiredSeriesCount; i++) {
            assertEquals("Required series empty", 1, seriesList.get(i).getData().size());
        }
    }

    private void assertEntityNotFound(SeriesQuery seriesQuery) throws JSONException {
        JSONObject jsonObject = new JSONObject(querySeries(Collections.singletonList(seriesQuery)).readEntity(String.class));
        String errorMessagePrefix = (String) jsonObject.get("error");
        assertTrue("Entity shouldn't be found", errorMessagePrefix.startsWith("com.axibase.tsd.service.DictionaryNotFoundException"));
    }

    private void assertSeriesEmpty(List<Series> seriesList) {
        assertEquals("Required series not found", 1, seriesList.size());
        assertEquals("Required series not empty", 0, seriesList.get(0).getData().size());
    }

    private List<Series> requestSeriesWithTags(SeriesQuery seriesQuery, final String key, final String value) throws Exception {
        seriesQuery.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put(key, value);
        }}));
        return executeQueryReturnSeries(seriesQuery);
    }
}
