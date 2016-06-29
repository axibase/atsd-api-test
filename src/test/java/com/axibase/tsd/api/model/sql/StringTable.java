package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Igor Shmagrinskiy
 *         <p>
 *         Class for storing SQL result table in {@link String}
 *         objects.
 *         It is using custom deserializer
 */
@JsonDeserialize(using = StringTableDeserializer.class)
public class StringTable {

    private final List<String> columnNames;
    private final List<List<String>> rows;
    private final List<List<String>> columns;


    public StringTable() {
        columnNames = new ArrayList<>();
        rows = new ArrayList<>();
        columns = new ArrayList<>();
    }


    public void addColumnName(String columnName) {
        columnNames.add(columnName);
    }

    public void addRow(ArrayList<String> row) {
        rows.add(row);
        while (columns.size() < row.size()) {
            columns.add(new ArrayList<String>());
        }
        int index = 0;
        for (String cell : row) {
            columns
                    .get(index)
                    .add(cell);
            index++;
        }
    }

    public String getColumnName(int index) {
        return columnNames.get(index);
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

    public List<List<String>> getColumns() {
        return columns;
    }


    /**
     * Filter row values by column names. Leaves those values, that indexes corresponded
     * with columnNames contained in the set of requested column names
     *
     * @param requestedColumnNames - set of requested column names
     * @return filtered rows
     */
    public List<List<String>> filterRows(Set<String> requestedColumnNames) {
        List<Integer> indexesOfRequestedColumns = new ArrayList<>();
        List<List<String>> filteredRows = new ArrayList<>(rows.size());
        int index = 0;
        for (String columnName : columnNames) {
            if (requestedColumnNames.contains(columnName)) {
                indexesOfRequestedColumns.add(index);
            }
            index++;
        }
        for (List<String> row : rows) {
            List<String> filteredRow = new ArrayList<>();
            index = 0;
            for (String cell: row) {
                if (indexesOfRequestedColumns.contains(index)) {
                    filteredRow.add(cell);
                }
            }
            filteredRows.add(filteredRow);
        }
        return filteredRows;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }
}
