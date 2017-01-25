package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;


public class ValueDeserializer extends JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String stringValue = jsonParser.readValueAs(String.class);
        return ("NaN".equals(stringValue)) ? null : new BigDecimal(stringValue);
    }
}
