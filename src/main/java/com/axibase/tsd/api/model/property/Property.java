package com.axibase.tsd.api.model.property;

import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.prettyPrint;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Property {
    private String type;
    private String entity;
    private Map<String, String> key;
    private Map<String, String> tags;
    private String date;

    public Property(String type, String entity) {
        if (type != null) {
            Registry.Type.checkExists(type);
        }
        if (entity != null) {
            Registry.Entity.checkExists(entity);
        }
        this.type = type;
        this.entity = entity;
    }

    public Property(Property oldProperty) {
        setType(oldProperty.getType());
        setEntity(oldProperty.getEntity());
        setKey(oldProperty.getKey());
        setTags(oldProperty.getTags());
        setDate(oldProperty.getDate());
    }

    public Property addTag(String tagName, String tagValue) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(tagName, tagValue);
        return this;
    }

    public Property addKey(String keyName, String keyValue) {
        if (key == null) {
            key = new HashMap<>();
        }
        key.put(keyName, keyValue);
        return this;
    }

    public Map<String, String> getKey() {
        if (null == key) {
            return null;
        }
        return new HashMap<>(key);
    }

    public Map<String, String> getTags() {
        if (null == tags) {
            return null;
        }
        return new HashMap<>(tags);
    }

    @JsonProperty
    public Property setDate(String date) {
        this.date = date;
        return this;
    }

    public Property setDate(long millis) {
        this.setDate(new Date(millis));
        return this;
    }

    public Property setDate(Date date) {
        this.setDate(Util.ISOFormat(date));
        return this;
    }

    @Override
    public String toString() {
        return prettyPrint(this);
    }
}
