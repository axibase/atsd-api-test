package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.*;

/**
 * @author Igor Shmagrinskiy
 *         <p>
 *         Class for storing SQL result table in {@link String}
 *         objects. It is using custom deserializer
 *         </p>
 */
@JsonDeserialize(using = StringTableDeserializer.class)
public class StringTable {
    private ColumnMetaData[] columnsMeta;
    private String[][] tableData;
    private int rowsCount = 0, columnsCount = 0;


    public StringTable(ColumnMetaData[] columnsMeta, String[][] tableData) {
        columnsCount = columnsMeta.length;

        if (columnsCount == 0) {
            throw new IllegalArgumentException("Empty columns metadata");
        }

        if (tableData.length == 0) {
            throw new IllegalArgumentException("Empty table data");
        }

        rowsCount = tableData.length;

        for (int i = 0; i < rowsCount; i++) {
            if (tableData[i].length != columnsCount) {
                throw new IllegalArgumentException("Non-square table data");
            }
        }

        this.columnsMeta = columnsMeta;
        this.tableData = tableData;
    }

    public ColumnMetaData getColumnMetaData(int index) {
        if (index < 0 || index >= rowsCount) {
            throw new IllegalStateException("Table doesn't contain column with index " + index);
        }
        return columnsMeta[index];
    }

    public String getValueAt(int i, int j) {
        return tableData[j][i];
    }

    public List<List<String>> getRows() {
        return getRows(null);
    }

    public List<List<String>> getRows(boolean[] columnFilter) {
        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < rowsCount; i++) {
            rows.set(i, new ArrayList<>());
            for (int j = 0; j < columnsCount; j++) {
                if (columnFilter == null || columnFilter[j]) {
                    rows.get(i).add(tableData[i][j]);
                }
            }
        }
        return rows;
    }

    public Set<ColumnMetaData> getColumnsMetaData() {
        return new HashSet<>(Arrays.asList(columnsMeta));
    }


    /**
     * Filter row values by column names. Leaves those values, that indexes corresponded
     * with columnNames contained in the set of requested column names
     *
     * @param requestedColumnNames - set of requested column names
     * @return filtered rows
     */
    public List<List<String>> filterRows(Set<String> requestedColumnNames) {
        boolean[] columnFilter = new boolean[columnsCount];
        for (int i = 0; i < columnsCount; i++) {
            columnFilter[i] = requestedColumnNames.contains(columnsMeta[i].getTitles());
        }
        return getRows(columnFilter);
    }

    public List<String> columnValues(String requestedColumnName) {
        List<List<String>> filteredRows = filterRows(new HashSet<>(Collections.singletonList(requestedColumnName)));
        List<String> resultColumn = new ArrayList<>();
        for (List<String> row : filteredRows) {
            if (row.size() < 1) {
                throw new IllegalStateException("Table doesn't contain requested column!");
            }
            resultColumn.add(row.get(0));
        }
        return resultColumn;
    }


    /**
     * Filter row values by column names. Leaves those values, that indexes corresponded
     * with columnNames contained in the set of requested column names
     *
     * @param requestedColumnNames - set of requested column names represented as args
     * @return filtered rows
     */
    public List<List<String>> filterRows(String... requestedColumnNames) {
        Set<String> filter = new HashSet<>();
        Collections.addAll(filter, requestedColumnNames);
        return filterRows(filter);
    }
}
