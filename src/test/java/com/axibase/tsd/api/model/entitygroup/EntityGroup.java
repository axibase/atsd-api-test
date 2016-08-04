package com.axibase.tsd.api.model.entitygroup;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * @author Igor Shmagrinskiy
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityGroup {
    private String name;
    private Map<String, String> tags;

    public String getName() {
        return name;
    }

    public EntityGroup setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public EntityGroup setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
