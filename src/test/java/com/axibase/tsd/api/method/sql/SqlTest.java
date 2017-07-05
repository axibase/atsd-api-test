package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.TestUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;


public abstract class SqlTest extends SqlMethod {
    private static final String DEFAULT_ASSERT_OK_REQUEST_MESSAGE = "Response status is  not ok";
    private static final String DEFAULT_ASSERT_BAD_REQUEST_MESSAGE = "Response status is  not bad";


    private static void assertTableRowsExist(String errorMessage, List<List<String>> expectedRows, StringTable table) {
        List<List<String>> actualRows = table.getRows();
        if (actualRows.size() != expectedRows.size()) {
            failNotEquals(errorMessage, expectedRows, actualRows);
        }
        for (int i = 0; i < actualRows.size(); i++) {
            List<String> actualRow = actualRows.get(i);
            List<String> expectedRow = expectedRows.get(i);
            int actualRowSize = actualRow.size();
            int expectedRowSize = expectedRow.size();
            if (actualRowSize != expectedRowSize) {
                failNotEquals(errorMessage, expectedRows, actualRows);
            }
            for (int j = 0; j < actualRow.size(); j++) {
                String dataType = table.getColumnMetaData(j).getDataType();
                String expectedValue = expectedRow.get(j);
                String actualValue = actualRow.get(j);
                if (!isEqualCells(expectedValue, actualValue, dataType)) {
                    failNotEquals(errorMessage, expectedRows, actualRows);
                }
            }

        }

    }

    public static void assertTableRowsExist(String errorMessage, String[][] expectedRowsArray, StringTable table) {
        assertTableRowsExist(errorMessage, TestUtil.twoDArrayToList(expectedRowsArray), table);
    }


    public static void assertTableRowsExist(String[][] expectedRowsArray, StringTable table) {
        assertTableRowsExist(TestUtil.twoDArrayToList(expectedRowsArray), table);
    }

    public static void assertTableRowsExist(List<List<String>> expectedRows, StringTable table) {
        assertTableRowsExist("Table rows must be equals", expectedRows, table);
    }

    private static Boolean isEqualCells(String expectedValue, String actualValue, String dataType) {
        if (expectedValue == null) {
            return Objects.equals(actualValue, "null");
        } else {
            try {
                switch (dataType) {
                    case "double": {
                        Double actualDoubleValue = Double.parseDouble(actualValue);
                        Double expectedDoubleValue = Double.parseDouble(expectedValue);
                        return actualDoubleValue.equals(expectedDoubleValue);
                    }
                    case "float": {
                        Float actualFloatValue = Float.parseFloat(actualValue);
                        Float expectedFloatValue = Float.parseFloat(expectedValue);
                        return actualFloatValue.equals(expectedFloatValue);
                    }
                    case "java_object": {
                        try {
                            Float actualFloatValue = Float.parseFloat(actualValue);
                            Float expectedFloatValue = Float.parseFloat(expectedValue);
                            return actualFloatValue.equals(expectedFloatValue);
                        } catch (NumberFormatException ex) {
                            return expectedValue.equals(actualValue);
                        }
                    }
                    default: {
                        return expectedValue.equals(actualValue);
                    }
                }
            } catch (NumberFormatException nfe) {
                return expectedValue.equals(actualValue);
            }
        }

    }

    private static void failNotEquals(String message, Object expected, Object actual) {
        fail(format(message, expected, actual));
    }

    private static String format(String message, Object expected, Object actual) {
        return String.format("%s expected:<%s> but was:<%s>", message, expected, actual);
    }

    public void assertSqlQueryRows(String message, List<List<String>> expectedRows, String sqlQuery) {
        StringTable resultTable = queryTable(sqlQuery);
        assertTableRowsExist(String.format("%s%nWrong result of the following SQL query: %n\t%s", message, sqlQuery), expectedRows,
                resultTable
        );
    }

    public void assertSqlQueryRows(String message, String[][] expectedRows, String sqlQuery) {
        assertSqlQueryRows(message, TestUtil.twoDArrayToList(expectedRows), sqlQuery);
    }

    public void assertSqlQueryRows(List<List<String>> expectedRows, String sqlQuery) {
        assertSqlQueryRows("", expectedRows, sqlQuery);
    }

    public void assertSqlQueryRows(String[][] expectedRows, String sqlQuery) {
        assertSqlQueryRows(TestUtil.twoDArrayToList(expectedRows), sqlQuery);
    }

    public void assertTableContainsColumnsValues(List<List<String>> values, StringTable table, String... columnNames) {
        assertEquals(String.format("Values of columns with names: %s are not equal to expected", columnNames), values, table.filterRows(columnNames));
    }

    public void assertTableContainsColumnValues(List<String> values, StringTable table, String columnName) {
        assertEquals(String.format("Values of column with name: %s are not equal to expected", columnName), values, table.columnValues(columnName));
    }

    public void assertTableColumnsNames(List<String> expectedColumnsNames, StringTable table) {
        assertTableColumnsNames(expectedColumnsNames, table, false);
    }

    public void assertTableColumnsLabels(List<String> expectedColumnsLabels, StringTable table) {
        assertTableColumnsLabels(expectedColumnsLabels, table, false);
    }

    public void assertTableColumnsLabels(List<String> expectedColumnsLabels, StringTable table, Boolean order) {
        List<String> columnsLabels = extractColumnLabels(table.getColumnsMetaData());

        if (order) {
            assertEquals("Table columns labels are not equal to expected", expectedColumnsLabels, columnsLabels);
        } else {
            assertEquals("Table columns labels contain different elements", new HashSet<>(expectedColumnsLabels), new HashSet<String>(columnsLabels));
        }
    }

    public void assertTableColumnsNames(List<String> expectedColumnsNames, StringTable table, Boolean order) {
        List<String> columnsNames = extractColumnNames(table.getColumnsMetaData());

        if (order) {
            assertEquals("Table columns names are not equal to expected", expectedColumnsNames, columnsNames);
        } else {
            assertEquals("Table columns names contain different elements", new HashSet<>(expectedColumnsNames), new HashSet<String>(columnsNames));

        }
    }

    public void assertOkRequest(Response response) {
        assertOkRequest(DEFAULT_ASSERT_OK_REQUEST_MESSAGE, response);
    }

    public void assertOkRequest(String assertMessage, Response response) {
        assertEquals(assertMessage, OK.getStatusCode(), response.getStatus());
        try {
            response.readEntity(StringTable.class);
        } catch (ProcessingException e) {
            fail("Failed to read table from response!");
        }
    }

    public void assertBadRequest(String expectedMessage, Response response) {
        assertBadRequest(DEFAULT_ASSERT_BAD_REQUEST_MESSAGE, expectedMessage, response);
    }

    public void assertBadRequest(String assertMessage, String expectedMessage, Response response) {
        assertEquals(assertMessage, BAD_REQUEST.getStatusCode(), response.getStatus());
        String responseMessage = extractSqlErrorMessage(response);
        assertEquals("Error message is different form expected", expectedMessage, responseMessage);
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

    /**
     * Retrieve column labels form table column metadata set
     *
     * @param columnMetaData set of column metadata values
     * @return column labels set
     */
    private List<String> extractColumnLabels(Set<ColumnMetaData> columnMetaData) {
        List<String> columnNames = new ArrayList<>();
        for (ColumnMetaData data : columnMetaData) {
            String label = data.getTitles();
            if (label == null) {
                label = data.getName();
            }

            columnNames.add(label);
        }
        return columnNames;
    }

    private String extractSqlErrorMessage(Response response) {
        String jsonText = response.readEntity(String.class);
        try {
            JSONObject json = new JSONObject(jsonText);
            return json.getJSONArray("errors")
                    .getJSONObject(0)
                    .getString("message");
        } catch (JSONException e) {
            return null;
        }

    }
}
