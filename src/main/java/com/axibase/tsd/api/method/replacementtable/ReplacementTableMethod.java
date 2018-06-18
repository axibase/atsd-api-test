package com.axibase.tsd.api.method.replacementtable;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.util.NotCheckedException;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public class ReplacementTableMethod extends BaseMethod {
    private static final Logger logger = LoggerFactory.getLogger(ReplacementTableMethod.class);
    private static final String METHOD_TABLE_JSON = "/replacement-tables/json/{table}";

    private static Response createResponse(ReplacementTable table) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_TABLE_JSON)
                .resolveTemplate("table", table.getName())
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
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private static Response getReplacementTablesResponse(String replacementTableName) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_TABLE_JSON)
                .resolveTemplate("table", replacementTableName)
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
            throw new NotCheckedException("Fail to execute replacement table query");
        }

        try {
            ReplacementTable replacementTable = response.readEntity(ReplacementTable.class);
            if (!replacementTable.getName().equalsIgnoreCase(replacementTableName)) {
                throw new NotCheckedException("ReplacementTable API returned an entry we weren't asking for.");
            }
        } catch (ProcessingException err) {
            NotCheckedException exception = new NotCheckedException("Could not parse Replacement Table from JSON: " + err.getMessage());
            exception.addSuppressed(err);
            throw exception;
        }
        return true;
    }
}
