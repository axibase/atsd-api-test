package com.axibase.tsd.api.method.sql;

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
