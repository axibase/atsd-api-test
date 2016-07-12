package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.model.sql.StringTable;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlTest extends SqlMethod {
    public static void assertTableRows(List<List<String>> row1, List<List<String>> row2) {
        assertEquals("Table rows  must  be identical", row1, row2);
    }

    public void assertTableContainsColumnsValues(List<List<String>> values, StringTable table, String... columnNames) {
        assertEquals(String.format("Values of columns with names: %s are not equal to expected", columnNames), table.filterRows(columnNames), values);
    }

    public void assertTableContainsColumnValues(List<String> values, StringTable table, String columnName) {
        assertEquals(String.format("Values of column with name: %s are not equal to expected", columnName), table.columnValues(columnName), values);
    }
}
