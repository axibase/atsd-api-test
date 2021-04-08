package com.axibase.tsd.api.model.financial;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Trade {
    public enum Side {SELL, BUY}

    public enum Session {S, N, L, E, O, I, D, A, a, b, p, C, M, V, X}


    private long number;
    /**
     * Trade time is measured in microseconds.
     * Trade time = timestamp * 1000 + microSeconds.
     * timestamp = trade time / 1000 = count of milliseconds in the trade time.
     * microSeconds = trade time - timestamp * 1000 = last 3 digits of trade time.
     */
    private long timestamp;
    private long microSeconds;
    private String clazz;
    private String symbol;
    private String exchange;
    private Side side;
    private long quantity;
    private BigDecimal price;
    private Long order;
    private Session session;

    public Trade(String exchange, String clazz, String symbol, long number, long timestamp, BigDecimal price, long quantity) {
        this.number = number;
        this.timestamp = timestamp;
        this.clazz = clazz;
        this.symbol = symbol;
        this.exchange = exchange;
        this.quantity = quantity;
        this.price = price;
    }

    public Trade(String exchange, String clazz, String symbol, long number,
                 String isoTime, BigDecimal price, long quantity) {
        this.clazz = clazz;
        this.symbol = symbol;
        this.exchange = exchange;
        this.quantity = quantity;
        this.price = price;
        this.number = number;
        final Instant instant = ZonedDateTime.parse(isoTime).toInstant();
        this.timestamp = instant.toEpochMilli();
        this.microSeconds = TimeUnit.NANOSECONDS.toMicros(instant.getNano()) % TimeUnit.MILLISECONDS.toMicros(1);
    }


    public void validate() {
        checkRequired(number > 0, "Number");
        checkRequired(timestamp > 0, "Timestamp");
        checkRequired(clazz != null, "Class");
        checkRequired(symbol != null, "Symbol");
        checkRequired(exchange != null, "Exchange");
        checkRequired(quantity > 0, "Quantity");
        checkRequired(price != null, "Price");
    }

    private void checkRequired(boolean condition, String field) {
        if (!condition) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    public String toCsvLine() {
        validate();
        String csvSide = side == null ? null : side.name().substring(0, 1);
        return Stream.of(number, timestamp, microSeconds, clazz, symbol, exchange, csvSide, quantity, price, order, session)
                .map(n -> n == null ? "" : n.toString())
                .collect(Collectors.joining(","));
    }

    /** @return Trade timestamp measured in nanoseconds. */
    public long epochNano() {
        return (timestamp * 1000 + microSeconds) * 1000;
    }
}
