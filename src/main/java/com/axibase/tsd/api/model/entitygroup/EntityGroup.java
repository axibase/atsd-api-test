package com.axibase.tsd.api.model.entitygroup;

import com.axibase.tsd.api.util.Registry;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
@Getter
@Setter
@NoArgsConstructor
public class EntityGroup {
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expression;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> tags = new HashMap<>();
    private Boolean enabled;

    public EntityGroup(String name) {
        if (null != name) {
            Registry.EntityGroup.checkExists(name);
        }
        this.name = name;
        this.enabled = true;
    }

    public void addTag(String tagName, String tagValue) {
        tags.put(tagName, tagValue);
    }
}
