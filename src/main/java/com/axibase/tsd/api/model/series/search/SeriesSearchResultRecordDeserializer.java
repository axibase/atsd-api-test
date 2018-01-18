package com.axibase.tsd.api.model.series.search;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

class SeriesSearchResultRecordDeserializer extends JsonDeserializer<SeriesSearchResultRecord> {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public SeriesSearchResultRecord deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        SeriesSearchResultRecord result = new SeriesSearchResultRecord();

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        JsonNode metricNameNode = node.get(0);
        String metricName = null;
        if (metricNameNode != null) {
            metricName = metricNameNode.textValue();
        }

        JsonNode metricLabelNode = node.get(1);
        String metricLabel = null;
        if (metricLabelNode != null) {
            metricLabel = metricLabelNode.textValue();
        }

        Metric metric = null;

        JsonNode metricNode = node.get(2);
        if (metricNode != null) {
            metric = mapper.convertValue(metricNode, Metric.class);
        }

        if (metric == null) {
            metric = new Metric();
        }
        metric.setName(metricName);
        metric.setLabel(metricLabel);

        Map<String, String> metricTags = null;
        JsonNode metricTagsNode = node.get(3);
        if (metricTagsNode != null) {
            metricTags = mapper.convertValue(metricTagsNode, Map.class);
        }
        if (metricTags != null && metricTags.size() != 0) {
            metric.setTags(metricTags);
        }

        result.setMetric(metric);

        JsonNode entityNameNode = node.get(4);
        String entityName = null;
        if (entityNameNode != null) {
            entityName = entityNameNode.textValue();
        }

        JsonNode entityLabelNode = node.get(5);
        String entityLabel = null;
        if (entityLabelNode != null) {
            entityLabel = entityLabelNode.textValue();
        }

        Entity entity = null;

        JsonNode entityNode = node.get(6);
        if (entityNode != null) {
            entity = mapper.convertValue(entityNode, Entity.class);
        }

        if (entity == null) {
            entity = new Entity();
        }
        entity.setName(entityName);
        entity.setLabel(entityLabel);

        Map<String, String> entityTags = null;
        JsonNode entityTagsNode = node.get(7);
        if (entityTagsNode != null) {
            entityTags = mapper.convertValue(entityTagsNode, Map.class);
        }
        if (entityTags != null && entityTags.size() != 0) {
            entity.setTags(entityTags);
        }

        result.setEntity(entity);

        Map<String, String> seriesTags = null;
        JsonNode seriesTagsNode = node.get(8);
        if (seriesTagsNode != null) {
            seriesTags = mapper.convertValue(seriesTagsNode, Map.class);
        }

        if (seriesTags != null && seriesTags.size() != 0) {
            result.setSeriesTags(seriesTags);
        }

        Double relevanceScore = null;
        JsonNode relevanceScoreNode = node.get(9);
        if (relevanceScoreNode != null) {
            relevanceScore = relevanceScoreNode.doubleValue();
        }

        result.setRelevanceScore(relevanceScore);

        return result;
    }
}
