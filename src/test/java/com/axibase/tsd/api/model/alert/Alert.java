package com.axibase.tsd.api.model.alert;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alert {
    private Integer id;
    private Boolean acknowledged;
    private String entity;
    private String metric;
    private String rule;
    //TODO replace to ENUM
    private String severity;
    private Map<String, String> tags;
    private Integer repeatCount;
    private String textValue;
    private Double value;
    private Double openValue;
    private String openDate;
    private String lastEventDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
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

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getOpenValue() {
        return openValue;
    }

    public void setOpenValue(Double openValue) {
        this.openValue = openValue;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getLastEventDate() {
        return lastEventDate;
    }

    public void setLastEventDate(String lastEventDate) {
        this.lastEventDate = lastEventDate;
    }
}
