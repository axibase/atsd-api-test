package com.axibase.tsd.api.model.entity;

import com.axibase.tsd.api.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {
    private String name;
    private Date lastInsertDate;
    private Map<String, String> tags;
    private Boolean enabled;

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
        return (Date) lastInsertDate.clone();
    }

    public void setLastInsertDate(Date lastInsertDate) {
        this.lastInsertDate = new Date(lastInsertDate.getTime());
    }

    public Map<String, String> getTags() {
        if (tags == null) {
            return null;
        }
        return new HashMap<>(tags);
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
}
