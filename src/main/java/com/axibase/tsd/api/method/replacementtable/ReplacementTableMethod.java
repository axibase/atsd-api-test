package com.axibase.tsd.api.method.replacementtable;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.util.NotCheckedException;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
public class ReplacementTableMethod extends BaseMethod {
    private static final String METHOD_TABLE_JSON = "/replacement-tables/json/{table}";

    private static WebTarget resolveTable(WebTarget webTarget, String tableName) {
        return webTarget.path(METHOD_TABLE_JSON).resolveTemplate("table", tableName);
    }

    private static Response createResponse(ReplacementTable table) {
        Response response = executeApiRequest(webTarget ->
                resolveTable(webTarget, table.getName())
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .request()
                .put(Entity.json(table)));

        response.bufferEntity();

        return response;
    }

    public static void createCheck(ReplacementTable table) {
        Response response = createResponse(table);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            String errorMessage = "Wasn't able to create a replacement table, Status Info is " + response.getStatusInfo();
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private static Response getReplacementTablesResponse(String replacementTableName) {
        Response response = executeApiRequest(webTarget ->
                resolveTable(webTarget, replacementTableName)
                .request().get());
        response.bufferEntity();
        return response;
    }

    public static boolean replacementTableExist(String replacementTableName) throws NotCheckedException {
        replacementTableName = replacementTableName.replace(" ", "_").toLowerCase();
        final Response response = ReplacementTableMethod.getReplacementTablesResponse(replacementTableName);
        if (response.getStatus() != OK.getStatusCode()) {
            if (response.getStatus() == NOT_FOUND.getStatusCode()) {
                return false;
            }
            String message = "Fail to execute replacement table query";
            log.error(message);
            throw new NotCheckedException(message);
        }

        try {
            ReplacementTable replacementTable = response.readEntity(ReplacementTable.class);
            if (!replacementTable.getName().equalsIgnoreCase(replacementTableName)) {
                String message = "ReplacementTable API returned an entry we weren't asking for.";
                log.error(message);
                throw new NotCheckedException(message);
            }
        } catch (ProcessingException err) {
            NotCheckedException exception = new NotCheckedException("Could not parse Replacement Table from JSON: " + err.getMessage());
            exception.addSuppressed(err);
            log.error(exception.getMessage());
            throw exception;
        }
        return true;
    }
}
