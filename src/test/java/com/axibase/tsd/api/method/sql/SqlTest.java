package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.axibase.tsd.api.model.sql.StringTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlTest extends SqlMethod {
    public static void assertTableRows(List<List<String>> row1, List<List<String>> row2) {
        assertEquals("Table rows  must  be identical", row1, row2);
    }

    public static void assertTableRows(List<List<String>> row1, StringTable table) {
        List<List<String>> row2 = table.getRows();
        assertEquals("Table rows  must  be identical", row1, row2);
    }

    public void assertTableContainsColumnsValues(List<List<String>> values, StringTable table, String... columnNames) {
        assertEquals(String.format("Values of columns with names: %s are not equal to expected", columnNames), table.filterRows(columnNames), values);
    }

    public void assertTableContainsColumnValues(List<String> values, StringTable table, String columnName) {
        assertEquals(String.format("Values of column with name: %s are not equal to expected", columnName), table.columnValues(columnName), values);
    }

    public void assertTableColumnsNames(List<String> expectedColumnsNames, StringTable table) {
        Set<String> resultColumnNames = extractColumnNames(table.getColumnsMetaData());
        assertEquals("Table columns names are not equal to expected", new HashSet<>(expectedColumnsNames), resultColumnNames);
    }

    /**
     * Retrieve column names form table column metadata set
     *
     * @param columnMetaData set of column metadata values
     * @return column names set
     */
    private Set<String> extractColumnNames(Set<ColumnMetaData> columnMetaData) {
        Set<String> columnNames = new HashSet<>();
        for (ColumnMetaData data : columnMetaData) {
            columnNames.add(data.getName());
        }
        return columnNames;
    }
}
