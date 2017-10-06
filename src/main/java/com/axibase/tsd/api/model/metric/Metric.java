package com.axibase.tsd.api.model.metric;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.util.Registry;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.prettyPrint;

@Data
@Accessors(chain = true)
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public InterpolationMode getInterpolate() { return interpolate; }

    public Metric setInterpolate(InterpolationMode interpolate) { this.interpolate = interpolate; return this; }

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
    public String toString() {
        return prettyPrint(this);
    }
}
