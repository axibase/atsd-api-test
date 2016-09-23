package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.axibase.tsd.api.model.sql.StringTable;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;


public class SqlTest extends SqlMethod {
    private static final String DEFAULT_ASSERT_OK_REQUEST_MESSAGE = "Response status is  not ok";
    private static final String DEFAULT_ASSERT_BAD_REQUEST_MESSAGE = "Response status is  not bad";


    public static void assertTableRowsExist(List<List<String>> expectedRows, StringTable table, String errorMessage) {
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

    public static void assertTableRowsExist(String[][] expectedRowsArray, StringTable table, String errorMessage) {
        assertTableRowsExist(Util.twoDArrayToList(expectedRowsArray), table, errorMessage);
    }


    public static void assertTableRowsExist(String[][] expectedRowsArray, StringTable table) {
        assertTableRowsExist(Util.twoDArrayToList(expectedRowsArray), table);
    }

    public static void assertTableRowsExist(List<List<String>> expectedRows, StringTable table) {
        assertTableRowsExist(expectedRows, table, "Table rows must be equals");
    }

    private static Boolean isEqualCells(String expectedValue, String actualValue, String dataType) {
        try {
            switch (dataType) {
                case "double":
                    Double actualDoubleValue = Double.parseDouble(actualValue);
                    Double expectedDoubleValue = Double.parseDouble(expectedValue);
                    return actualDoubleValue.equals(expectedDoubleValue);
                case "float":
                    Float actualFloatValue = Float.parseFloat(actualValue);
                    Float expectedFloatValue = Float.parseFloat(expectedValue);
                    return actualFloatValue.equals(expectedFloatValue);
                default:
                    return expectedValue.equals(actualValue);
            }
        } catch (NumberFormatException nfe) {
            return expectedValue.equals(actualValue);
        }
    }

    private static void failNotEquals(String message, Object expected, Object actual) {
        fail(format(message, expected, actual));
    }

    private static String format(String message, Object expected, Object actual) {
        return String.format("%s expected:<%s> but was:<%s>", message, expected, actual);
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

    public void assertOkRequest(Response response) {
        assertOkRequest(DEFAULT_ASSERT_OK_REQUEST_MESSAGE, response);
    }

    public void assertOkRequest(String assertMessage, Response response) {
        assertEquals(assertMessage, OK.getStatusCode(), response.getStatus());
        try {
            response.readEntity(StringTable.class);
        } catch (ProcessingException e) {
            fail("Failed to read table from respone!");
        }
    }

    public void assertBadRequest(Response response, String expectedMessage) {
        assertBadRequest(DEFAULT_ASSERT_BAD_REQUEST_MESSAGE, response, expectedMessage);
    }

    public void assertBadRequest(String assertMessage, Response response, String expectedMessage) {
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
