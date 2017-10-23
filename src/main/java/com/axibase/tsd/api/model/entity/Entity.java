package com.axibase.tsd.api.model.entity;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.series.DateDeserializer;
import com.axibase.tsd.api.util.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.prettyPrint;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {
    private String name;
    private InterpolationMode interpolationMode;
    private String label;
    @JsonDeserialize(using = DateDeserializer.class)
    private ZonedDateTime lastInsertDate;
    @JsonDeserialize(using = DateDeserializer.class)
    private ZonedDateTime createdDate;
    private Map<String, String> tags;
    private Boolean enabled;
    private String timeZoneID;

    public Entity(String name) {
        if (null != name) {
            Registry.Entity.checkExists(name);
        }
        this.name = name;
    }

    public Entity(String name, Map<String, String> tags) {
        if (null != name) {
            Registry.Entity.checkExists(name);
        }
        this.name = name;
        this.tags = tags;
    }

    public void addTag(String tagName, String tagValue) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(tagName, tagValue);
    }


    @JsonProperty("interpolate")
    public InterpolationMode getInterpolationMode() {
        return interpolationMode;
    }

    public Entity setInterpolationMode(String interpolationMode) {
        this.interpolationMode = InterpolationMode.valueOf(interpolationMode);
        return this;
    }

    @JsonProperty("interpolate")
    public Entity setInterpolationMode(InterpolationMode interpolationMode) {
        this.interpolationMode = interpolationMode;
        return this;
    }

    @Override
    public String toString() {
        return prettyPrint(this);
    }


    @JsonProperty("timeZone")
    public String getTimeZoneID() {
        return timeZoneID;
    }

    @JsonProperty("timeZone")
    public Entity setTimeZoneID(String timeZoneID) {
        this.timeZoneID = timeZoneID;
        return this;
    }
}
