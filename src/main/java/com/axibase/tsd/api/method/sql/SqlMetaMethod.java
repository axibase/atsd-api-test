package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.sql.AtsdExceptionDescription;
import com.axibase.tsd.api.model.sql.Error;
import com.axibase.tsd.api.model.sql.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;

public class SqlMetaMethod extends BaseMethod {
    private static final String METHOD_SQL_META_API = "/api/sql/meta";
    protected static final WebTarget httpSqlApiResource = httpRootResource.path(METHOD_SQL_META_API);
    private static final Logger logger = LoggerFactory.getLogger(SqlMethod.class);

    public static TableMetaData queryMetaData(String sqlQuery) {
        logger.debug("Meta query for: {}", sqlQuery);
        Form form = new Form();
        if (sqlQuery == null) {
            throw new IllegalStateException("Query must be defined");
        }
        form.param("q", sqlQuery);
        Response response = httpSqlApiResource
                .request()
                .post(Entity.form(form));
        response.bufferEntity();

        Integer statusCode = response.getStatus();
        if (OK.getStatusCode() == statusCode) {
            return response.readEntity(TableMetaData.class);
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
}
