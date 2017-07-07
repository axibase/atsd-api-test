package com.axibase.tsd.api.model.entitygroup;

import com.axibase.tsd.api.util.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroup {
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String expression;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> tags = new HashMap<>();

    public EntityGroup() {
    }

    public EntityGroup(String name) {
        if (null != name) {
            Registry.EntityGroup.checkExists(name);
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String tagName, String tagValue) {
        tags.put(tagName, tagValue);
    }
}
