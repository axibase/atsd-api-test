package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.Util;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeriesQuery {
    private String entity;
    private String metric;
    private String startDate;
    private String endDate;
    private Map<String, String> tags = new HashMap<>();
    private Map<String, Object> aggregate;

    public void addAggregateType(String type) {
        if (aggregate == null) {
            aggregate = new HashMap<>();
        }
        Object o = aggregate.get("types");
        if (o == null) {
            o = new ArrayList<>();
        }
        ArrayList<String> types = (ArrayList<String>) o;
        types.add(type);
        aggregate.put("types", types);
    }

    public Map<String, Object> getAggregate() {
        return aggregate;
    }

    public void setAggregatePeriod(int count, String unit) {
        if (aggregate == null) {
            aggregate = new HashMap<>();
        }
        Object o = aggregate.get("period");
        if (o == null) {
            o = new HashMap<>();
        }
        Map period = (HashMap) o;
        period.put("count", count);
        period.put("unit", unit);
        aggregate.put("period", period);
    }

    public SeriesQuery(String entity, String metric, long startTime, long endTime) {
        this.entity = entity;
        this.metric = metric;
        this.startDate = Util.ISOFormat(startTime);
        this.endDate = Util.ISOFormat(endTime);
    }

    public SeriesQuery(String entity, String metric, String startDate, String endDate) {
        this.entity = entity;
        this.metric = metric;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public SeriesQuery(String entity, String metric, String startDate, String endDate, Map<String, String> tags) {
        this.entity = entity;
        this.metric = metric;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tags = tags;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }


    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(String tag, String value) {
        tags.put(tag, value);
    }

    @Override
    public String toString() {
        return "Query{" +
                "entity='" + entity + '\'' +
                ", metric='" + metric + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}
