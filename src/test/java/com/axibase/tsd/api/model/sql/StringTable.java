package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 *         <p>
 *         Class for storing SQL result table in {@link String}
 *         objects.
 *         It is using custom deserializer
 */
@JsonDeserialize(using = StringTableDeserializer.class)
public class StringTable {

    private List<String> columns;
    private List<List<String>> rows;


    public StringTable() {
        columns = new ArrayList<>();
        rows = new ArrayList<>();
    }

    public void addColumn(String columnName) {
        columns.add(columnName);
    }

    public void addRow(ArrayList<String> row) {
        rows.add(row);
    }

    public String getColumnName(int index) {
        return columns.get(index);
    }

    public List<String> getRow(int index) {
        return rows.get(index);
    }

    public String getValueAt(int i, int j) {
        return rows.get(i).get(j);
    }

    public List<List<String>> getRows() {
        return rows;
    }


    public List<String> getColumns() {
        return columns;
    }
}
