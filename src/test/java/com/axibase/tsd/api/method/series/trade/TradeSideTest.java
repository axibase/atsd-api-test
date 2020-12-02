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
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
        for (Series series : response) {
            check(series, testCase.buyPrices, testCase.sellPrices);
        }
    }

    private void check(Series series, @Nullable BigDecimal[] expectedBuyPrices, @Nullable BigDecimal[] expectedSellPrices) {
        Map<String, String> tags = series.getTags();
        assertTrue(tags.containsKey("side"));
        String side = tags.get("side");
        switch (side) {
            case "B":
                if (expectedBuyPrices == null) {
                    Assert.fail("Unexpected series with side tag 'B' in response");
                }
                assertSamePrices(series.getData(), expectedBuyPrices);
                break;
            case "S":
                if (expectedSellPrices == null) {
                    Assert.fail("Unexpected series with side tag 'S' in response");
                }
                assertSamePrices(series.getData(), expectedSellPrices);
                break;
            default:
                Assert.fail("Not expected side tag in response: " + side);
        }
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
        List<TestCase> testCases = new ArrayList<>();
        addTestCases(testCases, new int[]{1, 3, 4, 6}, new int[]{2, 5}, null);
        addTestCases(testCases, new int[]{3, 6}, new int[]{2, 5}, new Period(1, HOUR));
        addTestCases(testCases, new int[]{3, 4, 6}, new int[]{2, 5}, new Period(5, MINUTE));
        return TestUtil.convertTo2DimArray(testCases);
    }

    private void addTestCases(List<TestCase> testCases, int[] buyPrices, int[] sellPrices, Period period) {
        BigDecimal[] decimalBuyPrices = toBigDecimal(buyPrices);
        BigDecimal[] decimalSellPrices = toBigDecimal(sellPrices);
        testCases.add(new TestCase(query(period), null, decimalBuyPrices, decimalSellPrices));
        testCases.add(new TestCase(query(period), "*", decimalBuyPrices, decimalSellPrices));
        testCases.add(new TestCase(query(period), "B", decimalBuyPrices, null));
        testCases.add(new TestCase(query(period), "S", null, decimalSellPrices));
    }

    private BigDecimal[] toBigDecimal(int[] intPrices) {
        int samplesCount = intPrices == null ? 0 : intPrices.length;
        BigDecimal[] decimalPrices = new BigDecimal[samplesCount];
        for (int i = 0; i < samplesCount; i++) {
            decimalPrices[i] = BigDecimal.valueOf(intPrices[i]);
        }
        return decimalPrices;
    }

    private SeriesQuery query(@Nullable Period period) {
        String startDate = "2020-12-01T00:00:00Z";
        String endDate = "2020-12-02T00:00:00Z";
        SeriesQuery query = new SeriesQuery(entity, metric, startDate, endDate);
        if (period != null) {
            query.setAggregate(new Aggregate(AggregationType.LAST, period));
        }
        return query;
    }

    private static class TestCase {
        private final SeriesQuery query;
        private final BigDecimal[] buyPrices;
        private final BigDecimal[] sellPrices;

        public TestCase(SeriesQuery query, String side, BigDecimal[] buyPrices, BigDecimal[] sellPrices) {
            if (side != null) {
                query.addTag("side", side);
            }
            this.query = query;
            this.buyPrices = buyPrices;
            this.sellPrices = sellPrices;
        }

        public int seriesCount() {
            return (buyPrices == null && sellPrices == null) ? 0 : (buyPrices == null || sellPrices == null) ? 1 : 2;
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
