package com.axibase.tsd.api.method.series.trade.ohlcv;

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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.model.TimeUnit.MINUTE;
import static com.axibase.tsd.api.util.Util.getUnixTime;

/**
 * Test empty and not empty series in response
 * in case when there data for one entity and there are no data for another entity.
 */
public class OhlcvTwoEntitiesTest {
    private final String metric = TradeUtil.tradePriceMetric();
    private final String exchange = Mocks.tradeExchange();
    private final String clazz = Mocks.tradeClass();
    private final String symbolA = Mocks.tradeSymbol();
    private final String entityA = TradeUtil.tradeEntity(symbolA, clazz);
    private final String symbolB = Mocks.tradeSymbol();
    private final String entityB = TradeUtil.tradeEntity(symbolB, clazz);

    @BeforeClass
    public void insertTrades() throws Exception {
        Trade[] trades = {
                trade(symbolA, 1, getUnixTime("2020-12-01T11:00:00Z")),
                trade(symbolB, 2, getUnixTime("2020-12-01T11:10:00Z"))
        };
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    private Trade trade(String symbol, int tradeNum, long unixTime) {
        Trade trade = new Trade(exchange, clazz, symbol, tradeNum, unixTime, BigDecimal.TEN, 1);
        trade.setSide(Trade.Side.BUY);
        return trade;
    }

    @DataProvider
    public Object[][] testCases() {
        TestCase[] testCases = {
            new TestCase(buildQuery("2020-12-01T11:00:00Z", "2020-12-01T11:20:00Z", entityA, entityB), true, true),
            new TestCase(buildQuery("2020-12-01T11:00:00Z", "2020-12-01T11:10:00Z", entityA, entityB), true, false),
            new TestCase(buildQuery("2020-12-01T11:10:00Z", "2020-12-01T11:20:00Z", entityA, entityB), false, true),
            new TestCase(buildQuery("2020-12-01T11:20:00Z", "2020-12-01T11:30:00Z", entityA, entityB), false, false),
            new TestCase(buildQuery("2020-12-01T11:00:00Z", "2020-12-01T11:10:00Z", entityB), false, false)
        };
        return TestUtil.convertTo2DimArray(testCases);
    }

    @Test(dataProvider = "testCases")
    public void test(TestCase testCase) throws JsonProcessingException {
        JsonNode responseArray = getResponseAsTree(testCase.query);
        for (JsonNode seriesNode : responseArray) {
            JsonNode dataArray = seriesNode.get("data");
            if (!testCase.seriesAHasData && !testCase.seriesBHasData) {
                Assert.assertTrue(dataArray.isEmpty(), "Empty data array expected.");
            } else {
                String entity = seriesNode.get("entity").asText();
                if ((testCase.seriesAHasData && entityA.equals(entity)) ||
                    (testCase.seriesBHasData && entityB.equals(entity))
                ) {
                    Assert.assertFalse(dataArray.isEmpty(), "Expect not empty data array.");
                } else {
                    Assert.assertTrue(dataArray.isEmpty(), "Expect an empty data array.");
                }
            }
        }
    }

    private JsonNode getResponseAsTree(SeriesQuery query) throws JsonProcessingException {
        Response response = SeriesMethod.querySeries(query);
        JsonNode responseArray = BaseMethod.responseAsTree(response);
        Assert.assertEquals(responseArray.getNodeType(), JsonNodeType.ARRAY, "Unexpected response format: array of series is expected.");
        Assert.assertFalse(responseArray.isEmpty(), "Not empty array of series expected in response.");
        return responseArray;
    }

    private SeriesQuery buildQuery(String startDate, String endDate, String... entities) {
        return new SeriesQuery()
                .setMetric(metric)
                .setEntities(Arrays.asList(entities))
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setAggregate(new Aggregate(AggregationType.OHLCV, new Period(10, MINUTE)));
    }

    @RequiredArgsConstructor
    private static class TestCase {
        private final SeriesQuery query;
        private final boolean seriesAHasData;
        private final boolean seriesBHasData;
    }
}
