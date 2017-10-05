package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
@JsonDeserialize(using = SeriesSearchResultRecordDeserializer.class)
public class SeriesSearchResultRecord {
    private Entity entity;
    private Metric metric;
    private Map<String, String> seriesTags;
    private Double relevanceScore;

    public SeriesSearchResultRecord() {

    }

    public SeriesSearchResultRecord(
            Entity entity,
            Metric metric,
            Map<String, String> seriesTags,
            Double relevanceScore) {
        this.entity = entity;
        this.metric = metric;
        this.seriesTags = seriesTags;
        this.relevanceScore = relevanceScore;
    }
}
