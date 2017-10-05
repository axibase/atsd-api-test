package com.axibase.tsd.api.model.series;

import lombok.experimental.Accessors;

import java.util.*;

@Accessors
public class SeriesSearchQuery {
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

    public Map<String, String> toUrlParameters() {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            result.put("query", query);
        }

        if (limit != null) {
            result.put("limit", limit.toString());
        }

        if (offset != null) {
            result.put("offset", offset.toString());
        }

        if (metricTags != null && metricTags.size() > 0) {
            String tags = String.join(",", metricTags);
            result.put("metricTags", tags);
        }

        if (metricFields != null && metricFields.size() > 0) {
            String fields = String.join(",", metricFields);
            result.put("metricFields", fields);
        }

        if (entityTags != null && entityTags.size() > 0) {
            String tags = String.join(",", entityTags);
            result.put("entityTags", tags);
        }

        if (entityFields != null && entityFields.size() > 0) {
            String fields = String.join(",", entityFields);
            result.put("entityFields", fields);
        }

        return result;
    }
}
