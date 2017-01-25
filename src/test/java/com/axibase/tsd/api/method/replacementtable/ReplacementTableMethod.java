package com.axibase.tsd.api.method.replacementtable;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

public class ReplacementTableMethod extends BaseMethod {
    private static final Logger logger = LoggerFactory.getLogger(ReplacementTableMethod.class);
    private static final String PATH = "/entities/lookup";
    private static final WebTarget resource = httpRootResource.path(PATH).property(ClientProperties.FOLLOW_REDIRECTS, false);

    protected static Response createResponse(ReplacementTable table) {
        MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        parameters.add("lookupName", table.getName());
        parameters.add("items", squashMapIntoString(table.getMap()));
        parameters.add("oldName", "");
        parameters.add("save", "Save");
        Form form = new Form(parameters);

        Response response = resource
                .request()
                .post(Entity.form(form));
        response.bufferEntity();

        return response;
    }

    public static void createCheck(ReplacementTable table) {
        Response response = createResponse(table);

        // It's confusing, but we get code 302 -- FOUND - Redirect, only if table was created
        if (response.getStatus() != Response.Status.FOUND.getStatusCode()) {
            String errorMessage = "Wasn't able to create a replacement table, Status Info is " + response.getStatusInfo();
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private static String squashMapIntoString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
