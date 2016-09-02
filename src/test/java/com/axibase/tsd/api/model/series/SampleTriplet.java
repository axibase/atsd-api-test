package com.axibase.tsd.api.model.series;

import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;

/**
 * @author Dmitry Korchagin.
 */
public class SampleTriplet extends Triple<String, String, BigDecimal> {
    private String entity;
    private String date;
    private BigDecimal value;

    public SampleTriplet(String entity, String date, BigDecimal value) {
        this.entity = entity;
        this.date = date;
        this.value = value;
    }

    public SampleTriplet(String entity, String date, Integer value) {
        this.entity = entity;
        this.date = date;
        this.value = new BigDecimal(value);
    }

    @Override
    public String getLeft() {
        return entity;
    }

    @Override
    public String getMiddle() {
        return date;
    }

    @Override
    public BigDecimal getRight() {
        return value;
    }

}
