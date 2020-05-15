package com.axibase.tsd.api.model.financial;

import lombok.Data;

@Data
public class InstrumentSearchEntry {
    private final String className;
    private final String symbol;
    private final String exchange;
    private final String description;
    private final String entity;

}
