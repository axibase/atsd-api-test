package com.axibase.tsd.api.model.series.search;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = SeriesSearchResultRecordDeserializer.class)
public class SeriesSearchResultRecord {
    private Entity entity;
    private Metric metric;
    private Map<String, String> seriesTags;
    private double relevanceScore;
}
