package com.axibase.tsd.api.model.metric;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.serialization.DateDeserializer;
import com.axibase.tsd.api.util.Registry;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.prettyPrint;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric {
    private String name;
    private Boolean enabled;
    private DataType dataType;
    private Boolean persistent;
    @JsonDeserialize(using = DateDeserializer.class)
    private ZonedDateTime createdDate;
    private Integer retentionDays;
    private Integer seriesRetentionDays;
    private String invalidAction;
    @JsonDeserialize(using = DateDeserializer.class)
    private ZonedDateTime lastInsertDate;
    private Boolean versioned;
    private String label;
    private String description;
    private InterpolationMode interpolate;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String timeZoneID;
    private String filter;
    private String units;
    private Map<String, String> tags;
    private Map<String, Object> additionalProperties = new HashMap<>();

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


    public Metric addTag(String tagName, String tagValue) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(tagName, tagValue);

        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Metric setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    public InterpolationMode getInterpolate() { return interpolate; }

    public Metric setInterpolate(InterpolationMode interpolate) { this.interpolate = interpolate; return this; }

    public Metric setInterpolate(String interpolate) {
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
        return this;
    }

    @JsonProperty("timeZone")
    public String getTimeZoneID() {
        return timeZoneID;
    }

    @JsonProperty("timeZone")
    public Metric setTimeZoneID(String timeZoneID) {
        this.timeZoneID = timeZoneID;
        return this;
    }

    @Override
    public String toString() {
        return prettyPrint(this);
    }
}
