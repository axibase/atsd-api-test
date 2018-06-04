package com.axibase.tsd.api.model.property;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
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

    public void addKey(String keyName, String keyValue) {
        if(key == null) {
            key = new HashMap<>();
        }
        key.put(keyName, keyValue);
    }
}
