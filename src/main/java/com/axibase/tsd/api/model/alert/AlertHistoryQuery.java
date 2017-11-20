package com.axibase.tsd.api.model.alert;


import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertHistoryQuery {
    private String entity;
    private List<String> entities;
    private String entityExpression;
    private String entityGroup;
    private String rule;
    private String metric;
    private String startDate;
    private String endDate;
    private Period interval;
    private Integer limit;

    public Integer getLimit() {
        return limit;
    }

    public AlertHistoryQuery setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }

    public String getEntity() {
        return entity;

    }

    public AlertHistoryQuery setEntity(String entity) {
        this.entity = entity;
        return this;
    }

    public List<String> getEntities() {
        return entities;
    }

    public AlertHistoryQuery setEntities(List<String> entities) {
        this.entities = entities;
        return this;
    }

    public String getEntityExpression() {
        return entityExpression;
    }

    public AlertHistoryQuery setEntityExpression(String entityExpression) {
        this.entityExpression = entityExpression;
        return this;
    }

    public String getEntityGroup() {
        return entityGroup;
    }

    public AlertHistoryQuery setEntityGroup(String entityGroup) {
        this.entityGroup = entityGroup;
        return this;
    }

    public String getRule() {
        return rule;
    }

    public AlertHistoryQuery setRule(String rule) {
        this.rule = rule;
        return this;
    }

    public String getMetric() {
        return metric;
    }

    public AlertHistoryQuery setMetric(String metric) {
        this.metric = metric;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public AlertHistoryQuery setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public AlertHistoryQuery setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public Period getInterval() {
        return interval;
    }

    public AlertHistoryQuery setInterval(Period interval) {
        this.interval = interval;
        return this;
    }
}
