package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.MethodParameters;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricSeriesParameters extends MethodParameters {
    private String entity;
    private Map<String, String> tags = new HashMap<>();
    private String minInsertDate;
    private String maxInsertDate;
    private Integer limit;

    public MetricSeriesParameters addTag(String name, String value) {
        tags.put(name, value);
        return this;
    }

    public MetricSeriesParameters setTags(Map<String, String> tags) {
        this.tags = new HashMap<>(tags);
        return this;
    }

    public Map<String, String> getTags() {
        return new HashMap<>(tags);
    }

    @Override
    public Map<String, Object> toParameterMap() {
        Map<String, Object> parameters = this.toMap();
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            parameters.put("tags." + tag.getKey(), tag.getValue());
        }
        parameters.remove("tags");
        return Collections.unmodifiableMap(parameters);
    }
}
