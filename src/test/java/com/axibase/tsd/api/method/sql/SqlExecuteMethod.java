package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * @author Igor Shmagrinskiy
 *         <p>
 *         Class that make sql queries to ATSD instanse
 *         and retrive result in specifed format.
 *         Usage:
 *         * <pre>
 *         {@code
 *              SqlExecuteMethod
 *                          .executeQuery("SELECT 1")
 *                          .readEntity(String.class);
 *         }
 *         </pre>
 */
public class SqlExecuteMethod extends BaseMethod {
    private static final String METHOD_SQL_API = "/api/sql";
    private static WebTarget httpSqlApiResource = httpRootResource.path(METHOD_SQL_API);
    private static final Logger logger = LoggerFactory.getLogger(SqlExecuteMethod.class);


    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery     SQL query in a String format
     * @param outputFormat some field from {@link OutputFormat}
     * @return instance of Response
     */
    public static Response executeQuery(String sqlQuery, OutputFormat outputFormat) {
        return httpSqlApiResource
                .queryParam("q", sqlQuery)
                .queryParam("outputFormat", outputFormat.toString())
                .request()
                .get();
    }

    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery SQL executeQuery in a String format
     * @return instance of Response
     */
    public static Response executeQuery(String sqlQuery) {
        return executeQuery(sqlQuery, OutputFormat.JSON);
    }
}
