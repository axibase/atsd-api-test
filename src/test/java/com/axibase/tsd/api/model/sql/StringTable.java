package com.axibase.tsd.api.model.sql;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Igor Shmagrinskiy
 */
public class StringTable implements Table<String, ArrayList<String>> {

    private ArrayList<String> columns;
    private ArrayList<ArrayList<String>> rows;

    public static StringTable parseTable(JSONObject tableJson) throws JSONException {
        StringTable tableModel = new StringTable();
        JSONArray columns = tableJson
                .getJSONObject("metadata")
                .getJSONObject("tableSchema")
                .getJSONArray("columns");
        JSONArray data = tableJson.getJSONArray("data");
        Integer columnCount = columns.length();
        for (int i = 0; i < columnCount; i++) {
            tableModel.addColumn(columns
                    .getJSONObject(i)
                    .getString("name"));
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
                    row[j] = rowJsonObject.getString(tableModel.getColumnName(j));
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


    private StringTable() {
        columns = new ArrayList<>();
        rows = new ArrayList<>();
    }

    @Override
    public void addColumn(String columnName) {
        columns.add(columnName);
    }

    @Override
    public void addRow(ArrayList<String> row) {
        rows.add(row);
    }

    @Override
    public String getColumnName(int index) {
        return columns.get(index);
    }

    @Override
    public ArrayList<String> getRow(int index) {
        return rows.get(index);
    }

    public String getValueAt(int i, int j) {
        return rows.get(i).get(j);
    }

    public ArrayList<ArrayList<String>> getRows() {
        return rows;
    }


    public ArrayList<String> getColumns() {
        return columns;
    }
}
