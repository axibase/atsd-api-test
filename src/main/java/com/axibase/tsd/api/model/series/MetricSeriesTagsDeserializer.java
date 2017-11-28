package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MetricSeriesTagsDeserializer extends JsonDeserializer<MetricSeriesTags> {
    @Override
    public MetricSeriesTags deserialize(JsonParser jsonParser, DeserializationContext ctx)
            throws IOException {
        Map<String, List<String>> tags = jsonParser.readValueAs(new TypeReference<Map<String, List<String>>>() {});
        return new MetricSeriesTags(tags);
    }
}
