package com.axibase.tsd.api.model.message;

import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    private String entity;
    private String type;
    private String source;
    private String date;
    private String severity;
    private String message;
    private Boolean persist;
    private Map<String, String> tags = new HashMap<>();

    public Message(String entity) {
        this(entity, true);
    }

    public Message(String entity, boolean checkThatEntityDoesNotExistsInAtsd) {
        if (checkThatEntityDoesNotExistsInAtsd && entity != null) {
            Registry.Entity.checkExists(entity);
        }
        this.entity = entity;
    }

    public Message(String entity, String type) {
        if (entity != null) {
            Registry.Entity.checkExists(entity);
        }
        if (type != null) {
            Registry.Type.checkExists(type);
        }
        this.entity = entity;
        this.type = type;
    }

    public Message setDate(String date) {
        this.date = date;
        return this;
    }

    public Message setDate(Date date) {
        this.date = Util.ISOFormat(date);
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public void setTags(Map<String, String> tags) {
        this.tags = new HashMap<>(tags);
    }

    public String getStringField(String fieldName) {
        switch(fieldName) {
            case "entity":
                return entity;
            case "type":
                return type;
            case "source":
                return source;
            case "severity":
                return severity;
            case "message":
                return message;
            default:
                throw new IllegalArgumentException("There are no String field with name: " + fieldName);
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "entity='" + entity + '\'' +
                ", type='" + type + '\'' +
                ", source='" + source + '\'' +
                ", date='" + date + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", persist=" + persist +
                ", tags=" + tags +
                '}';
    }
}
