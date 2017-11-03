package com.axibase.tsd.api.method.version;

import com.axibase.tsd.api.method.BaseMethod;

import javax.ws.rs.core.Response;

public class VersionMethod extends BaseMethod {
    private static final String METHOD_VERSION = "/version";

    public static Response queryVersion() {
        Response response = executeRootRequest(webTarget -> webTarget
                .path(METHOD_VERSION)
                .request()
                .get());
        response.bufferEntity();
        return response;
    }
}
