package com.axibase.tsd.api.model.entityGroup;

import com.axibase.tsd.api.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityGroup {
    String name;
    String expression;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, String> tags = new HashMap<>();

    public EntityGroup() {
        if (null != name) {
            Registry.EntityGroup.register(name);
        }
    }

    public EntityGroup(String name) {
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
