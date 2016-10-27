package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.sql.AtsdExceptionDescription;
import com.axibase.tsd.api.model.sql.Error;
import com.axibase.tsd.api.model.sql.StringTable;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;

public class SqlMethod extends BaseMethod {
    private static final String METHOD_SQL_API = "/api/sql";
    protected static final WebTarget httpSqlApiResource = httpRootResource
            .property(ClientProperties.CONNECT_TIMEOUT, 1000)
            .property(ClientProperties.READ_TIMEOUT, 1000)
            .path(METHOD_SQL_API);
    private static final Logger logger = LoggerFactory.getLogger(SqlMethod.class);

    /**
     * Execute SQL queryResponse and retrieve result in specified format
     *
     * @param sqlQuery     SQL query in a String format
     * @param outputFormat some field from {@link OutputFormat}
     * @param limit        limit of returned Rows
     * @return instance of Response
     */
    public static Response queryResponse(String sqlQuery, OutputFormat outputFormat, Integer limit) {
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

    public static StringTable queryTable(String sqlQuery, Integer limit) {
        Response response = queryResponse(sqlQuery, limit);
        Integer statusCode = response.getStatus();
        if (OK.getStatusCode() == statusCode) {
            return response.readEntity(StringTable.class);
        }

        if (BAD_REQUEST.getStatusCode() == statusCode) {
            Error atsdError = response.readEntity(AtsdExceptionDescription.class).getErrors().get(0);

            throw new IllegalStateException(String.format("%s.%n\tQuery: %s", atsdError.getMessage(), sqlQuery));
        }
        String errorMessage = String.format("Unexpected behavior on server when executing sql query.%n \t Query: %s",
                sqlQuery
        );
        throw new IllegalStateException(errorMessage);
    }

    public static StringTable queryTable(String sqlQuery) {
        return queryTable(sqlQuery, null);
    }

    /**
     * Execute SQL queryResponse and retrieve result in specified format
     *
     * @param sqlQuery SQL queryResponse in a String format
     * @param limit    limit of returned rows
     * @return instance of Response
     */
    public static Response queryResponse(String sqlQuery, Integer limit) {
        return queryResponse(sqlQuery, OutputFormat.JSON, limit);
    }

    /**
     * Execute SQL queryResponse and retrieve result in specified format
     *
     * @param sqlQuery SQL queryResponse in a String format
     * @return instance of Response
     */
    public static Response queryResponse(String sqlQuery) {
        return queryResponse(sqlQuery, OutputFormat.JSON, null);
    }
}
