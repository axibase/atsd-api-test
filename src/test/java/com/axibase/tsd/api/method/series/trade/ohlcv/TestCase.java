package com.axibase.tsd.api.method.series.trade.ohlcv;

import com.axibase.tsd.api.model.series.query.SeriesQuery;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
class TestCase {
    final SeriesQuery query;
    List<OhlcvSeries> seriesList = new LinkedList<>();

    TestCase series(OhlcvSeries series) {
        seriesList.add(series);
        return this;
    }
}
