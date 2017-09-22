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
    private TableMetaData tableMeta;
    private String[][] tableData;
    private int rowsCount = 0, columnsCount = 0;


    public StringTable(TableMetaData tableMeta, String[][] tableData) {
        columnsCount = tableMeta.size();
        rowsCount = tableData.length;

        for (int i = 0; i < rowsCount; i++) {
            if (tableData[i].length != columnsCount) {
                throw new IllegalArgumentException("Non-square table data");
            }
        }

        this.tableMeta = tableMeta;
        this.tableData = tableData;
    }

    public TableMetaData getTableMetaData() {
        return tableMeta;
    }

    public ColumnMetaData getColumnMetaData(int index) {
        return tableMeta.getColumnMeta(index);
    }

    public String getValueAt(int i, int j) {
        return tableData[j][i];
    }

    public List<List<String>> getRows() {
        return getRows(null);
    }

    private List<List<String>> getRows(boolean[] columnFilter) {
        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < rowsCount; i++) {
            rows.add(new ArrayList<>(columnsCount));
            for (int j = 0; j < columnsCount; j++) {
                if (columnFilter == null || columnFilter[j]) {
                    rows.get(i).add(tableData[i][j]);
                }
            }
        }
        return rows;
    }

    public ColumnMetaData[] getColumnsMetaData() {
        return tableMeta.getColumnsMeta();
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
            columnFilter[i] = requestedColumnNames.contains(tableMeta.getColumnMeta(i).getTitles());
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
