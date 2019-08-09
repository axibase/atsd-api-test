package com.axibase.tsd.api.model.entity;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class EntityMethodGroupResponse {
    private final String name;
    private final Map<String, String> tags;

    public EntityMethodGroupResponse(EntityGroup group) {
        this.name = group.getName();
        this.tags = group.getTags();
    }
}
