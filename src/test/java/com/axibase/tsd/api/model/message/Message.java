package com.axibase.tsd.api.model.message;

import com.axibase.tsd.api.model.Model;
import com.axibase.tsd.api.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message extends Model {
    private String entity;
    private String type;
    private String source;
    private String date;
    private String severity;
    private String message;
    private Boolean persist;

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
}
