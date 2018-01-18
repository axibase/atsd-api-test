package com.axibase.tsd.api.model.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateDeserializer extends JsonDeserializer<ZonedDateTime> {
    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String stringValue = jsonParser.readValueAs(String.class);
        if (stringValue == null) {
            return null;
        }
        return ZonedDateTime.parse(stringValue, DateTimeFormatter.ISO_DATE_TIME);
    }
}
