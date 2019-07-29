package com.axibase.tsd.api.method.replacementtable;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

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

        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
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

    public static boolean replacementTableExist(ReplacementTable replacementTable) throws NotCheckedException {
        String replacementTableName = replacementTable.getName().replace(" ", "_").toLowerCase();
        final Response response = getReplacementTablesResponse(replacementTableName);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            if (response.getStatus() == NOT_FOUND.getStatusCode()) {
                return false;
            }
            String message = "Fail to execute replacement table query: " + response.getStatusInfo();
            log.error(message);
            throw new NotCheckedException(message);
        }

        try {
            ReplacementTable newReplacementTable = response.readEntity(ReplacementTable.class);
            if (!StringUtils.equalsIgnoreCase(replacementTableName, newReplacementTable.getName())) {
                String message = "ReplacementTable API returned an entry we weren't asking for.";
                log.error(message);
                throw new NotCheckedException(message);
            }
            for(Map.Entry<String, String> entry: newReplacementTable.getKeys().entrySet()) {
                if(!replacementTable.getKeys().get(entry.getKey()).equals(entry.getValue())) {
                    String message = "ReplacementTable with the name " + replacementTableName + " exists, but does not equal to the given one.";
                    throw new NotCheckedException(message);
                }
            }

        } catch (ProcessingException err) {
            NotCheckedException exception = new NotCheckedException("Could not parse Replacement Table from JSON: " + err.getMessage());
            exception.addSuppressed(err);
            log.error(exception.getMessage());
            throw exception;
        }
        return true;
    }

    public static boolean replacementTableExist(String replacementTableName) throws NotCheckedException {
        replacementTableName = replacementTableName.replace(" ", "_").toLowerCase();
        final Response response = getReplacementTablesResponse(replacementTableName);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            if (response.getStatus() == NOT_FOUND.getStatusCode()) {
                return false;
            }
            String message = "Fail to execute replacement table query: " + response.getStatusInfo();
            log.error(message);
            throw new NotCheckedException(message);
        }

        try {
            ReplacementTable replacementTable = response.readEntity(ReplacementTable.class);
            if (!StringUtils.equalsIgnoreCase(replacementTableName, replacementTable.getName())) {
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
