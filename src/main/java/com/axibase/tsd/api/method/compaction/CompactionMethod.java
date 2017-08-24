package com.axibase.tsd.api.method.compaction;

import com.axibase.tsd.api.method.BaseMethod;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.FOUND;

public class CompactionMethod extends BaseMethod {
    public static void performCompaction() {
        WebTarget httpCompactionResource = httpRootResource.path("/admin/compaction")
                .queryParam("start_compaction", "");
        Response response = httpCompactionResource.request().post(Entity.text(""));
        response.bufferEntity();
        if (response.getStatus() != FOUND.getStatusCode()) {
            throw new IllegalStateException("Failed to perform compaction!");
        }
    }
}