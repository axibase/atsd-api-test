package com.axibase.tsd.api.model.property;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Property {
    private String type;
    private String entity;
    private Map<String, String> key;
    private Map<String, String> tags;
    private Date date;

    public Property() {
    }

    public Property(String type, String entity) {
        if (type != null)
            Registry.Type.register(type);
        if (entity != null)
            Registry.Entity.register(entity);
        this.type = type;
        this.entity = entity;
    }

    public void addTag(String tagName, String tagValue) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(tagName, tagValue);
    }

    public void addKey(String keyName, String keyValue) {
        if (key == null) {
            key = new HashMap<>();
        }
        key.put(keyName, keyValue);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Map<String, String> getKey() {
        if (null == key) {
            return null;
        }
        return new HashMap<>(key);
    }

    public void setKey(Map<String, String> key) {
        this.key = key;
    }

    public Map<String, String> getTags() {
        if (null == tags) {
            return null;
        }
        return new HashMap<>(tags);
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getDate() {
        if (date == null) {
            return null;
        }
        return Util.ISOFormat(date);
    }

    public void setDate(Long millis) {
        this.date = new Date(millis);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) throws ParseException {
        this.date = Util.getDate(date);
    }

    @Override
    public String toString() {
        return "{" +
                "type='" + getType() + '\'' +
                ", entity='" + getEntity() + '\'' +
                ", key=" + getKey() +
                ", tags=" + getTags() +
                ", date=" + getDate() +
                '}';
    }
}
