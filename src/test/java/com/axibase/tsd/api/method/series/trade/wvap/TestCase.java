package com.axibase.tsd.api.method.series.trade.wvap;

import com.axibase.tsd.api.model.series.query.SeriesQuery;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class TestCase {
    final SeriesQuery query;
    List<ExpectedSeries> seriesList = new LinkedList<>();

    TestCase series(ExpectedSeries series) {
        seriesList.add(series);
        return this;
    }
}
