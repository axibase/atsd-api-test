package com.axibase.tsd.api.model.property;

import com.axibase.tsd.api.model.DateFilter;
import com.axibase.tsd.api.model.EntityFilter;
import com.axibase.tsd.api.model.ResultFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyQuery {
    private String type;
    private Boolean exactMatch;
    private String keyTagExpression;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> key = new HashMap<>();

    @JsonUnwrapped
    private EntityFilter entityFilter;
    @JsonUnwrapped
    private DateFilter dateFilter;
    @JsonUnwrapped
    private ResultFilter resultFilter;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(Boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getKeyTagExpression() {
        return keyTagExpression;
    }

    public void setKeyTagExpression(String keyTagExpression) {
        this.keyTagExpression = keyTagExpression;
    }

    public EntityFilter getEntityFilter() {
        return entityFilter;
    }

    public void setEntityFilter(EntityFilter entityFilter) {
        this.entityFilter = entityFilter;
    }

    public DateFilter getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(DateFilter dateFilter) {
        this.dateFilter = dateFilter;
    }

    public ResultFilter getResultFilter() {
        return resultFilter;
    }

    public void setResultFilter(ResultFilter resultFilter) {
        this.resultFilter = resultFilter;
    }

    public Map<String, String> getKey() {
        return Collections.unmodifiableMap(key);
    }

    public void setKey(Map<String, String> key) {
        this.key = new HashMap<>(key);
    }
}
