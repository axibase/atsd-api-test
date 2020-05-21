package com.axibase.tsd.api.model.financial;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Trade {
    public enum Side {SELL, BUY}

    private long number;
    private long timestamp;     // trade time in microseconds truncated to milliseconds = trade time with last 3 digits removed
    private long microSeconds;  // (trade time in microseconds - timestamp * 1000) = last 3 digits of trade time
    private String clazz;
    private String symbol;
    private String exchange;
    private Side side;
    private long quantity;
    private BigDecimal price;
    private Long order;

    public Trade(String exchange, String clazz, String symbol, long number, long timestamp, BigDecimal price, long quantity) {
        this.number = number;
        this.timestamp = timestamp;
        this.clazz = clazz;
        this.symbol = symbol;
        this.exchange = exchange;
        this.quantity = quantity;
        this.price = price;
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
        return Stream.of(number, timestamp, microSeconds, clazz, symbol, exchange, csvSide, quantity, price, order)
                .map(n -> n == null ? "" : n.toString())
                .collect(Collectors.joining(","));
    }
}