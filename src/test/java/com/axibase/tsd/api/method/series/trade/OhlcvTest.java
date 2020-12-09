package com.axibase.tsd.api.method.series.trade;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.TradeSender;
import com.axibase.tsd.api.util.TradeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.model.TimeUnit.MINUTE;
import static com.axibase.tsd.api.util.Util.getUnixTime;

/**
 * Test OHLCV calculations, and response format.
 */
public class OhlcvTest {
    private final String metric = TradeUtil.tradePriceMetric();
    private final String exchange = Mocks.tradeExchange();
    private final String clazz = Mocks.tradeClass();
    private final String symbolA = Mocks.tradeSymbol();
    private final String entityA = TradeUtil.tradeEntity(symbolA, clazz);
    private final String symbolB = Mocks.tradeSymbol();
    private final String entityB = TradeUtil.tradeEntity(symbolB, clazz);
    private final String[] symbols = {symbolA, symbolB};
    /** Test data and requests do not depend on entity, so
     * each response must contain identical series for each entity.*/
    private final String[] entities = {entityA, entityB};

    /** These trade data are repeated in each 10-minute interval.
     * (Each 10 minutes trade numbers are incremented by +100.)
     * Frequency - 2 trades (one BUY and one SELL) each minute.
     *                         minute:      0  1  2  3  4  5  6  7  8  9 */
    private final int[] buyPrices =        {7, 3, 6, 9, 5, 4, 1, 2, 2, 6};
    private final int[] buyVolumes =       {3, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private final int[] buyTradeNumbers =  {1, 4, 5, 7, 9,11,14,15,17,19};
    private final int[] sellPrices =       {4, 2, 4, 3, 2, 4, 6, 7, 5, 5};
    private final int[] sellVolumes =      {1, 1, 1, 1, 1, 1, 1, 2, 1, 1};
    private final int[] sellTradeNumbers = {2, 3, 6, 8,10,12,13,16,18,20};

    @BeforeClass
    public void insertTrades() throws Exception {
        List<Trade> trades = new ArrayList<>();
        addTrades(trades, "2020-12-01T11:00:00Z", 0, Trade.Side.BUY, Trade.Side.SELL);
        addTrades(trades, "2020-12-01T11:10:00Z", 100, null, null);
        addTrades(trades, "2020-12-01T11:20:00Z", 200, Trade.Side.BUY, null);
        addTrades(trades, "2020-12-01T11:30:00Z", 300, null, Trade.Side.SELL);
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @DataProvider
    public Object[][] testCases() {
        /* These arrays are expected in response. */
        final String ohlcvBothSides = "[7, 9, 1, 5, 23]";
        final String ohlcvBuy =       "[7, 9, 1, 6, 12]";
        final String ohlcvSell =      "[4, 7, 2, 5, 11]";

        List<TestCase> testCases = new ArrayList<>();
        SeriesQuery query;

        testCases.add(new TestCase(
                buildBaseQuery(),
                2,
                null,
                new String[]{"2020-12-01T11:00:00Z", "2020-12-01T11:20:00Z", "2020-12-01T11:30:00Z"},
                new String[]{ohlcvBothSides, ohlcvBuy, ohlcvSell}
        ));

        query = buildBaseQuery();
        query.addTag("side", "B");
        testCases.add(new TestCase(
                query,
                2,
                "B",
                new String[]{"2020-12-01T11:00:00Z", "2020-12-01T11:20:00Z"},
                new String[]{ohlcvBuy, ohlcvBuy}
        ));

        query = buildBaseQuery();
        query.addTag("side", "S");
        testCases.add(new TestCase(
                query,
                2,
                "S",
                new String[]{"2020-12-01T11:00:00Z", "2020-12-01T11:30:00Z"},
                new String[]{ohlcvSell, ohlcvSell}
        ));

        query = buildBaseQuery();
        query.addTag("side", "*");
        testCases.add(new TestCase(
                query,
                2,
                null,
                new String[]{"2020-12-01T11:00:00Z", "2020-12-01T11:20:00Z", "2020-12-01T11:30:00Z"},
                new String[]{ohlcvBothSides, ohlcvBuy, ohlcvSell}
        ));

        query = buildBaseQuery();
        query.setStartDate("2020-12-01T11:40:00Z");
        query.setEndDate("2020-12-01T11:50:00Z");
        testCases.add(new TestCase(query, 1, null, new String[0], new String[0] ));

        query = buildBaseQuery();
        query.setStartDate("2020-12-01T11:30:00Z");
        query.setEndDate("2020-12-01T11:50:00Z");
        testCases.add(new TestCase(query, 2, null,
                new String[]{"2020-12-01T11:30:00Z"}, new String[]{ohlcvSell} ));

        return TestUtil.convertTo2DimArray(testCases);
    }

    @Test(dataProvider = "testCases")
    public void test(TestCase testCase) throws JsonProcessingException, JSONException {
        JsonNode responseArray = getResponseAsTree(testCase.query, testCase.seriesCount);
        for (JsonNode seriesNode : responseArray) {
            // check side tag in response
            if (testCase.sideTag != null) {
                JsonNode tagsNode = seriesNode.get("tags");
                Assert.assertNotNull(tagsNode, "Response has no tags, but the 'side' tag expected in response.");
                JsonNode sideTag = tagsNode.get("side");
                Assert.assertNotNull(tagsNode, "The 'side' tag expected in response.");
                Assert.assertEquals(sideTag.asText(), testCase.sideTag);
            }

            // check data array
            JsonNode dataArray = seriesNode.get("data");
            List<TestCase.MultiValueSample> expectedSamples = testCase.samples;
            Assert.assertEquals(dataArray.size(), expectedSamples.size(), "Unexpected samples count in response.");
            int samplesCount = expectedSamples.size();
            for (int i = 0; i < samplesCount; i++) {
                assertSample(dataArray.get(i), expectedSamples.get(i));
            }
        }
    }

    private JsonNode getResponseAsTree(SeriesQuery query, int expectedSeriesCount) throws JsonProcessingException {
        Response response = SeriesMethod.querySeries(query);
        JsonNode responseArray = BaseMethod.responseAsTree(response);
        Assert.assertEquals(responseArray.getNodeType(), JsonNodeType.ARRAY, "Unexpected response format: array of series is expected.");
        Assert.assertEquals(responseArray.size(), expectedSeriesCount, "Unexpected count of series in response.");
        return responseArray;
    }

    private void assertSample(JsonNode actualSample, TestCase.MultiValueSample expectedSample) throws JSONException {
        long expectedTime = getUnixTime(expectedSample.date);
        long actualTime = actualSample.get("t").asLong();
        Assert.assertEquals(actualTime, expectedTime, "Different actual and expected sample timestamps.");

        String expectedValues = expectedSample.values;
        String actualValues = actualSample.get("v").toString();
        JSONAssert.assertEquals(expectedValues, actualValues, JSONCompareMode.STRICT);
    }

    private SeriesQuery buildBaseQuery() {
        List<String> entities = Arrays.asList(this.entities);
        Map<String, String> tags = new HashMap<>();
        Aggregate aggregate = new Aggregate(AggregationType.OHLCV, new Period(10, MINUTE));
        return new SeriesQuery()
                .setMetric(metric)
                .setEntities(entities)
                .setStartDate("2020-12-01T11:00:00Z")
                .setEndDate("2020-12-01T11:40:00Z")
                .setAggregate(aggregate)
                .setTags(tags)
                .setTimeFormat("milliseconds");
    }

    /**
     * Insert trades for 10-minutes interval from specified date-time and for specified side.
     */
    private void addTrades(List<Trade> trades, String date,
                           int tradeNumberOffset,
                           @Nullable Trade.Side buy,
                           @Nullable Trade.Side sell) {
        long startMillis = getUnixTime(date);
        long minute = 60_000;
        for (int i = 0; i < 10; i++) {
            long tradeMillis = startMillis + i * minute;
            if (buy != null) {
                addTrade(trades, tradeMillis, tradeNumberOffset + buyTradeNumbers[i], buy, buyPrices[i], buyVolumes[i]);
            }
            if (sell != null) {
                addTrade(trades, tradeMillis, tradeNumberOffset + sellTradeNumbers[i], sell, sellPrices[i], sellVolumes[i]);
            }
        }
    }

    private void addTrade(List<Trade> trades, long tradeMillis, int tradeNumber, Trade.Side side, int price, int volume) {
        for (String symbol : symbols) {
            Trade trade = new Trade(exchange, clazz, symbol, tradeNumber, tradeMillis, BigDecimal.valueOf(price), volume);
            trade.setSide(side);
            trades.add(trade);
        }
    }

    private static class TestCase {
        private final SeriesQuery query;
        private final int seriesCount;
        private final String sideTag;
        private final List<MultiValueSample> samples;

        @RequiredArgsConstructor
        private static class MultiValueSample {
            private final String date;
            private final String values;
        }

        public TestCase(SeriesQuery query, int seriesCount, String sideTag, String[] dates, String[] values) {
            this.query = query;
            this.seriesCount = seriesCount;
            this.sideTag = sideTag;
            int count = dates.length;
            this.samples = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                samples.add(new MultiValueSample(dates[i], values[i]));
            }
        }
    }
}
