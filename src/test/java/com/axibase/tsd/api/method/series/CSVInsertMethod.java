package com.axibase.tsd.api.method.series;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.OK;

public class CSVInsertMethod extends SeriesMethod {
    protected static final String METHOD_CSV_INSERT = "/series/csv/";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected boolean csvInsert(String entity, String csv, Map<String, String> tags, long sleepDuration) throws IOException, InterruptedException {
        WebTarget csvInsert = httpApiResource.path(METHOD_CSV_INSERT).path("{entity}");
        if (tags != null && tags.size() > 0) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                csvInsert = csvInsert.queryParam(entry.getKey(), entry.getValue());
            }
        }

        Response response = csvInsert.resolveTemplate("entity", entity).request().post(Entity.entity(csv, new MediaType("text", "csv")));
        Thread.sleep(sleepDuration);
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("CSV looks inserted");
        } else {
            logger.error("Fail to insert csv");
        }
        return OK.getStatusCode() == response.getStatus();
    }

    protected boolean csvInsert(String entity, String csv, Map<String, String> tags) throws IOException, InterruptedException {
        return csvInsert(entity, csv, tags, 0);
    }

    protected boolean csvInsert(String entity, String csv, long sleepDuration) throws IOException, InterruptedException {
        return csvInsert(entity, csv, null, sleepDuration);
    }

    protected boolean csvInsert(String entity, String csv) throws IOException, InterruptedException {
        return csvInsert(entity, csv, null, 0);
    }
}
