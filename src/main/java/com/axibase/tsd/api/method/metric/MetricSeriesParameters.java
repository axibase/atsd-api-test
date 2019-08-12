package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.MethodParameters;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true)
@Setter @Getter
public class MetricSeriesParameters extends MethodParameters {
    private String entity;
    private Map<String, String> tags = new HashMap<>();
    private String minInsertDate;
    private String maxInsertDate;

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
        Map<String, String> tagMap = (Map)parameters.get("tags"); //getting tag map to rewrite it to format tags.name=value
        for(Map.Entry<String, String> tag : tagMap.entrySet()) {
            parameters.put("tags." + tag.getKey(), tag.getValue());
        }
        parameters.remove("tags");
        return Collections.unmodifiableMap(parameters);
    }
}
