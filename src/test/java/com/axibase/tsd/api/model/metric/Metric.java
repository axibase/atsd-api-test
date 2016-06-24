package com.axibase.tsd.api.model.metric;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.series.DataType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric {
    private String name;
    private String enabled;
    private String persistent;
    private DataType dataType;
    private String timePrecision;
    private String retentionInterval;

    public Metric() {
    }

    public Metric(String name) {
        if (name != null) {
            Registry.Metric.register(name);
        }
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getPersistent() {
        return persistent;
    }

    public void setPersistent(String persistent) {
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
