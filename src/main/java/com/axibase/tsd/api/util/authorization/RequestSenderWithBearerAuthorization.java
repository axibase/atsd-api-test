package com.axibase.tsd.api.util.authorization;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;

public class RequestSenderWithBearerAuthorization extends RequestSenderWithAuthorization {
    private static final String API_PATH = Config.getInstance().getApiPath();
    private final String headerValue;

    public RequestSenderWithBearerAuthorization(String token) {
        this.headerValue = "Bearer " + token;
    }

    @Override
    public Response executeApiRequest(String path, Map<String, Object> templateReplacements, Map<String, Object> params,
                                      Map<String, Object> additionalHeaders, String httpMethod, Entity<?> entity) {
        return BaseMethod.executeTokenRootRequest(webTarget ->
                prepareBuilder(webTarget, API_PATH + path, templateReplacements, params, additionalHeaders)
                        .header(HttpHeaders.AUTHORIZATION, headerValue)
                        .method(httpMethod, entity)
        );
    }

    @Override
    public Response executeApiRequest(String path, Map<String, Object> templateReplacements,
                                      Map<String, Object> params, Map<String, Object> additionalHeaders, String httpMethod) {
        return BaseMethod.executeTokenRootRequest(webTarget ->
                prepareBuilder(webTarget, API_PATH + path, templateReplacements, params, additionalHeaders)
                        .header(HttpHeaders.AUTHORIZATION, headerValue)
                        .method(httpMethod)
        );
    }

    @Override
    public Response executeRootRequest(String path, Map<String, Object> templateReplacements, Map<String, Object> params, Map<String, Object> additionalHeaders, String httpMethod, Entity<?> entity) {
        return BaseMethod.executeTokenRootRequest(webTarget ->
                prepareBuilder(webTarget, path, templateReplacements, params, additionalHeaders)
                        .header(HttpHeaders.AUTHORIZATION, headerValue)
                        .method(httpMethod, entity)
        );
    }

    @Override
    public Response executeRootRequest(String path, Map<String, Object> templateReplacements, Map<String, Object> params, Map<String, Object> additionalHeaders, String httpMethod) {
        return BaseMethod.executeTokenRootRequest(webTarget ->
                prepareBuilder(webTarget, path, templateReplacements, params, additionalHeaders)
                        .header(HttpHeaders.AUTHORIZATION, headerValue)
                        .method(httpMethod)
        );
    }
}
