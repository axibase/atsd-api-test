package com.axibase.tsd.api.method.tokens;

import lombok.Data;


import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.Response;

import com.axibase.tsd.api.method.BaseMethod;

import org.testng.annotations.Test;

import io.qameta.allure.Issue;

import java.util.concurrent.ConcurrentHashMap;

@Issue("6052")
public class TokenRepository extends BaseMethod {
    private static final ConcurrentMap<TokenRequest, String> tokens = new ConcurrentHashMap<>();

    @Issue("6052")
    public static String getToken(String user, String method, String url){
        return tokens.computeIfAbsent(new TokenRequest(user, method, url), TokenRepository::generateTokenInAtsd);
        
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
                    .queryParam("expirationDate")
                    .queryParam("Addresses")
                    .queryParam("create", "Issue+Token")
                    .request()
                    .method("POST"));
        String[] responseUrl = response.getHeaderString("Location").split(";")[0].split("/");
        String token=responseUrl[responseUrl.length-1];
        return token;
    }

    @Data
    private static final class TokenRequest {
        private final String user;
        private final String method;
        private final String url;
    }
}