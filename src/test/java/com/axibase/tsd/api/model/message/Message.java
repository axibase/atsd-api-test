package com.axibase.tsd.api.model.message;

import com.axibase.tsd.api.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    public Message() {}

    public Message(String entity) {
        if (entity != null) {
            Registry.Entity.register(entity);
        }
        this.entity = entity;
    }

    public Message(String entity, String type) {
        if (entity != null) {
            Registry.Entity.register(entity);
        }
        if (type != null) {
            Registry.Type.register(type);
        }
        this.entity = entity;
        this.type = type;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getPersist() {
        return persist;
    }

    public void setPersist(Boolean persist) {
        this.persist = persist;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public void setTags(Map<String, String> tags) {
        this.tags = new HashMap<>(tags);
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
