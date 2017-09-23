package com.axibase.tsd.api.method.metric;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MetricSeriesResponse {
    private String metric;
    private String entity;
    private Map<String, String> tags = new HashMap<>();
    private String lastInsertDate;

    public Map<String, Object> getTags() {
        return new HashMap<>(tags);
    }
}
