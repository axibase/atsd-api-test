package com.axibase.tsd.api.model.entity;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.axibase.tsd.api.Util.prettyPrint;

/**
 * @author Dmitry Korchagin.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {
    private String name;
    private InterpolationMode interpolationMode;
    private String label;
    private Date lastInsertDate;
    private Map<String, String> tags;
    private Boolean enabled;
    private String timeZoneID;

    public Entity() {

    }

    public Entity(String name) {
        if (null != name) {
            Registry.Entity.register(name);
        }
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastInsertDate() {
        if (null == lastInsertDate) {
            return null;
        }
        return new Date(lastInsertDate.getTime());
    }

    public void setLastInsertDate(Date lastInsertDate) {
        this.lastInsertDate = new Date(lastInsertDate.getTime());
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String tagName, String tagValue) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(tagName, tagValue);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    @JsonProperty("interpolate")
    public InterpolationMode getInterpolationMode() {
        return interpolationMode;
    }

    public void setInterpolationMode(String interpolationMode) {
        this.interpolationMode = InterpolationMode.valueOf(interpolationMode);
    }

    @JsonProperty("interpolate")
    public void setInterpolationMode(InterpolationMode interpolationMode) {
        this.interpolationMode = interpolationMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return Objects.equals(getName(), entity.getName()) &&
                getInterpolationMode() == entity.getInterpolationMode() &&
                Objects.equals(getLabel(), entity.getLabel()) &&
                Objects.equals(getLastInsertDate(), entity.getLastInsertDate()) &&
                Objects.equals(getTags(), entity.getTags()) &&
                Objects.equals(getEnabled(), entity.getEnabled()) &&
                Objects.equals(getTimeZoneID(), entity.getTimeZoneID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getInterpolationMode(), getLabel(), getLastInsertDate(), getTags(), getEnabled(), getTimeZoneID());
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
    public void setTimeZoneID(String timeZoneID) {
        this.timeZoneID = timeZoneID;
    }
}
