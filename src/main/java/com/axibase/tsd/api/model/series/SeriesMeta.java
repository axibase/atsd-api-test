package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.fasterxml.jackson.annotation.*;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeriesMeta {
    private double groupScore;
    private double totalScore;
    private String name;
    private Entity entity;
    private Metric metric;
    private List<SeriesMetaInfo> series;

    @JsonIgnore
    private Map<String, Object> extraFields;

    @JsonAnyGetter
    public Object getField(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "metric":
                return metric;
            default:
                if (extraFields.containsKey(name)) {
                    return extraFields.get(name);
                }
        }

        return null;
    }

    @JsonAnySetter
    private void setField(String name, Object value) {
        switch (name) {
            case "entity":
                entity = (Entity) value;
                break;
            case "metric":
                metric = (Metric) value;
            default:
                extraFields.put(name, value);
        }
    }
}
