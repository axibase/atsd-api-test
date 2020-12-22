package com.axibase.tsd.api.method.series.trade.ohlcv;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TradeSender;
import com.axibase.tsd.api.util.TradeUtil;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.util.Util.getUnixTime;

public class OhlcvSyntheticDataProvider {
    final String metric = TradeUtil.tradePriceMetric();
    final String exchange = Mocks.tradeExchange();
    final String clazz = Mocks.tradeClass();
    final String symbolA = Mocks.tradeSymbol();
    final String entityA = TradeUtil.tradeEntity(symbolA, clazz);
    final String symbolB = Mocks.tradeSymbol();
    final String entityB = TradeUtil.tradeEntity(symbolB, clazz);
    final String[] symbols = {symbolA, symbolB};
    /** Test data and requests do not depend on entity, so
     * each response must contain identical series for each entity.*/
    final String[] entities = {entityA, entityB};

    /** Periods of test data. */
    final String time00 = "2020-12-01T11:00:00Z"; // first period start
    final String time10 = "2020-12-01T11:10:00Z";
    final String time20 = "2020-12-01T11:20:00Z";
    final String time30 = "2020-12-01T11:30:00Z";
    final String time40 = "2020-12-01T11:40:00Z"; // last period end
    final String time50 = "2020-12-01T11:50:00Z";

    /** These trade data are repeated in each 10-minute interval.
     * (Each 10 minutes trade numbers are incremented by +100.)
     * Frequency - 2 trades (one BUY and one SELL) each minute.
     *                         minute:      0  1  2  3  4  5  6  7  8  9 */
    final int[] buyPrices =        {7, 3, 6, 9, 5, 4, 1, 2, 2, 6};
    final int[] buyVolumes =       {3, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    final int[] buyTradeNumbers =  {1, 4, 5, 7, 9,11,14,15,17,19};
    final int[] sellPrices =       {4, 2, 4, 3, 2, 4, 6, 7, 5, 5};
    final int[] sellVolumes =      {1, 1, 1, 1, 1, 1, 1, 2, 1, 1};
    final int[] sellTradeNumbers = {2, 3, 6, 8,10,12,13,16,18,20};

    /* These arrays are expected in response. */
    final String ohlcvBothSides =      "[7, 9, 1, 5, 23]"; // per period
    final String ohlcvBuy =            "[7, 9, 1, 6, 12]"; // per period
    final String ohlcvSell =           "[4, 7, 2, 5, 11]"; // per period
    final String ohlcvBothSidesTotal = "[7, 9, 1, 5, 46]"; // per period
    final String ohlcvBuyTotal =       "[7, 9, 1, 6, 24]"; // for whole data set
    final String ohlcvSellTotal =      "[4, 7, 2, 5, 22]"; // for whole data set

    public void insertTrades() throws Exception {
        List<Trade> trades = new ArrayList<>();
        addTrades(trades, time00, 0, Trade.Side.BUY, Trade.Side.SELL);
        addTrades(trades, time10, 100, null, null);
        addTrades(trades, time20, 200, Trade.Side.BUY, null);
        addTrades(trades, time30, 300, null, Trade.Side.SELL);
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
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
}
