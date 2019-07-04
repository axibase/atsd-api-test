package com.axibase.tsd.api.model.series.query.transformation;

public enum Transformation {
    // here is default ordering (left to right) in which transformations are applied if order is not specified in query
    INTERPOLATE, GROUP, RATE, AGGREGATE, SMOOTH, DOWNSAMPLE, FORECAST, EVALUATE;
}
