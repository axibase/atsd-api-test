package com.axibase.tsd.api.method.compaction;

import com.axibase.tsd.api.method.BaseMethod;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

public class CompactionMethod extends BaseMethod {
    public static void performCompaction(String day, Boolean historical) {
        WebTarget httpCompactionResource = httpRootResource.path("compaction");
        if (day != null) {
            httpCompactionResource.queryParam("day", day);
        }
        if (historical != null) {
            httpCompactionResource.queryParam("historical", historical).request().get().close();
        }
        Response response = httpCompactionResource.request().get();
        response.bufferEntity();
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Failed to perform compaction!");
        }
    }

    public static void performCompaction(String day) {
        performCompaction(day, null);
    }

    public static void performCompaction(Boolean historical) {
        performCompaction(null, historical);
    }

    public static void performCompaction() {
        performCompaction(null, null);
    }
}