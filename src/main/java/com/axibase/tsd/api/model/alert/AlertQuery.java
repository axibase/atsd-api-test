package com.axibase.tsd.api.model.alert;


import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertQuery {
    private List<String> rules;
    private List<String> metrics;
    private List<String> severities;
    private String minSeverity;
    private Boolean acknowledged;
    private String entity;
    private List<String> entities;
    private String entityGroup;
    private String entityExpression;
    private String startDate;
    private String endDate;
    private Interval interval;
    private Integer id;

    public AlertQuery(String entity) {
        this.entity = entity;
    }

    public AlertQuery(String entity, long startDateMillis, long endDateMillis) {
        this.entity = entity;
        this.startDate = Util.ISOFormat(startDateMillis);
        this.endDate = Util.ISOFormat(endDateMillis);
    }

    public void addMetric(String metric) {
        if (metrics == null)
            metrics = new ArrayList<>();
        metrics.add(metric);
    }

    public void addRule(String rule) {
        if (rules == null)
            rules = new ArrayList<>();
        rules.add(rule);
    }

    public void addSeverity(String severity) {
        if (severities == null)
            severities = new ArrayList<>();
        severities.add(severity);
    }

    public void addEntity(String entity) {
        if (entities == null)
            entities = new ArrayList<>();
        entities.add(entity);
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
