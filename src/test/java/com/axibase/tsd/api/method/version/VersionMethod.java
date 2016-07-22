package com.axibase.tsd.api.method.version;

import com.axibase.tsd.api.method.BaseMethod;

import javax.ws.rs.core.Response;

public class VersionMethod extends BaseMethod {
    private static final String METHOD_VERSION = "/version";

    public static Response execute() {
        Response response = httpApiResource.path(METHOD_VERSION).request().get();
        response.bufferEntity();
        return response;
    }
}