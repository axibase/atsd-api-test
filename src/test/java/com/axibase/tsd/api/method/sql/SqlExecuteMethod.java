package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlExecuteMethod extends BaseMethod {
    private static final String METHOD_SQL_API = "/api/sql";
    private static WebTarget httpSqlApiResource = httpRootResource.path(METHOD_SQL_API);
    private static final Logger logger = LoggerFactory.getLogger(SqlExecuteMethod.class);

    /**
     * Execute SQL query and retrieve result in specified format
     *
     * @param sqlQuery     SQL query in a String format
     * @param outputFormat some field from {@link OutputFormat}
     * @return HTTP Response
     */
    public static Response query(String sqlQuery, OutputFormat outputFormat) {
        return httpSqlApiResource
                .queryParam("q", sqlQuery)
                .queryParam("outputFormat", outputFormat.toString())
                .request()
                .get();
    }

    /**
     * Execute SQL query and retrieve result in specified format as {@link String}
     *
     * @param sqlQuery     SQL query in a String format
     * @param outputFormat some field from {@link OutputFormat}
     * @return Response body as a {@link String}
     */
    public static String queryAsString(String sqlQuery, OutputFormat outputFormat) {
        return query(sqlQuery, outputFormat).readEntity(String.class);
    }

    /**
     * Execute SQL query and retrieve result in JSON format as {@link JSONObject}
     *
     * @param sqlQuery SQL query in a String format
     * @return response body as {@link JSONObject}
     * @throws JSONException on incorrect {@link Response} body
     */
    public static JSONObject queryAsJson(String sqlQuery) throws JSONException {
        return new JSONObject(queryAsString(sqlQuery, OutputFormat.JSON));
    }


    /**
     * Execute SQL query and retrieve result in JSON format as {@link JSONObject}
     *
     * @param sqlQuery SQL query in a String format
     * @return response body in a table view model
     * @throws JSONException on incorrect {@link Response} body
     * @throws IOException   when response json contains errors field
     */
    public static TableModel queryAsTable(String sqlQuery) throws IOException, JSONException {
        DefaultTableModel tableModel;
        JSONObject tableJson;
        try {
            tableJson = queryAsJson(sqlQuery);
        } catch (JSONException je) {
            logger.debug("Failed to parse response body");
            throw je;
        }

        tableModel = new DefaultTableModel();
        if (tableJson.has("errors")) {
            JSONArray errors = tableJson.getJSONArray("errors");
            String errorMessage = errors.getString(0);
            logger.debug("Failed to execute sql query : {} with. Reason: {}", sqlQuery, errorMessage);
            throw new IOException(errorMessage);
        }

        JSONArray columns = tableJson
                .getJSONObject("metadata")
                .getJSONObject("tableSchema")
                .getJSONArray("columns");
        JSONArray data = tableJson.getJSONArray("data");
        Integer columnCount = columns.length();
        for (int i = 0; i < columnCount; i++) {
            tableModel.addColumn(columns
                    .getJSONObject(i)
                    .getString("name"));
        }

        Object[] row = new Object[columnCount];
        Object rowJSON;
        JSONObject rowJsonObject;
        JSONArray rowJsonArray;
        for (int i = 0; i < data.length(); i++) {
            rowJSON = data.get(i);
            if (rowJSON instanceof JSONObject) {
                rowJsonObject = (JSONObject) rowJSON;
                for (int j = 0; j < columnCount; j++) {
                    row[j] = rowJsonObject.getString(tableModel.getColumnName(j));
                }
            } else if (rowJSON instanceof JSONArray) {
                rowJsonArray = data.getJSONArray(i);
                for (int j = 0; j < columnCount; j++) {
                    row[j] = rowJsonArray.getString(j);
                }
            } else {
                throw new IllegalStateException("It's not JSON structure " + rowJSON);
            }
            tableModel.addRow(row);
        }
        return tableModel;
    }


}
