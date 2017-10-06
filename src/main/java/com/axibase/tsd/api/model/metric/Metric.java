package com.axibase.tsd.api.model.metric;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.util.Registry;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.axibase.tsd.api.util.Util.prettyPrint;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric {
    private String name;
    private Boolean enabled;
    private DataType dataType;
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
            Registry.Metric.checkExists(name);
        }
        this.name = name;
    }

    public Metric(String name, Map<String, String> tags) {
        if (name != null) {
            Registry.Metric.checkExists(name);
        }
        this.name = name;
        this.tags = tags;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metric)) return false;
        Metric metric = (Metric) o;
        return Objects.equals(getName(), metric.getName()) &&
                Objects.equals(getEnabled(), metric.getEnabled()) &&
                Objects.equals(getDataType(), metric.getDataType()) &&
                Objects.equals(getPersistent(), metric.getPersistent()) &&
                Objects.equals(getTimePrecision(), metric.getTimePrecision()) &&
                Objects.equals(getRetentionInterval(), metric.getRetentionInterval()) &&
                Objects.equals(getInvalidAction(), metric.getInvalidAction()) &&
                Objects.equals(getLastInsertDate(), metric.getLastInsertDate()) &&
                Objects.equals(getVersioned(), metric.getVersioned()) &&
                Objects.equals(getLabel(), metric.getLabel()) &&
                Objects.equals(getDescription(), metric.getDescription()) &&
                Objects.equals(getInterpolate(), metric.getInterpolate()) &&
                Objects.equals(getTimeZoneID(), metric.getTimeZoneID()) &&
                Objects.equals(getFilter(), metric.getFilter()) &&
                Objects.equals(getTags(), metric.getTags()) &&
                Objects.equals(getAdditionalProperties(), metric.getAdditionalProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEnabled(), getDataType(), getPersistent(), getTimePrecision(),
                getRetentionInterval(), getInvalidAction(), getLastInsertDate(), getVersioned(),
                getLabel(), getDescription(), getInterpolate(), getTimeZoneID(), getFilter(), getTags(),
                getAdditionalProperties());
    }

    @Override
    public String toString() {
        return prettyPrint(this);
    }
}
