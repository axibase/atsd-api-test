package com.axibase.tsd.api.method.tokens;

import lombok.Data;


import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.Response;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import io.qameta.allure.Issue;

import java.util.concurrent.ConcurrentHashMap;

public class TokenRepository extends BaseMethod {
    private static final ConcurrentMap<TokenRequest, String> tokens = new ConcurrentHashMap<>();

    public static String getToken(String user, String method, String url) throws Exception{
        Config config = Config.getInstance();
        String apiPath=config.getApiPath();
        return tokens.computeIfAbsent(new TokenRequest(user, method, apiPath+url), TokenRepository::generateTokenInAtsd);
        
    }

    private static String generateTokenInAtsd(TokenRequest tokenRequest) {
        String user = tokenRequest.getUser();
        String method = tokenRequest.getMethod();
        String url = tokenRequest.getUrl();


        String requestString ="/admin/users/tokens/new";
        Response response = executeRootRequest(webTarget -> webTarget.path(requestString)
                    .queryParam("user", user)
                    .queryParam("username",user)
                    .queryParam("method", method)
                    .queryParam("urls", url)
                    .request()
                    .method("POST"));
        String token=StringUtils.substringAfterLast(StringUtils.substringBefore(response.getHeaderString("Location"),";"), "/");
        return token;
    }

    @Data
    private static final class TokenRequest {
        private final String user;
        private final String method;
        private final String url;
    }
}