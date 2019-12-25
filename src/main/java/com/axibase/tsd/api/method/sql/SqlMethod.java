package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.sql.AtsdExceptionDescription;
import com.axibase.tsd.api.model.sql.Error;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Function;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class SqlMethod extends BaseMethod {
    private static final String METHOD_SQL_API = "/api/sql";
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

        Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .post(Entity.form(form)));

        response.bufferEntity();
        return response;
    }

    public static Response executeSqlRequest(Function<WebTarget, Response> sqlFunction) {
        return executeRootRequest(webTarget -> sqlFunction.apply(webTarget.path(METHOD_SQL_API)));
    }

    public static StringTable queryTable(String sqlQuery, Integer limit) {
        Response response = queryResponse(sqlQuery, limit);
        int statusCode = response.getStatus();
        String atsdError;

        try {
            List<Error> errors = response.readEntity(AtsdExceptionDescription.class).getErrors();
            if (errors != null && errors.size() != 0) {
                atsdError = errors.get(0).getMessage();
            } else {
                atsdError = null;
            }
        } catch (ProcessingException e) {
            atsdError = null;
        }

        /* Even if status == OK, the response may contain errors */
        if (atsdError != null && (Response.Status.Family.SUCCESSFUL == Util.responseFamily(response) ||
                BAD_REQUEST.getStatusCode() == statusCode)) {
            throw new IllegalStateException(String.format("%s.%n   Query: %s", atsdError, sqlQuery));
        }

        if (Response.Status.Family.SUCCESSFUL == Util.responseFamily(response)) {
            return response.readEntity(StringTable.class);
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
