package com.axibase.tsd.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @see <a href="https://github.com/axibase/atsd-docs/blob/master/api/data/filter-entity.md#entity-filter-fields">api docs</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityFilter {
    private String entity;
    private List<String> entities;
    private String entityGroup;
    private String entityExpression;

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
}
