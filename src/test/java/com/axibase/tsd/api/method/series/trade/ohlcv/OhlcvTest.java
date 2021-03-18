package com.axibase.tsd.api.method.series.trade.ohlcv;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.series.trade.SyntheticDataProvider;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.CommonAssertions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Objects;

import static com.axibase.tsd.api.util.Util.getUnixTime;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test OHLCV aggregation, grouping, and response format.
 */
public class OhlcvTest {
    private final SyntheticDataProvider dataProvider = new SyntheticDataProvider();

    @BeforeClass
    public void insertTrades() throws Exception {
        dataProvider.insertTrades();
    }

    @DataProvider
    public Object[][] testCases() {
        OhlcvTestCasesBuilder testBuilder = new OhlcvTestCasesBuilder(dataProvider);
        return testBuilder.testCases();
    }

    @Test(dataProvider = "testCases")
    public void test(TestCase testCase) throws JsonProcessingException, JSONException {
        JsonNode responseArray = getResponseAsTree(testCase.query, testCase.seriesList.size());
        for (JsonNode seriesNode : responseArray) {
            String entity = getEntity(seriesNode);
            String side = getSide(seriesNode);

            OhlcvSeries expectedSeries = pull(entity, side, testCase.seriesList);
            String explanation = String.format("Expect series with entity {} and side {}.", entity, side);
            assertNotNull(expectedSeries, explanation);
            assertAggregation(seriesNode, expectedSeries.aggregation);
            assertGroup(seriesNode, expectedSeries.group);


            JsonNode dataArray = seriesNode.get("data");
            assertNotNull(dataArray, "There are no 'data' array in response.");
            List<MultiValueSample> expectedSamples = expectedSeries.samples;
            int samplesCount = expectedSamples.size();
            assertEquals(dataArray.size(), samplesCount, "Unexpected samples count in response.");
            for (int i = 0; i < samplesCount; i++) {
                assertSample(dataArray.get(i), expectedSamples.get(i));
            }
        }
    }

    private OhlcvSeries pull(String entity, String side, List<OhlcvSeries> seriesList) {
        OhlcvSeries found = null;
        for (OhlcvSeries series : seriesList) {
            if (Objects.equals(entity, series.entity) && Objects.equals(side, series.side)) {
                found = series;
                break;
            }
        }
        if (found != null) {
            seriesList.remove(found);
        }
        return found;
    }

    private void assertAggregation(JsonNode seriesNode, String expectedAggregation) {
        assertType(seriesNode, expectedAggregation, "aggregate");
    }

    private void assertGroup(JsonNode seriesNode, String expectedGroup) {
        assertType(seriesNode, expectedGroup, "group");
    }

    private void assertType(JsonNode seriesNode, String expectedType, String transformation) {
        if (expectedType != null) {
            JsonNode node = seriesNode.get(transformation);
            assertNotNull(node, "Response has no '" + transformation + "' field, but it is expected.");
            JsonNode typeNode = node.get("type");
            assertNotNull(typeNode, transformation + " has no 'type', but it is expected.");
            assertEquals(typeNode.asText(), expectedType, "Unexpected " + transformation + " type.");
        }
    }

    @Nullable
    private String getSide(JsonNode seriesNode) {
        JsonNode tagsNode = seriesNode.get("tags");
        // assertNotNull(tagsNode, "Response has no tags, but the 'side' tag expected in response.");
        if (tagsNode == null) return null;
        JsonNode sideNode = tagsNode.get("side");
        // assertNotNull(sideNode, "The 'side' tag expected in response.");
        return sideNode == null ? null : sideNode.asText();
    }

    @Nullable
    private String getEntity(JsonNode seriesNode) {
        JsonNode entityNode = seriesNode.get("entity");
        // assertNotNull(entityNode, "Response has no entity, but entity is expected in response.");
        return entityNode == null ? null : entityNode.asText();
    }

    private JsonNode getResponseAsTree(SeriesQuery query, int expectedSeriesCount) throws JsonProcessingException {
        JsonNode responseArray = SeriesMethod.getResponseAsTree(query);
        CommonAssertions.assertArraySize(responseArray, expectedSeriesCount);
        return responseArray;
    }

    private void assertSample(JsonNode actualSample, MultiValueSample expectedSample) throws JSONException {
        long expectedTime = getUnixTime(expectedSample.date);
        long actualTime = actualSample.get("t").asLong();
        assertEquals(actualTime, expectedTime, "Different actual and expected sample timestamps.");

        String expectedValues = expectedSample.values;
        String actualValues = actualSample.get("v").toString();
        JSONAssert.assertEquals(expectedValues, actualValues, JSONCompareMode.STRICT);
    }
}
