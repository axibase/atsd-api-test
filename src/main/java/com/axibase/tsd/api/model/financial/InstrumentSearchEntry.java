package com.axibase.tsd.api.model.financial;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentSearchEntry {
    private final String className;
    private final String symbol;
    private final String exchange;
    private final String description;
    private final String entity;

}
