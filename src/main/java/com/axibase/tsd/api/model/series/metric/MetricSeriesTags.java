package com.axibase.tsd.api.model.series.metric;

import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@JsonDeserialize(using = MetricSeriesTagsDeserializer.class)
public class MetricSeriesTags {
    private Map<String, List<String>> tags = new HashMap<>();

    public MetricSeriesTags() {
    }

    MetricSeriesTags(Map<String, List<String>> tags) {
        this.tags = tags;
    }

    public MetricSeriesTags addTags(String tagName, String... tagValues) {
        tags.put(tagName, Arrays.asList(tagValues));
        return this;
    }

    public String toString() {
        return Util.prettyPrint(tags);
    }
}
