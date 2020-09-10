package com.axibase.tsd.api.model.financial;

import com.axibase.tsd.api.model.property.Property;

public class TradeSessionSummary extends Property {
    public static final String TYPE = "statistics";

    public TradeSessionSummary(String clazz, String symbol, TradeSessionType session, TradeSessionStage stage, String date) {
        setType(TYPE);
        setEntity(symbol + "_[" + clazz + "]");
        addKey("session", session.toString());
        addKey("stage", stage.toString());
        setDate(date);
    }

}