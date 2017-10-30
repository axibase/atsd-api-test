package com.axibase.tsd.api.method.compaction;

import com.axibase.tsd.api.method.BaseMethod;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.FOUND;

public class CompactionMethod extends BaseMethod {
    public static void performCompaction() {
        Response response = executeRootRequest(webTarget -> webTarget
                .path("/admin/compaction")
                .queryParam("start_compaction", "")
                .request()
                .post(Entity.text("")));

        response.bufferEntity();
        if (response.getStatus() != FOUND.getStatusCode()) {
            throw new IllegalStateException("Failed to perform compaction!");
        }
    }
}