package com.axibase.tsd.api.model.metric;

import com.axibase.tsd.api.model.series.Series;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class MetricSeriesResponse {
    private final String metric;
    private final String entity;
    private final Map<String, String> tags;
    private final String lastInsertDate;

    public MetricSeriesResponse(Series series) {
        this.entity = series.getEntity();
        this.metric = series.getMetric();
        this.tags = series.getTags();
        this.lastInsertDate = series.getData().get(series.getData().size() - 1).getRawDate();
    }
}
