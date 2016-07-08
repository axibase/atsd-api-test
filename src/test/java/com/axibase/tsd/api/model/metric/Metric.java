package com.axibase.tsd.api.model.metric;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.series.DataType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.org.apache.xpath.internal.operations.Bool;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric {
    private String name;
    private Boolean enabled;
    private DataType dataType;
    private Boolean counter;
    private Boolean persistent;
    private String timePrecision;
    private String retentionInterval;
    private String invalidAction;
    private String lastInsertDate;
    private Boolean versioned;

    public Metric() {
    }

    public Metric(String name) {
        if (name != null) {
            Registry.Metric.register(name);
        }
        this.name = name;
    }

    public String getInvalidAction() {
        return invalidAction;
    }

    public void setInvalidAction(String invalidAction) {
        this.invalidAction = invalidAction;
    }

    public String getLastInsertDate() {
        return lastInsertDate;
    }

    public void setLastInsertDate(String lastInsertDate) {
        this.lastInsertDate = lastInsertDate;
    }

    public Boolean getVersioned() {
        return versioned;
    }

    public void setVersioned(Boolean versioned) {
        this.versioned = versioned;
    }

    public Boolean getCounter() {
        return counter;
    }

    public void setCounter(Boolean counter) {
        this.counter = counter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public void setPersistent(Boolean persistent) {
        this.persistent = persistent;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getTimePrecision() {
        return timePrecision;
    }

    public void setTimePrecision(String timePrecision) {
        this.timePrecision = timePrecision;
    }

    public String getRetentionInterval() {
        return retentionInterval;
    }

    public void setRetentionInterval(String retentionInterval) {
        this.retentionInterval = retentionInterval;
    }
}
