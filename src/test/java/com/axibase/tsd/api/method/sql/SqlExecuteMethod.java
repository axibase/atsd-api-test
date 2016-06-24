package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.sql.StringTable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * @author Igor Shmagrinskiy
 *
 * Class that make sql queries to ATSD instanse
 *  and retrive result in specifed format.
 * Usage:
 * * <pre>
 * {@code
 *      SqlExecuteMethod sqlMethod = new SqlExecuteMethod();
 *      String s = sqlMetod
 *                  .executeQuery("SELECT 1")
 *                  .result(String.class);
 * }
 * </pre>
 */
public class SqlExecuteMethod extends BaseMethod {
    private static final String METHOD_SQL_API = "/api/sql";
    private static WebTarget httpSqlApiResource = httpRootResource.path(METHOD_SQL_API);
    private static final Logger logger = LoggerFactory.getLogger(SqlExecuteMethod.class);
    private Response queryResponse;


    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery     SQL executeQuery in a String format
     * @param outputFormat some field from {@link OutputFormat}
     * @return HTTP Response
     */
    public SqlExecuteMethod executeQuery(String sqlQuery, OutputFormat outputFormat) {
        queryResponse = httpSqlApiResource
                .queryParam("q", sqlQuery)
                .queryParam("outputFormat", outputFormat.toString())
                .request()
                .get();
        return this;
    }

    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery     SQL executeQuery in a String format
     * @return SqlExecuteMethod
     */
    public SqlExecuteMethod executeQuery(String sqlQuery) {
        return executeQuery(sqlQuery, OutputFormat.JSON);
    }
    /**
     * Map response to specified classs
     *
     * @param type Class type that we want get as result
     * @return   Object of specified class
     */
    public <T> T result(Class<T> type) {
        try {
            if (type == String.class) {
                return type.cast(queryResponse.readEntity(String.class));
            }
            if (type == JSONObject.class) {
                return type.cast(new JSONObject(result(String.class)));
            }
            if (type == StringTable.class) {
                return type.cast(StringTable.parseTable(result(JSONObject.class)));
            }
        } catch (JSONException je) {
            logger.error("Failed mapping to {}. Reason:", je.getMessage(), je.getMessage());
            throw new IllegalStateException("Failed mapping to " + type + "Reason" + je.getMessage());
        }
        throw new IllegalStateException("No mapping for class {}" + type);
    }
}
