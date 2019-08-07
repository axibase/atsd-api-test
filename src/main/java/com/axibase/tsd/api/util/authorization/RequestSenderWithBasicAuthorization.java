package com.axibase.tsd.api.util.authorization;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import lombok.RequiredArgsConstructor;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Map;

@RequiredArgsConstructor
public class RequestSenderWithBasicAuthorization extends RequestSenderWithAuthorization {
    private final String username;
    private final String password;

    public static final RequestSenderWithBasicAuthorization DEFAULT_BASIC_SENDER = new RequestSenderWithBasicAuthorization();

    private RequestSenderWithBasicAuthorization() {
        Config config = Config.getInstance();
        this.username = config.getLogin();
        this.password = config.getPassword();
    }

    @Override
    public Response executeApiRequest(String path, Map<String, Object> templateReplacements, Map<String, Object> params,
                                      Map<String, Object> additionalHeaders, String httpMethod, Entity<?> entity) {
        return BaseMethod.executeApiRequest(webTarget ->
                prepareBuilder(webTarget, path, templateReplacements, params, additionalHeaders)
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
                        .method(httpMethod, entity)
        );
    }

    @Override
    public Response executeApiRequest(String path, Map<String, Object> templateReplacements,
                                      Map<String, Object> params, Map<String, Object> additionalHeaders, String httpMethod) {
        return BaseMethod.executeApiRequest(webTarget ->
                prepareBuilder(webTarget, path, templateReplacements, params, additionalHeaders)
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
                        .method(httpMethod)
        );
    }
}
