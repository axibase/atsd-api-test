package com.axibase.tsd.api.model.metric;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.model.series.DataType;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

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
    private String label;
    private String description;
    private InterpolationMode interpolate;
    private String timeZoneID;
    private String filter;
    private Map<String, String> tags;
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Metric() {
    }

    public Metric(String name) {
        if (name != null) {
            Registry.Metric.register(name);
        }
        this.name = name;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getInvalidAction() {
        return invalidAction;
    }

    public Metric setInvalidAction(String invalidAction) {
        this.invalidAction = invalidAction;
        return this;
    }

    public String getLastInsertDate() {
        return lastInsertDate;
    }

    public Metric setLastInsertDate(String lastInsertDate) {
        this.lastInsertDate = lastInsertDate;
        return this;
    }

    public Boolean getVersioned() {
        return versioned;
    }

    public Metric setVersioned(Boolean versioned) {
        this.versioned = versioned;
        return this;
    }

    public Boolean getCounter() {
        return counter;
    }

    public Metric setCounter(Boolean counter) {
        this.counter = counter;
        return this;
    }

    public String getName() {
        return name;
    }

    public Metric setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Metric setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public Metric setPersistent(Boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Metric setDataType(DataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public String getTimePrecision() {
        return timePrecision;
    }

    public Metric setTimePrecision(String timePrecision) {
        this.timePrecision = timePrecision;
        return this;

    }

    public String getRetentionInterval() {
        return retentionInterval;
    }

    public Metric setRetentionInterval(String retentionInterval) {
        this.retentionInterval = retentionInterval;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;

    }

    public Metric setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InterpolationMode getInterpolate() {
        return interpolate;
    }

    public void setInterpolate(InterpolationMode interpolate) {
        this.interpolate = interpolate;
    }

    public void setInterpolate(String interpolate) {
        switch (interpolate) {
            case "LINEAR":
                this.interpolate = InterpolationMode.LINEAR;
                break;
            case "PREVIOUS":
                this.interpolate = InterpolationMode.PREVIOUS;
                break;
            default:
                throw new IllegalStateException(String.format("Incorrect interpolate type: %s", interpolate));
        }
    }

    @JsonProperty("timeZone")
    public String getTimeZoneID() {
        return timeZoneID;
    }

    @JsonProperty("timeZone")
    public void setTimeZoneID(String timeZoneID) {
        this.timeZoneID = timeZoneID;
    }
}
