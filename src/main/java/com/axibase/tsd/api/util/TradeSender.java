package com.axibase.tsd.api.util;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.TradeCheck;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.transport.tcp.TCPTradesSender;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TradeSender {

    public static TradeBatchChecker send(Collection<Trade> trades) throws IOException {
        TCPTradesSender.send(trades);
        return new TradeBatchChecker(trades.stream());
    }

    public static TradeBatchChecker send(Trade... trades) throws IOException {
        TCPTradesSender.send(trades);
        return new TradeBatchChecker(Arrays.stream(trades));
    }

    public static class TradeBatchChecker {
        private Stream<Trade> trades;

        private TradeBatchChecker(Stream<Trade> trades) {
            this.trades = trades;
        }

        public void waitUntilTradesInsertedAtMost(long time, TimeUnit timeUnit) {
            trades.map(TradeCheck::new).forEach(check -> Checker.check(check, time, timeUnit));
        }
    }

}