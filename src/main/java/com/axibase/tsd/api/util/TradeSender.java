package com.axibase.tsd.api.util;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.TradeCheck;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.transport.tcp.TCPTradesSender;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class TradeSender {

    public static TradeBatchChecker send(Collection<Trade> trades) throws IOException {
        TCPTradesSender.send(trades);
        return new TradeBatchChecker(trades);
    }

    public static TradeBatchChecker send(Trade... trades) throws IOException {
        return send(Arrays.asList(trades));
    }

    @RequiredArgsConstructor
    public static class TradeBatchChecker {
        private final Collection<Trade> trades;

        public void waitUntilTradesInsertedAtMost(long time, TimeUnit timeUnit) {
            trades.stream().map(TradeCheck::new).forEach(check -> Checker.check(check, time, timeUnit));
        }
    }

}