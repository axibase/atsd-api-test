package com.axibase.tsd.api.util;

public class TradeUtil {

    public static String tradePriceMetric() {
        return "trade_price";
    }

    public static String tradeQuantityMetric() {
        return "trade_quantity";
    }
    public static String tradeEntity(String symbol, String clazz) {
        return symbol.toLowerCase() + "_[" + clazz.toLowerCase() + "]";
    }
}
