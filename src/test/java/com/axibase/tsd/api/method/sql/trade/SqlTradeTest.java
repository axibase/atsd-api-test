package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TradeSender;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public abstract class SqlTradeTest extends SqlTest {
    private static final AtomicLong TRADE_NUMBER = new AtomicLong(1116137);
    private final String exchange = Mocks.tradeExchange();
    private final String clazz = Mocks.tradeClass();
    private final String symbol = Mocks.tradeSymbol();
    private final String symbolTwo = Mocks.tradeSymbol();
    private final String symbolThree = Mocks.tradeSymbol();

    protected String exchange() {
        return exchange;
    }

    protected String clazz() {
        return clazz;
    }

    protected String symbol() {
        return symbol;
    }

    protected String symbolTwo() {
        return symbolTwo;
    }

    protected String symbolThree() {
        return symbolThree;
    }

    protected void insert(List<Trade> trades) throws Exception {
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    protected void insert(Trade... trades) throws Exception {
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    protected Trade trade(long timestamp) {
        return trade(timestamp, BigDecimal.ONE, 1);
    }

    protected Trade trade(long timestamp, BigDecimal price, long quantity) {
        return new Trade(exchange(), clazz(), symbol(), TRADE_NUMBER.incrementAndGet(), timestamp, price, quantity);
    }

    protected List<Trade> fromISO(String... timestamps) {
        return Arrays.stream(timestamps).map(this::fromISOString).collect(Collectors.toList());
    }

    protected Trade fromISOString(String timestamp) {
        Instant instant = ZonedDateTime.parse(timestamp).toInstant();
        long millis = instant.toEpochMilli();
        long micros = TimeUnit.NANOSECONDS.toMicros(instant.getNano()) % TimeUnit.MILLISECONDS.toMicros(1);
        return trade(millis).setMicroSeconds(micros);
    }

    protected String classCondition() {
        return String.format("exchange='%s' AND class='%s'", exchange(), clazz());
    }

    protected String instrumentCondition() {
        return String.format("exchange='%s' AND class='%s' AND symbol='%s'", exchange(), clazz(), symbol());
    }

    protected String instrumentTwoCondition() {
        return String.format("exchange='%s' AND class='%s' AND symbol='%s'", exchange(), clazz(), symbolTwo());
    }

    protected String entity() {
        return symbol() + "_[" + clazz() + "]";
    }

    protected String entityTwo() {
        return symbolTwo() + "_[" + clazz() + "]";
    }

    protected String entityThree() {
        return symbolThree() + "_[" + clazz() + "]";
    }

    protected class TradeTestConfig<T extends TradeTestConfig> extends SqlTestConfig<T> {

        public TradeTestConfig(String description) {
            super(description);
            instrument(instrumentCondition());
        }

        protected T fields(String fields) {
            setVariable("fields", fields);
            return (T) this;
        }

        protected T instrument(String instrument) {
            setVariable("instrument", instrument);
            return (T) this;
        }
    }
}