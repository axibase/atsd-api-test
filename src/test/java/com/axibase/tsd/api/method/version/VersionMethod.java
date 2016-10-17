package com.axibase.tsd.api.method.version;

import com.axibase.tsd.api.method.BaseMethod;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class VersionMethod extends BaseMethod {
    private static final String METHOD_VERSION = "/version";
    private static WebTarget httpVersionApiResource = httpRootResource
            .path(METHOD_VERSION);

    public static Response queryVersion() {
        Response response = httpVersionApiResource
                .request()
                .get();
        response.bufferEntity();
        return response;
    }
}
