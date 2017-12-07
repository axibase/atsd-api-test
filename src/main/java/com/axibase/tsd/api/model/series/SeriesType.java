package com.axibase.tsd.api.model.series;

public enum SeriesType {
    HISTORY, FORECAST;

    public SeriesQueryType toQueryType() {
        switch (this) {
            case HISTORY:
                return SeriesQueryType.HISTORY;
            case FORECAST:
                return SeriesQueryType.FORECAST;
            default:
                throw new IllegalStateException("Unknown series type");
        }
    }
}
