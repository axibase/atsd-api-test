package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

public class SqlMethod extends BaseMethod {
    private static final String METHOD_SQL_API = "/api/sql";
    protected static final WebTarget httpSqlApiResource = getHttpRootResource()
            .property(ClientProperties.CONNECT_TIMEOUT, 1000)
            .property(ClientProperties.READ_TIMEOUT, 1000)
            .path(METHOD_SQL_API);
    private static final Logger logger = LoggerFactory.getLogger(SqlMethod.class);

    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery     SQL query in a String format
     * @param outputFormat some field from {@link OutputFormat}
     * @param limit        limit of returned Rows
     * @return instance of Response
     */
    public static Response executeQuery(String sqlQuery, OutputFormat outputFormat, Integer limit) {
        logger.debug("SQL query : {}", sqlQuery);
        Form form = new Form();
        if (sqlQuery == null) {
            throw new IllegalStateException("Query must be defined");
        }
        form.param("q", sqlQuery);
        if (outputFormat != null) {
            form.param("outputFormat", outputFormat.toString());
        }
        if (limit != null) {
            form.param("limit", Integer.toString(limit));
        }
        Response response = httpSqlApiResource
                .request()
                .post(Entity.form(form));
        response.bufferEntity();
        return response;
    }


    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery SQL executeQuery in a String format
     * @param limit    limit of returned rows
     * @return instance of Response
     */
    public static Response executeQuery(String sqlQuery, Integer limit) {
        return executeQuery(sqlQuery, OutputFormat.JSON, limit);
    }

    /**
     * Execute SQL executeQuery and retrieve result in specified format
     *
     * @param sqlQuery SQL executeQuery in a String format
     * @return instance of Response
     */
    public static Response executeQuery(String sqlQuery) {
        return executeQuery(sqlQuery, OutputFormat.JSON, null);
    }
}
