package com.axibase.tsd.api.model.series.search;

import com.axibase.tsd.api.method.MethodParameters;
import lombok.Data;

import java.util.*;

@Data
public class SeriesSearchQuery extends MethodParameters {
    private String query;
    private Integer limit;
    private Integer offset;
    private List<String> metricTags;
    private List<String> metricFields;
    private List<String> entityTags;
    private List<String> entityFields;

    public SeriesSearchQuery(String query) {
        this.query = query;
    }

    public void addMetricTags(String... tags) {
        if (metricTags == null) {
            metricTags = new ArrayList<>();
        }
        Collections.addAll(metricTags, tags);
    }

    public void addMetricFields(String... fields) {
        if (metricFields == null) {
            metricFields = new ArrayList<>();
        }
        Collections.addAll(metricFields, fields);
    }

    public void addEntityTags(String... tags) {
        if (entityTags == null) {
            entityTags = new ArrayList<>();
        }
        Collections.addAll(entityTags, tags);
    }

    public void addEntityFields(String... fields) {
        if (entityFields == null) {
            entityFields = new ArrayList<>();
        }
        Collections.addAll(entityFields, fields);
    }
}
