package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.axibase.tsd.api.model.sql.StringTable;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.*;

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
        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }

        return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
    }

    public static TestNameGenerator testNames(Class clazz) {
        return new TestNameGenerator(clazz);
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

    public static class TestNameGenerator {
        private static final String API_METHODS_PACKAGE_NAME = "com.axibase.tsd.api.method";
        private String baseName;
        private Map<String, Integer> dictionary;

        public TestNameGenerator(Class clazz) {
            this.baseName = extractBaseName(clazz);
            this.dictionary = new HashMap<>();
            for (Keys key : Keys.values()) {
                dictionary.put(key.toString(), 0);
            }
        }

        public String getEntityName() {
            return getTestName(Keys.ENTITY);
        }

        public String getMetricName() {
            return getTestName(Keys.METRIC);
        }

        private String getTestName(Keys key) {
            Integer entityTestNumber = dictionary.get(key.toString());
            String testName = String.format("%s%s-%d", baseName, key.toString(), entityTestNumber);
            dictionary.put(key.toString(), entityTestNumber + 1);
            return testName;
        }

        private String extractBaseName(Class clazz) {
            String canonicalClassName = clazz.getCanonicalName();
            String className = clazz.getSimpleName();
            if (canonicalClassName.contains(API_METHODS_PACKAGE_NAME)) {
                String result = canonicalClassName.replace(API_METHODS_PACKAGE_NAME + '.', "").replace(className, "").replace('.', '-');
                return result + className.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase() + "-";
            } else {
                throw new IllegalStateException("Failed to generate test name for non-method package");
            }

        }

        private enum Keys {
            ENTITY("entity"), METRIC("metric");

            private String textValue;

            Keys(String textValue) {
                this.textValue = textValue;
            }


            @Override
            public String toString() {
                return this.textValue;
            }
        }
    }
}
