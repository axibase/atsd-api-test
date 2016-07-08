package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class CSVInsertMethod extends SeriesMethod {
    protected static final String METHOD_CSV_INSERT = "/series/csv/{entity}";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Response csvInsert(String entity, String csv, Map<String, String> tags) throws InterruptedException {
        WebTarget webTarget = httpApiResource.path(METHOD_CSV_INSERT).resolveTemplate("entity", entity);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
        }

        Response response = webTarget.request().post(Entity.entity(csv, new MediaType("text", "csv")));
        response.bufferEntity();
        Thread.sleep(Util.REQUEST_INTERVAL);
        return response;
    }

    public static Response csvInsert(String entity, String csv) throws InterruptedException {
        return csvInsert(entity, csv, new HashMap<String, String>());
    }


}
