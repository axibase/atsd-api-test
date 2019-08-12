package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.TokenCheck;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TokenRepository extends BaseMethod {
    private static final ConcurrentMap<TokenRequest, String> tokens = new ConcurrentHashMap<>();

    public static String getToken(String user, String method, String urls) throws Exception {
        Config config = Config.getInstance();
        String apiPath = config.getApiPath();
        return tokens.computeIfAbsent(new TokenRequest(user, method, apiPath + urls.replaceAll(",", "," + apiPath).replaceAll("\n", "," + apiPath)), TokenRepository::generateTokenInAtsd);

    }

    private static String generateTokenInAtsd(TokenRequest tokenRequest) {
        String user = tokenRequest.getUser();
        String method = tokenRequest.getMethod();
        String url = tokenRequest.getUrl();

        String requestString = "/admin/users/tokens/new";
        Response response = executeRootRequest(webTarget -> webTarget.path(requestString)
                .queryParam("user", user)
                .queryParam("username", user)
                .queryParam("method", method)
                .queryParam("urls", url)
                .request()
                .method(HttpMethod.POST, Entity.json("user=" + user)));
        response.bufferEntity();
        String token = StringUtils.substringAfterLast(StringUtils.substringBefore(response.getHeaderString("Location"), ";"), "/");
        Checker.check(new TokenCheck(token));
        return token;
    }

    @Data
    private static final class TokenRequest {
        private final String user;
        private final String method;
        private final String url;
    }
}