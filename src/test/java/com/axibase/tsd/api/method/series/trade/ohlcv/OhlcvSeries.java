package com.axibase.tsd.api.method.series.trade.ohlcv;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class OhlcvSeries {
    final String entity;
    final String side;
    final String aggregation;
    final String group;
    List<MultiValueSample> samples = new ArrayList<>();

    public OhlcvSeries(String entity, String side) {
        this.entity = entity;
        this.side = side;
        aggregation = "OHLCV";
        group = null;
    }

    OhlcvSeries sample(String date, String values) {
        samples.add(new MultiValueSample(date, values));
        return this;
    }
}
