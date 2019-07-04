package com.axibase.tsd.api.model.series.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

import java.util.Map;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class SeriesSettings {
    String name;
    String metric;
    String entity;
    String[] entities;
    String entityGroup;
    String entityExpression;
    Map<String, String[]> tags;
    Boolean exactMatch;
    String tagExpression;

    public static SeriesSettings of(String name, String metric, String entity) {
        return new SeriesSettings().withName(name).withMetric(metric).withEntity(entity);
    }
}
