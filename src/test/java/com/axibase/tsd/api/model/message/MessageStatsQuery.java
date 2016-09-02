package com.axibase.tsd.api.model.message;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.series.Aggregate;
import com.fasterxml.jackson.annotation.JsonInclude;
import test.listeners.AggregateSampleTest;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageStatsQuery {
    static final String MESSAGE_STATS_METRIC = "message-count";
    private String entity;
    private String type;
    private final String metric = MESSAGE_STATS_METRIC;
    private String startDate;
    private String endDate;
    private String severity;
    private String source;
    private Map<String, String> tags;
    private Interval interval;
    private Aggregate aggregate;

    public Aggregate getAggregate() {
        return aggregate;
    }

    public void setAggregate(Aggregate aggregate) {
        this.aggregate = aggregate;
    }

    public String getMetric() {
        return metric;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
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

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }
}
