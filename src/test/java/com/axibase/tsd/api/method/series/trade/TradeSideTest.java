package com.axibase.tsd.api.method.series.trade;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.TradeSender;
import com.axibase.tsd.api.util.TradeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.model.TimeUnit.HOUR;
import static com.axibase.tsd.api.model.TimeUnit.MINUTE;
import static com.axibase.tsd.api.util.Util.getUnixTime;
import static org.testng.AssertJUnit.*;

/**
 * Test that the response contains series with requested sides.
 * Queries with and without aggregation are tested, because they may be processed differently in ATSD.
 * By the same reason two different aggregation periods are tested.
 */
public class TradeSideTest {

    private final String exchange = Mocks.tradeExchange();
    private final String clazz = Mocks.tradeClass();
    private final String symbol = Mocks.tradeSymbol();
    private final String metric = TradeUtil.tradePriceMetric();
    private final String entity = TradeUtil.tradeEntity(symbol, clazz);

    @BeforeClass
    public void insertTrades() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(trade("2020-12-01T10:56:00Z", 1, Trade.Side.BUY, 1));
        trades.add(trade("2020-12-01T10:58:00Z", 2, Trade.Side.SELL, 2));
        trades.add(trade("2020-12-01T10:59:00Z", 3, Trade.Side.BUY, 3));
        trades.add(trade("2020-12-01T11:01:00Z", 4, Trade.Side.BUY, 4));
        trades.add(trade("2020-12-01T11:02:00Z", 5, Trade.Side.SELL, 5));
        trades.add(trade("2020-12-01T11:06:00Z", 6, Trade.Side.BUY, 6));
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @Test(dataProvider = "testCases")
    public void test(TestCase testCase) {
        List<Series> response = SeriesMethod.querySeriesAsList(testCase.query);
        assertEquals("Response has unexpected series count", testCase.seriesCount(), response.size());
        response.forEach(series -> check(series, testCase));
    }

    private void check(Series series, TestCase testCase) {
        if (testCase.isNoDataFoundResponse()) {
            assertTrue("It is expected that response contains an empty 'data' array.", series.getData().isEmpty());
            assertSide(testCase.side(), series.getTags());
            return;
        }
        Map<String, String> tags = series.getTags();
        assertTrue("The 'side' tag is expected in series response.", tags.containsKey("side"));
        String side = tags.get("side");
        switch (side) {
            case "B":
                if (testCase.hasNoBuyPrices()) {
                    Assert.fail("Unexpected series with side tag 'B' in response");
                }
                assertSamePrices(series.getData(), testCase.buyPrices);
                break;
            case "S":
                if (testCase.hasNoSellPrices()) {
                    Assert.fail("Unexpected series with side tag 'S' in response");
                }
                assertSamePrices(series.getData(), testCase.sellPrices);
                break;
            default:
                Assert.fail("Not expected side tag in response: " + side);
        }
    }

    private void assertSide(String expectedSide, Map<String, String> actualTags) {
        if (expectedSide == null) return;
        String message = "Expect tag 'side' = '" + expectedSide + "'";
        assertNotNull(message, actualTags);
        String actualSide = actualTags.get("side");
        assertEquals(message, expectedSide, actualSide);
    }

    private void assertSamePrices(List<Sample> actualSeries, @NotNull BigDecimal[] expectedPrices) {
        assertEquals("Unexpected length of series", actualSeries.size(), expectedPrices.length);
        int count = expectedPrices.length;
        for (int i = 0; i < count; i++) {
            BigDecimal expectedValue = expectedPrices[i];
            BigDecimal actualValue = actualSeries.get(i).getValue();
            assertEquals("Unexpected series value", 0, expectedValue.compareTo(actualValue));
        }
    }

    @DataProvider
    public Object[][] testCases() {
        Period aMinute =  new Period(1, MINUTE);
        List<TestCase> testCases = new ArrayList<>();
        addTestCases("2020-12-01T00:00:00Z", "2020-12-02T00:00:00Z", false, null, new int[]{1, 3, 4, 6}, new int[]{2, 5}, testCases);
        addTestCases("2020-12-01T10:57:00Z", "2020-12-01T10:59:00Z", false, null, null, new int[]{2}, testCases);
        addTestCases("2020-12-01T10:59:00Z", "2020-12-01T11:02:00Z", false, null, new int[]{3, 4}, null, testCases);
        addTestCases("2020-12-01T00:00:00Z", "2020-12-02T00:00:00Z", true, null, new int[]{6}, new int[]{5}, testCases);
        addTestCases("2020-12-01T10:57:00Z", "2020-12-01T10:59:00Z", true, null, null, new int[]{2}, testCases);
        addTestCases("2020-12-01T10:59:00Z", "2020-12-01T11:02:00Z", true, null, new int[]{4}, null, testCases);
        addTestCases("2020-12-01T00:00:00Z", "2020-12-02T00:00:00Z", true, new Period(1, HOUR), new int[]{3, 6}, new int[]{2, 5}, testCases);
        addTestCases("2020-12-01T00:00:00Z", "2020-12-02T00:00:00Z", true, new Period(5, MINUTE), new int[]{3, 4, 6}, new int[]{2, 5}, testCases);
        addTestCases("2020-12-01T10:57:00Z", "2020-12-01T10:59:00Z", true, aMinute, null, new int[]{2}, testCases);
        addTestCases("2020-12-01T10:59:00Z", "2020-12-01T11:02:00Z", true, aMinute, new int[]{3, 4}, null, testCases);
        addTestCases("2020-12-01T11:03:00Z", "2020-12-01T11:06:00Z", false, null, null, null, testCases);
        addTestCases("2020-12-01T11:03:00Z", "2020-12-01T11:06:00Z", true, null, null, null, testCases);
        addTestCases("2020-12-01T11:03:00Z", "2020-12-01T11:06:00Z", false, aMinute, null, null, testCases);
        return TestUtil.convertTo2DimArray(testCases);
    }

    private void addTestCases(String startTime, String endTime, boolean setAggregation, Period period, int[] buyPrices, int[] sellPrices, List<TestCase> testCases) {
        BigDecimal[] decimalBuyPrices = toBigDecimal(buyPrices);
        BigDecimal[] decimalSellPrices = toBigDecimal(sellPrices);

        SeriesQuery query = query(startTime, endTime, null, setAggregation, period);
        testCases.add(new TestCase(query, decimalBuyPrices, decimalSellPrices));

        query = query(startTime, endTime, "*", setAggregation, period);
        testCases.add(new TestCase(query, decimalBuyPrices, decimalSellPrices));

        query = query(startTime, endTime, "B", setAggregation, period);
        testCases.add(new TestCase(query, decimalBuyPrices, null));

        query = query(startTime, endTime, "S", setAggregation, period);
        testCases.add(new TestCase(query, null, decimalSellPrices));
    }

    @NotNull
    private BigDecimal[] toBigDecimal(int[] intPrices) {
        int samplesCount = intPrices == null ? 0 : intPrices.length;
        BigDecimal[] decimalPrices = new BigDecimal[samplesCount];
        for (int i = 0; i < samplesCount; i++) {
            decimalPrices[i] = BigDecimal.valueOf(intPrices[i]);
        }
        return decimalPrices;
    }

    private SeriesQuery query(String startTime,
                              String endTime,
                              @Nullable String side,
                              boolean setAggregation,
                              @Nullable Period period) {
        SeriesQuery query = new SeriesQuery(entity, metric, startTime, endTime);
        if (side != null) {
            query.addTag("side", side);
        }
        if (setAggregation) {
            query.setAggregate(new Aggregate(AggregationType.LAST, period));
        }
        return query;
    }

    private static class TestCase {
        private final SeriesQuery query;
        @NotNull
        private final BigDecimal[] buyPrices;
        @NotNull
        private final BigDecimal[] sellPrices;

        public TestCase(SeriesQuery query, @Nullable BigDecimal[] buyPrices, @Nullable BigDecimal[] sellPrices) {
            this.query = query;
            this.buyPrices = buyPrices == null ? new BigDecimal[0] : buyPrices;
            this.sellPrices = sellPrices == null ? new BigDecimal[0] : sellPrices;
        }

        public int seriesCount() {
            return buyPrices.length == 0 || sellPrices.length == 0 ? 1 : 2;
        }

        public boolean isNoDataFoundResponse() {
            return buyPrices.length == 0 && sellPrices.length == 0;
        }

        @Nullable
        public String side() {
            Map<String, List<String>> tags = query.getTags();
            if (tags == null) {
                return null;
            }
            List<String> sides = tags.get("side");
            if (sides == null || sides.isEmpty()) {
                return null;
            }
            if (sides.size() > 1) {
                throw new IllegalArgumentException("A query in this test can not have more than one side.");
            }
            String side = sides.get(0);
            return side.equalsIgnoreCase("*") ? null : side;
        }

        public boolean hasNoBuyPrices() {
            return buyPrices.length == 0;
        }

        public boolean hasNoSellPrices() {
            return sellPrices.length == 0;
        }
    }

    private Trade trade(String date, long tradeNumber, Trade.Side side, int price) {
        long quantity = 1;
        Trade trade =
                new Trade(exchange, clazz, symbol, tradeNumber, getUnixTime(date), BigDecimal.valueOf(price), quantity);
        trade.setSide(side);
        return trade;
    }
}
