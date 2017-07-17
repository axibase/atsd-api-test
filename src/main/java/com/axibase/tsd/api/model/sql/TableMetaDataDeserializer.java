package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class TableMetaDataDeserializer extends JsonDeserializer<TableMetaData> {
    @Override
    public TableMetaData deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        String jsonText = jsonParser.readValueAsTree().toString();
        TableMetaData result;
        try {
            result = SqlTableParser.parseMeta(new JSONObject(jsonText));
        } catch (JSONException je) {
            throw new JsonParseException(jsonParser, je.getMessage());
        }
        return result;
    }
}
