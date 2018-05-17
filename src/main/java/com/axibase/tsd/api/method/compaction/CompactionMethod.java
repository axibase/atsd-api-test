package com.axibase.tsd.api.method.compaction;

import com.axibase.tsd.api.method.BaseMethod;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import static javax.ws.rs.core.Response.Status.FOUND;


@Slf4j
public class CompactionMethod extends BaseMethod {
    public static void performCompaction() {
        Response response = executeRootRequest(webTarget -> webTarget
                .path("/admin/compaction")
                .queryParam("start_compaction", "")
                .request()
                .post(Entity.text("")));

        response.bufferEntity();
        final int status = response.getStatus();
        Family statusFamily = Family.familyOf(status);
        if (!Family.REDIRECTION.equals(statusFamily) && !Family.SUCCESSFUL.equals(statusFamily)) {
            throw new IllegalStateException("Failed to perform compaction!");
        }
    }
}