package com.axibase.tsd.api.method.export;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

public class ExportMethod extends BaseMethod {
    private final static String EXPORT_PATH = "/export";

    public static Response sendGetRequest(final Map<String, Object> requestParameters) {
        RequestSenderWithBasicAuthorization sender = new RequestSenderWithBasicAuthorization(
                Config.getInstance().getLogin(),
                Config.getInstance().getPassword()
        );
        Response response = sender.executeRootRequest(
                EXPORT_PATH,
                Collections.emptyMap(),
                requestParameters,
                Collections.emptyMap(),
                HttpMethod.GET
        );
        response.bufferEntity();
        return response;
    }
}
