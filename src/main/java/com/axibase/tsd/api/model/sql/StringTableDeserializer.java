package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author Igor Shmagrinskiy
 *         Deserialize class for Object mapper that used in {@link javax.ws.rs.core.Response} class for deserialization of JSON objects
 */
class StringTableDeserializer extends JsonDeserializer<StringTable> {
    @Override
    public StringTable deserialize(JsonParser jsonParser,
                                   DeserializationContext deserializationContext) throws IOException {
        String jsonText = jsonParser.readValueAsTree().toString();
        StringTable result;
        try {
            result = SqlTableParser.parseStringTable(new JSONObject(jsonText));
        } catch (JSONException je) {
            throw new JsonParseException(jsonParser, je.getMessage());
        }
        return result;
    }
}
