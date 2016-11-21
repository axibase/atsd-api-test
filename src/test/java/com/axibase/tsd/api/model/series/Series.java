package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Series {
    private String entity;
    private String metric;
    private List<Sample> data;
    private Map<String, String> tags;

    public Series() {
        data = new ArrayList<>();
        tags = new HashMap<>();
    }

    public Series(String entity, String metric) {
        if (null != entity) {
            Registry.Entity.register(entity);
        }
        if (null != metric) {
            Registry.Metric.register(metric);
        }
        this.entity = entity;
        this.metric = metric;
        this.data = new ArrayList<>();
        this.tags = new HashMap<>();
    }

    public Series copy() {
        Series copy = new Series();
        copy.setEntity(entity);
        copy.setMetric(metric);
        copy.setData(new ArrayList<>(data));
        copy.setTags(new HashMap<>(tags));
        return copy;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @JsonIgnore
    public Map<String, String> getFormattedTags() {
        Map<String, String> formattedTags = new HashMap<>();
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            formattedTags.put(tag.getKey().toLowerCase().trim(), tag.getValue().trim());
        }
        return formattedTags;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public List<Sample> getData() {
        return data;
    }

    public void setData(List<Sample> data) {
        this.data = data;
    }

    public void addTag(String key, String value) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(key, value);
    }

    public void addData(Sample sample) {
        if (data == null) {
            data = new ArrayList<>();
        }
        data.add(sample);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Series series = (Series) o;

        if (entity != null ? !entity.equals(series.entity) : series.entity != null) return false;
        if (metric != null ? !metric.equals(series.metric) : series.metric != null) return false;
        if (data != null ? !data.equals(series.data) : series.data != null) return false;
        return tags != null ? tags.equals(series.tags) : series.tags == null;

    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        result = 31 * result + (metric != null ? metric.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }
}
