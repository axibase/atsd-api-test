package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

final class SqlTableParser {
    private static final Logger logger = LoggerFactory.getLogger(SqlTableParser.class);

    private static final String META_DATA_FIELD = "metadata";
    private static final String TABLE_SCHEMA_FIELD = "tableSchema";
    private static final String COLUMNS_FIELD = "columns";
    private static final String DATA_FIELD = "data";

    static TableMetaData parseMeta(JSONObject meta) throws JSONException, IOException {
        ObjectMapper objMapper = new ObjectMapper();
        JSONArray columns = meta.getJSONObject(TABLE_SCHEMA_FIELD).getJSONArray(COLUMNS_FIELD);
        logger.debug("Parsing columns {}", columns);
        ColumnMetaData[] columnMetaData = objMapper.readValue(columns.toString(), ColumnMetaData[].class);
        return new TableMetaData(columnMetaData);
    }

    static StringTable parseStringTable(JSONObject tableJson) throws JSONException, IOException {
        JSONObject meta = tableJson.getJSONObject(META_DATA_FIELD);
        JSONArray data = tableJson.getJSONArray(DATA_FIELD);

        TableMetaData tableMeta = parseMeta(meta);

        int columnCount = tableMeta.size();
        int rowCount = data.length();

        String[][] tableValues = new String[rowCount][columnCount];

        JSONArray jsonRow;
        for (int i = 0; i < data.length(); i++) {
            jsonRow = data.getJSONArray(i);
            for (int j = 0; j < columnCount; j++) {
                tableValues[i][j] = jsonRow.getString(j);
            }
        }
        return new StringTable(tableMeta, tableValues);
    }

    private SqlTableParser() {}
}
