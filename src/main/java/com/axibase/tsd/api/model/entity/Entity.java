package com.axibase.tsd.api.model.entity;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.util.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.prettyPrint;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {
    private String name;
    private InterpolationMode interpolationMode;
    private String label;
    private String lastInsertDate;
    private String createdDate;
    private Map<String, String> tags;
    private Boolean enabled;
    private String timeZoneID;

    public Entity() {

    }

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
