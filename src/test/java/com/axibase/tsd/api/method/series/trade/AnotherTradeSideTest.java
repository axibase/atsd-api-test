package com.axibase.tsd.api.method.series.trade;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.model.financial.Trade.Side;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.util.Util.getUnixTime;
import static org.testng.Assert.assertEquals;

/**
 * Test that the response contains series with requested trade sides.
 * This test covers trades with empty side,
 * which is not covered by the {@link TradeSideTest}.
 *
 * Test does not verify aggregation values,
 * it just checks series count in response, side tag, and samples (data) count.
 *
 * Test different combinations of following settings:
 * group/aggregate,
 * ohlcv/close/count - because ATSD may process these aggregations differently,
 * different conditions on the 'side' tag value,
 * different periods - because HBase aggregation filter must write response for each row (hourly),
 * and that may affect results for different periods differently.
 */
public class AnotherTradeSideTest {

    private final String exchange = Mocks.tradeExchange();
    private final String clazz = Mocks.tradeClass();
    private final String symbolSided = Mocks.tradeSymbol();    // trades for this symbol have side value "B" or "S"
    private final String symbolSideless = Mocks.tradeSymbol(); // trades for this symbol do not have side value
    private final String metric = TradeUtil.tradePriceMetric();
    private final String startDate = "2021-03-01T10:00:00Z";
    private final String endDate = "2021-03-01T15:00:00Z";
    private final int tradesPeriodMinutes = 3;                // time interval between trades in test data

    @BeforeClass
    public void insertTrades() throws Exception {
        long periodMillis = tradesPeriodMinutes * 60_000;
        long startTimeMillis = getUnixTime(startDate);
        long endTimeMillis = getUnixTime(endDate);
        long time = startTimeMillis;
        int tradeNumber = 1;
        List<Trade> trades = new ArrayList<>();
        while (time < endTimeMillis) {
            trades.add(new Trade(exchange, clazz, symbolSided, tradeNumber++, time, BigDecimal.ONE, 1).setSide(Side.BUY));
            trades.add(new Trade(exchange, clazz, symbolSided, tradeNumber++, time, BigDecimal.ONE, 1).setSide(Side.SELL));
            trades.add(new Trade(exchange, clazz, symbolSideless, tradeNumber++, time, BigDecimal.ONE, 1));
            time += periodMillis;
        }
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @DataProvider
    public Object[][] testCases() {
        List<TestCase> testCases = new ArrayList<>();
        String[] functions = {"CLOSE", "OHLCV", "COUNT"};
        String[] periods = {null, "150 minute", "2 hour", "31 minute", "7 minute", "5 minute"};
        String[][] sides = {null, {}, {"*"}, {"B"}, {"S"}, {"B", "S"}};
        for (String function : functions) {
            for (String period : periods) {
                for (String[] requestedSides : sides) {
                    addTestCase(true, function, period, requestedSides, testCases);
                    addTestCase(false, function, period, requestedSides, testCases);
                }
            }
        }
        return TestUtil.convertTo2DimArray(testCases);
    }

    @Test(dataProvider = "testCases")
    public void test(TestCase testCase) throws JsonProcessingException {
        JsonNode response = SeriesMethod.getResponseAsTree(testCase.query);
        CommonAssertions.assertArraySize(response, testCase.expectedSeriesCount);
        Assert.assertEquals(extractSideValues(response), testCase.expectedSides);
        for (JsonNode seriesNode : response) {
            JsonNode dataArray = seriesNode.get("data");
            assertEquals(dataArray.size(), testCase.expectedSamplesCount, "Unexpected samples count.");
        }
    }

    private void addTestCase(boolean isAggregate, String function, String periodStr, String[] requestedSides, List<TestCase> testCases) {
        Period period = Period.from(periodStr);
        SeriesQuery query = query(isAggregate, function, period, requestedSides);
        int seriesCount = expectedSeriesCount(isAggregate, requestedSides);
        Set<String> expectedSides = expectedSides(isAggregate, requestedSides);
        int expectedSamplesCount = expectedSamplesCount(period);
        testCases.add(new TestCase(query, seriesCount, expectedSides, expectedSamplesCount));
    }

    private SeriesQuery query(boolean isAggregate, String function, Period period, String[] sideValues) {
        SeriesQuery.SeriesQueryBuilder queryBuilder = SeriesQuery.builder()
                .metric(metric).startDate(startDate).endDate(endDate).exactMatch(false);
        String entitySided = TradeUtil.tradeEntity(symbolSided, clazz);
        String entitySideless = TradeUtil.tradeEntity(symbolSideless, clazz);
        List<String> entities = Arrays.asList(entitySided, entitySideless);
        if (isAggregate) {
            queryBuilder.entities(entities).aggregate(new Aggregate(AggregationType.valueOf(function), period));
        } else {
            // Currently ATSD does not group entities. So use single entity here.
            queryBuilder.entity(entitySided).group(new Group(GroupType.valueOf(function), period));
        }
        if (sideValues != null) {
            queryBuilder.tags(ImmutableMap.of("side", Arrays.asList(sideValues)));
        }
        return queryBuilder.build();
    }

    private int expectedSeriesCount(boolean isAggregate, String[] requestedSides) {
        if (requestedSides == null || requestedSides.length == 0) {
            return isAggregate ? 3 : 1;
        }
        if (requestedSides.length == 1) {
            String side = requestedSides[0];
            if (side.equals("*")) {
                return isAggregate ? 3 : 1;
            }
            return 1;
        }
        return isAggregate ? 2 : 1;
    }

    private Set<String> expectedSides(boolean isAggregate, String[] requestedSides) {
        boolean onlyB = requestedSides != null && requestedSides.length == 1 && requestedSides[0].equals("B");
        boolean onlyS = requestedSides != null && requestedSides.length == 1 && requestedSides[0].equals("S");
        Set<String> result = new HashSet<>();
        if (!isAggregate && !onlyB && !onlyS) return result;
        if (!onlyS) {
            result.add("B");
        }
        if (!onlyB) {
            result.add("S");
        }
        return result;
    }

    private Set<String> extractSideValues(JsonNode response) {
        Set<String> sides = new HashSet<>();
        for (JsonNode seriesNode : response) {
            JsonNode tagsNode = seriesNode.get("tags");
            if (tagsNode == null) continue;
            JsonNode sideNode = tagsNode.get("side");
            if (sideNode == null) continue;
            String side = sideNode.asText();
            if (StringUtils.isBlank(side)) continue;
            if ("B".equalsIgnoreCase(side) || "BUY".equalsIgnoreCase(side)) sides.add("B");
            if ("S".equalsIgnoreCase(side) || "SELL".equalsIgnoreCase(side)) sides.add("S");
        }
        return sides;
    }

    private int expectedSamplesCount(Period period) {
        if (period == null) return 1;
        long startTimeMillis = getUnixTime(startDate);
        long endTimeMillis = getUnixTime(endDate);
        long selectionIntervalMillis = endTimeMillis - startTimeMillis;
        long periodMillis = period.toMilliseconds();
        int periodsCount = (int) (selectionIntervalMillis / periodMillis);
        if (selectionIntervalMillis - periodsCount * periodMillis > tradesPeriodMinutes * 60_000) {
            // in this case "incomplete" period contains trades for all tested instruments
            periodsCount++;
        }
        return periodsCount;
    }

    @RequiredArgsConstructor
    private static class TestCase {
        @NotNull
        private final SeriesQuery query;
        private final int expectedSeriesCount;
        /* Expected set of values of the 'side' tag, collected for all series in response.
         * So it allows rough verification of 'side' tag values in response.  */
        @NotNull
        private final Set<String> expectedSides;
        /* Each series in response must have that number of samples. */
        private final int expectedSamplesCount;
    }
}
