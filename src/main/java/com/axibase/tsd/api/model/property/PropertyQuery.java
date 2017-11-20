package com.axibase.tsd.api.model.property;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyQuery {
    private String type;
    private String entity;
    private List<String> entities;
    private String entityGroup;
    private String entityExpression;
    private Map<String, String> key = new HashMap<>();
    private String keyTagExpression;
    private String startDate;
    private String endDate;
    private Period interval;

    private Boolean exactMatch;

    private Integer limit;
    private Boolean last;
    private Integer offset;

    public PropertyQuery() {

    }

    public PropertyQuery(String type, String entity) {
        this(type, entity, null);
    }

    public PropertyQuery(String type, String entity, Map<String, String> key) {
        this.type = type;
        this.entity = entity;
        if(key != null) {
            this.key = new HashMap<>(key);
        }
    }


    public Period getInterval() {
        return interval;
    }

    public void setInterval(Period interval) {
        this.interval = interval;
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

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public String getEntityGroup() {
        return entityGroup;
    }

    public void setEntityGroup(String entityGroup) {
        this.entityGroup = entityGroup;
    }

    public String getEntityExpression() {
        return entityExpression;
    }

    public void setEntityExpression(String entityExpression) {
        this.entityExpression = entityExpression;
    }

    public Map<String, String> getKey() {
        return key;
    }

    public void setKey(Map<String, String> key) {
        this.key = key;
    }

    public void addKey(String keyName, String keyValue) {
        if(key == null) {
            key = new HashMap<>();
        }
        key.put(keyName, keyValue);
    }

    public String getKeyTagExpression() {
        return keyTagExpression;
    }

    public void setKeyTagExpression(String keyTagExpression) {
        this.keyTagExpression = keyTagExpression;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(Boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
