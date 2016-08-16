package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.axibase.tsd.api.model.sql.StringTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

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
        assertEquals(String.format("Values of column with name: %s are not equal to expected", columnName), values, table.columnValues(columnName));
    }

    public void assertTableColumnsNames(List<String> expectedColumnsNames, StringTable table) {
        assertTableColumnsNames(expectedColumnsNames, table, false);
    }


    public void assertTableColumnsNames(List<String> expectedColumnsNames, StringTable table, Boolean order) {
        List<String> columnsNames = extractColumnNames(table.getColumnsMetaData());

        if (order) {
            assertEquals("Table columns names are not equal to expected", expectedColumnsNames, columnsNames);
        } else {
            assertEquals("Table columns names contain different elements", new HashSet<>(expectedColumnsNames), new HashSet<String>(columnsNames));

        }
    }

    /**
     * Retrieve column names form table column metadata set
     *
     * @param columnMetaData set of column metadata values
     * @return column names set
     */
    private List<String> extractColumnNames(Set<ColumnMetaData> columnMetaData) {
        List<String> columnNames = new ArrayList<>();
        for (ColumnMetaData data : columnMetaData) {
            columnNames.add(data.getName());
        }
        return columnNames;
    }
}
