package com.axibase.tsd.api.model.entitygroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class UpdateQuery {
    private String name;
    private String expression;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> tags;
    private Boolean enabled;

    public UpdateQuery(EntityGroup entityGroup) {
        this.name = entityGroup.getName();
        this.expression = entityGroup.getExpression();
        this.tags = entityGroup.getTags();
        this.enabled = entityGroup.getEnabled();
    }

    public void addTag(String tagName, String tagValue) {
        tags.put(tagName, tagValue);
    }
}
