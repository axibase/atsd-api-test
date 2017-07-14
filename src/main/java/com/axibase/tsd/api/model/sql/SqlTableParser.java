package com.axibase.tsd.api.model.sql;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class SqlTableParser {
    private static final String META_DATA_FIELD = "metadata";
    private static final String TABLE_SCHEMA_FIELD = "tableSchema";
    private static final String COLUMNS_FIELD = "columns";
    private static final String DATA_FIELD = "data";
    private static final String COLUMN_NAME_FIELD = "name";
    private static final String COLUMN_INDEX_FIELD = "columnIndex";
    private static final String[] ADDITIONAL_COLUMN_FIELDS = {
            "datatype", "table", "propertyUrl", "titles"
    };

    public static TableMetaData parseMeta(JSONObject meta) throws JSONException {
        JSONArray columns = meta.getJSONObject(TABLE_SCHEMA_FIELD).getJSONArray(COLUMNS_FIELD);
        int columnCount = columns.length();
        ColumnMetaData[] columnMetaData = new ColumnMetaData[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnMetaData[i] = parseColumn(columns.getJSONObject(i));
        }
        return new TableMetaData(columnMetaData);
    }

    static StringTable parseStringTable(JSONObject tableJson) throws JSONException {
        JSONObject meta = tableJson.getJSONObject(META_DATA_FIELD);
        JSONArray data = tableJson.getJSONArray(DATA_FIELD);

        TableMetaData tableMeta = parseMeta(meta);

        int columnCount = tableMeta.size();
        int rowCount = data.length();

        String[][] tableValues = new String[rowCount][columnCount];

        Object rowJSON;
        JSONObject rowJsonObject;
        JSONArray rowJsonArray;
        for (int i = 0; i < data.length(); i++) {
            rowJSON = data.get(i);

            if (rowJSON instanceof JSONObject) {
                rowJsonObject = (JSONObject) rowJSON;
                for (int j = 0; j < columnCount; j++) {
                    tableValues[i][j] =
                            rowJsonObject.getString(tableMeta.getColumnMeta(j).getName());
                }
            } else if (rowJSON instanceof JSONArray) {
                rowJsonArray = data.getJSONArray(i);
                for (int j = 0; j < columnCount; j++) {
                    tableValues[i][j] = rowJsonArray.getString(j);
                }
            } else {
                throw new IllegalStateException("It's not JSON structure " + rowJSON);
            }
        }
        return new StringTable(tableMeta, tableValues);
    }

    private static ColumnMetaData parseColumn(JSONObject jsonColumn) throws JSONException {
        ColumnMetaData columnMetaData = new ColumnMetaData(
                jsonColumn.getString(COLUMN_NAME_FIELD),
                jsonColumn.getInt(COLUMN_INDEX_FIELD)
        );
        for (String metaField : ADDITIONAL_COLUMN_FIELDS) {
            if (jsonColumn.has(metaField)) {
                columnMetaData.setDataType(jsonColumn.getString(metaField));
            }
        }
        return columnMetaData;
    }


    private SqlTableParser() {}
}
