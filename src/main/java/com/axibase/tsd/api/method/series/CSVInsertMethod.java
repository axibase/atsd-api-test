package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.util.Util;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

public class CSVInsertMethod extends SeriesMethod {
    private static final String METHOD_CSV_INSERT = "/series/csv/{entity}";

    public static Response csvInsert(String entity, String csv, Map<String, String> tags, String user, String password) {
        Response response = executeApiRequest(webTarget -> {
            WebTarget target = webTarget.path(METHOD_CSV_INSERT).resolveTemplate("entity", entity);
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());
            }

            Invocation.Builder builder = target.request();
            if (user != null && password != null) {
                builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
                builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
            }
            return builder.post(Entity.entity(csv, new MediaType("text", "csv")));
        });

        response.bufferEntity();
        return response;
    }

    public static Response csvInsert(String entity, String csv, Map<String, String> tags) {
        return csvInsert(entity, csv, tags, null, null);
    }

    public static Response csvInsert(String entity, String csv) {
        return csvInsert(entity, csv, Collections.EMPTY_MAP);
    }

    public void csvInsertCheck(AbstractCheck check, String entity, String csv, Map<String, String> tags) {
        Response response = csvInsert(entity, csv, tags);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            String errorMessage = String.format(
                    "Failed to insert Series as CSV for entity %s with payload : %n %s",
                    entity, csv
            );
            throw new IllegalStateException(errorMessage);
        }
        Checker.check(check);
    }
}
