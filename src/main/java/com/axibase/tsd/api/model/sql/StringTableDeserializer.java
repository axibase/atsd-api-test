package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Igor Shmagrinskiy
 *         Deserialize class for Object mapper that used in {@link javax.ws.rs.core.Response} class for deserialization of JSON objects
 */
class StringTableDeserializer extends JsonDeserializer<StringTable> {
    @Override
    public StringTable deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String jsonText = jsonParser
                .readValueAsTree()
                .toString();
        StringTable result;
        try {
            result = parseStringTable(new JSONObject(jsonText));
        } catch (JSONException je) {
            throw new JsonParseException(jsonParser, je.getMessage());
        }
        return result;
    }


    private StringTable parseStringTable(JSONObject tableJson) throws JSONException {
        StringTable tableModel = new StringTable();
        JSONArray columns = tableJson
                .getJSONObject("metadata")
                .getJSONObject("tableSchema")
                .getJSONArray("columns");
        JSONArray data = tableJson.getJSONArray("data");
        Integer columnCount = columns.length();
        for (int i = 0; i < columnCount; i++) {
            tableModel.addColumnMetaData(parseColumn(columns.getJSONObject(i)));
        }
        String[] row = new String[columnCount];
        Object rowJSON;
        JSONObject rowJsonObject;
        JSONArray rowJsonArray;
        for (int i = 0; i < data.length(); i++) {
            rowJSON = data.get(i);

            if (rowJSON instanceof JSONObject) {
                rowJsonObject = (JSONObject) rowJSON;
                for (int j = 0; j < columnCount; j++) {
                    row[j] = rowJsonObject.getString(tableModel.getColumnMetaData(j).getName());
                }
            } else if (rowJSON instanceof JSONArray) {
                rowJsonArray = data.getJSONArray(i);
                for (int j = 0; j < columnCount; j++) {
                    row[j] = rowJsonArray.getString(j);
                }
            } else {
                throw new IllegalStateException("It's not JSON structure " + rowJSON);
            }
            tableModel.addRow(new ArrayList<>(Arrays.asList(row)));
        }
        return tableModel;
    }

    private ColumnMetaData parseColumn(JSONObject jsonColumn) throws JSONException {
        ColumnMetaData columnMetaData = new ColumnMetaData(jsonColumn.getString("name"), jsonColumn.getInt("columnIndex"));
        if (jsonColumn.has("datatype")) {
            columnMetaData.setDataType(jsonColumn.getString("datatype"));
        }
        if (jsonColumn.has("table")) {
            columnMetaData.setTable(jsonColumn.getString("table"));
        }
        if (jsonColumn.has("propertyUrl")) {
            columnMetaData.setPropertyUrl(jsonColumn.getString("propertyUrl"));
        }
        if (jsonColumn.has("titles")) {
            columnMetaData.setTitles(jsonColumn.getString("titles"));
        }
        return columnMetaData;
    }
}
