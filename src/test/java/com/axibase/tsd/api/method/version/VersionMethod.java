package com.axibase.tsd.api.method.version;

import com.axibase.tsd.api.method.BaseMethod;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

public class VersionMethod extends BaseMethod {
    private static final String METHOD_VERSION = "/version";
    protected static WebTarget httpSqlApiResource = httpRootResource
            .property(ClientProperties.CONNECT_TIMEOUT, 1000)
            .property(ClientProperties.READ_TIMEOUT, 1000)
            .path(METHOD_VERSION);

    public static Response queryVersion() {
        Response response = httpSqlApiResource
                .request()
                .get();
        response.bufferEntity();
        return response;
    }

    public static Version queryVersionCheck() {
        Response response = queryVersion();
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Failed to get version");
        }
        return response.readEntity(Version.class);
    }
}
