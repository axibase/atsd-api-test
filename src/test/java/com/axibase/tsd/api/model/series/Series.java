package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.Model;
import com.axibase.tsd.api.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Series {
    private String entity;
    private String metric;
    private ArrayList<Sample> data;
    private Map<String, String> tags;

    public Series() {
    }

    public Series(String entity, String metric) {
        if(null != entity) {
            Registry.Entity.register(entity);
        }
        if(null != metric) {
            Registry.Metric.register(metric);
        }
        this.entity = entity;
        this.metric = metric;
        this.data = new ArrayList<>();
        this.tags = new HashMap<>();
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getEntity() {
        return entity;
    }

    public String getMetric() {
        return metric;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public ArrayList<Sample> getData() {
        return data;
    }

    public void setData(Sample sample) {
        data = new ArrayList<>();
        addData(sample);
    }

    public void setData(ArrayList<Sample> data) {
        this.data = data;
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public void addData(Sample sample) {
        data.add(sample);
    }

    @Override
    public String toString() {
        return "Series{" +
                "entity='" + entity + '\'' +
                ", metric='" + metric + '\'' +
                ", tags=" + tags +
                ", data=" + data +
                '}';
    }
}
