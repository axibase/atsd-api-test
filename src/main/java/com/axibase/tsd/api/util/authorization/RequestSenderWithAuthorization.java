package com.axibase.tsd.api.util.authorization;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;


public abstract class RequestSenderWithAuthorization {
    /**
     * Executes request to /api/v1/* endpoint
     *
     * @param path                 Rest API endpoint excluding /api/v1 part
     * @param templateReplacements Map of path replacements: key - template, value - replacement. Leave empty map if no templates to replace
     * @param additionalHeaders    Specific headers needed by the request, e.g. Content-Type. Leave empty map if no headers needed
     * @param params               Map of query params, e.g. startDate. Leave empty map if no params are needed
     * @param httpMethod           Name of Http method
     * @param entity               Payload
     * @return Response
     */
    public abstract Response executeApiRequest(String path, Map<String, Object> templateReplacements, Map<String, Object> params,
                                               Map<String, Object> additionalHeaders, String httpMethod, Entity<?> entity);

    public abstract Response executeApiRequest(String path, Map<String, Object> templateReplacements, Map<String, Object> params,
                                               Map<String, Object> additionalHeaders, String httpMethod);

    public Response executeApiRequest(String path, Map<String, Object> templateReplacements, String httpMethod, Entity<?> entity) {
        return executeApiRequest(path, templateReplacements, Collections.EMPTY_MAP, Collections.EMPTY_MAP, httpMethod, entity);
    }

    public Response executeApiRequest(String path, Map<String, Object> templateReplacements, String httpMethod) {
        return executeApiRequest(path, templateReplacements, Collections.EMPTY_MAP, Collections.EMPTY_MAP, httpMethod);
    }

    protected Invocation.Builder prepareBuilder(WebTarget webTarget,
                                                String path, Map<String, Object> templateReplacements, Map<String, Object> params,
                                                Map<String, Object> additionalHeaders) {
        WebTarget target = webTarget.path(path);
        for (Map.Entry<String, Object> entry : templateReplacements.entrySet()) {
            target = target.resolveTemplate(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }

        Invocation.Builder builder = target.request();
        for (Map.Entry<String, Object> entry : additionalHeaders.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }
        return builder;
    }
}
