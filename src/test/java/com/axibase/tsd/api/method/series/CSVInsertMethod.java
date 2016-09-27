package com.axibase.tsd.api.method.series;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class CSVInsertMethod extends SeriesMethod {
    protected static final String METHOD_CSV_INSERT = "/series/csv/{entity}";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Response csvInsert(String entity, String csv, Map<String, String> tags, String user, String password) {
        WebTarget webTarget = httpApiResource.path(METHOD_CSV_INSERT).resolveTemplate("entity", entity);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
        }

        Invocation.Builder builder = webTarget.request();
        if (user != null && password != null) {
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
        }
        Response response = builder.post(Entity.entity(csv, new MediaType("text", "csv")));
        response.bufferEntity();
        return response;
    }
    public static Response csvInsert(String entity, String csv, Map<String, String> tags) throws InterruptedException {
        Response response = csvInsert(entity, csv, tags, null, null);
        Thread.sleep(DEFAULT_EXPECTED_PROCESSING_TIME);
        return response;
    }

    public static Response csvInsert(String entity, String csv) throws InterruptedException {
        return csvInsert(entity, csv, new HashMap<String, String>());
    }


}
