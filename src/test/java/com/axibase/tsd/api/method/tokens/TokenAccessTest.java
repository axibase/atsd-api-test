package com.axibase.tsd.api.method.tokens;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import com.axibase.tsd.api.method.BaseMethod;

import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.qameta.allure.Issue;

import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


public class TokenAccessTest extends BaseMethod {

    @DataProvider
    private Object[][] availablePaths() {
        return new String[][]{
                // Data API
                {"/series/query", HttpMethod.POST},
                {"/series/insert", HttpMethod.POST},
                {"/series/csv/entity", HttpMethod.POST},
                {"/series/json/entity/metric", HttpMethod.GET},
                {"/series/csv/entity/metric", HttpMethod.GET},
                {"/series/query", HttpMethod.POST},
                {"/series/delete", HttpMethod.POST},
                {"/properties/query", HttpMethod.POST},
                {"/properties/insert", HttpMethod.POST},
                {"/properties/delete", HttpMethod.POST},
                {"/properties/entity/types/type", HttpMethod.GET},
                {"/properties/entity/types", HttpMethod.GET},
                {"/messages/query", HttpMethod.POST},
                {"/messages/insert", HttpMethod.POST},
                {"/messages/stats", HttpMethod.POST},
                {"/messages/stats/query", HttpMethod.POST},
                {"/messages/webhook", HttpMethod.POST},
                {"/messages/webhook", HttpMethod.GET},
                {"/alerts", HttpMethod.POST},
                {"/alerts/query", HttpMethod.POST},
                {"/alerts/update", HttpMethod.POST},
                {"/alerts/delete", HttpMethod.POST},
                {"/alerts/history", HttpMethod.POST},
                {"/alerts/history/query", HttpMethod.POST},
                {"/csv", HttpMethod.POST},
                {"/nmon", HttpMethod.POST},
                {"/command", HttpMethod.POST},
                {"/commands/batch", HttpMethod.POST},
                {"/export", HttpMethod.GET},
                // Meta API
                {"/metrics", HttpMethod.GET},
                {"/metrics/metric", HttpMethod.GET},
                {"/metrics/metric", HttpMethod.PUT},
                {"/metrics/metric", "PATCH"},
                {"/metrics/metric", HttpMethod.DELETE},
                {"/metrics/metric/rename", HttpMethod.POST},
                {"/metrics/metric/series", HttpMethod.GET},
                {"/entities", HttpMethod.GET},
                {"/entities", HttpMethod.POST},
                {"/entities/entity", HttpMethod.GET},
                {"/entities/entity", HttpMethod.PUT},
                {"/entities/entity", "PATCH"},
                {"/entities/entity", HttpMethod.DELETE},
                {"/entities/entity/groups", HttpMethod.GET},
                {"/entities/entity/metrics", HttpMethod.GET},
                {"/entities/entity/property-types", HttpMethod.GET},
                {"/entity-groups", HttpMethod.GET},
                {"/entity-groups/group", HttpMethod.GET},
                {"/entity-groups/group", HttpMethod.PUT},
                {"/entity-groups/group", "PATCH"},
                {"/entity-groups/group", HttpMethod.DELETE},
                {"/entity-groups/group/entities", HttpMethod.GET},
                {"/entity-groups/group/entities/add", HttpMethod.POST},
                {"/entity-groups/group/entities/set", HttpMethod.POST},
                {"/entity-groups/group/entities/delete", HttpMethod.POST},
                {"/replacement-tables/csv", HttpMethod.GET},
                {"/replacement-tables/json", HttpMethod.GET},
                {"/replacement-tables/csv/replacement-table", HttpMethod.GET},
                {"/replacement-tables/json/replacement-table", HttpMethod.GET},
                {"/replacement-tables/replacement-table", HttpMethod.DELETE},
                {"/replacement-tables/csv/replacement-table", HttpMethod.PUT},
                {"/replacement-tables/json/replacement-table", HttpMethod.PUT},
                {"/replacement-tables/csv/replacement-table", "PATCH"},
                {"/replacement-tables/json/replacement-table", "PATCH"},
                {"/search", HttpMethod.GET},
                {"/version", HttpMethod.GET},
                {"/ping", HttpMethod.GET}
        };
    }

    @DataProvider
    private Object[][] users() {
        return new String[][]{
                {TokenUsers.ADMIN_NAME},
                {TokenUsers.USER_NAME}
        };
    }

    @DataProvider
    private Object[][] userAndPathAndMethod() {
        List<Object[]> pathsAndMethods = Arrays.asList(availablePaths());
        List<Object[]> result = new ArrayList<>();

        for (Object[] arr : pathsAndMethods) {
            result.add(ArrayUtils.addAll(arr, new String[]{TokenUsers.ADMIN_NAME}));
            result.add(ArrayUtils.addAll(arr, new String[]{TokenUsers.USER_NAME}));
        }

        return result.toArray(new Object[pathsAndMethods.size()][3]);
    }


    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void tokenAccessToItsUrlTest(String path, String method, String username) throws Exception {
        String token = TokenRepository.getToken(username, method, path);
        Response response = executeApiRequest(token, path, method);
        assertNotUnauthorized("Token for path " + path + " and method " + method + " failed to authorise user " + username,
                response);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void tokenAccessToOtherUrlsTest(String path, String method, String username) throws Exception {
        String availablePath = "/csv";
        String availableMethod = HttpMethod.POST;
        if ((path.equals(availablePath) && method.equals(availableMethod))) {
            return;
        }
        String token = TokenRepository.getToken(username, availableMethod, availablePath);
        Response response = executeApiRequest(token, path, method);
        assertUnauthorized("Token for path " + path + " and method " + method + " did not fail to authorise user " + username,
                response);
        assertCode15("Token for path " + path + " and method " + method + " did not give code 15 in response for user " + username,
                response);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void tokenAccessToSimilarRootUrlsDenialTest(String path, String method, String username) throws Exception {
        String token = TokenRepository.getToken(username, method, path);
        Response response = executeRootRequest(token, path, method);
        assertUnauthorized("Token for path " + path + " and method " + method + " did not fail to authorise user " + username + " for root request.",
                response);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void testTokenAccessToSimilarApiUrlsDenial(String path, String method, String username) throws Exception {
        String token = TokenRepository.getToken(username, method, path);
        Response response = executeRootRequest(token, "/api" + path, method);
        assertUnauthorized("Token for path " + path + " and method " + method + " did not fail to authorise user " + username + " for /api request.",
                response);
    }

    @Test(
            dataProvider = "users"
    )
    @Issue("6052")
    public void tokenAccessToUrlParamsDenialTest(String username) throws Exception {
        String path = "/csv";
        String paramName = "config";
        String paramValue = "not_valid_config";
        String method = HttpMethod.POST;
        String token = TokenRepository.getToken(username, method, path);
        Response response = executeApiRequestWithParams(token, path, method, ImmutableMap.of(paramName, paramValue));

        assertUnauthorized("Authorisation not failed with not valid parameter on token: " + token + " Path: " + path + " Method: " + method + "User: " + username,
                response);
        assertCode15("Token for path " + path + " and method " + method + "with params " + paramName + "=" + paramValue + " did not give code 15 in response for user " + username,
                response);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void dualTokenAccessToItsUrlsTest(String path, String method, String username) throws Exception {
        String anotherPath = findAnotherPath(method);
        if (path.equals(anotherPath)) {
            return;
        }
        String token = TokenRepository.getToken(username, method, path + "," + anotherPath);
        Response firstResponse = executeApiRequest(token, path, method);
        Response secondResponse = executeApiRequest(token, anotherPath, method);

        assertNotUnauthorized("Dual token for urls " + path + "," + anotherPath + " failed to grand access to " + path + " for user " + username,
                firstResponse);
        assertNotUnauthorized("Dual token for urls " + path + "," + anotherPath + " failed to grand access to " + anotherPath + " for user " + username,
                secondResponse);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void dualTokenAccessToOtherUrlsTest(String path, String method, String username) throws Exception {
        String firstPath = "/csv";
        String secondPath = "/command";
        String availableMethod = HttpMethod.POST;
        if (path.equals(firstPath) || path.equals(secondPath) && method.equals(availableMethod)) {
            return;
        }
        String token = TokenRepository.getToken(username, availableMethod, firstPath + "," + secondPath);
        Response response = executeApiRequest(token, path, method);

        assertUnauthorized("Dual token for urls " + firstPath + "," + secondPath + " and method " + method + " did not fail to authorise user " + username + " to path " + path,
                response);
        assertCode15("Dual token for urls " + firstPath + "," + secondPath + " and method " + method + " did not fail to authorise user " + username + " to path " + path,
                response);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void dualTokenAccessToSimilarRootUrlsDenialTest(String path, String method, String username) throws Exception {
        String anotherPath = findAnotherPath(method);
        if (path.equals(anotherPath)) {
            return;
        }
        String token = TokenRepository.getToken(username, method, path + "," + anotherPath);
        Response firstResponse = executeRootRequest(token, path, method);
        Response secondResponse = executeRootRequest(token, anotherPath, method);

        assertUnauthorized("Dual Token for path " + path + "and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for root request for path " + path,
                firstResponse);
        assertUnauthorized("Dual Token for path " + path + "and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for root request for path " + path,
                secondResponse);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void testDualTokenAccessToSimilarApiUrlsDenial(String path, String method, String username) throws Exception {
        String anotherPath = findAnotherPath(method);
        if (path.equals(anotherPath)) {
            return;
        }
        String token = TokenRepository.getToken(username, method, path + "," + anotherPath);
        Response firstResponse = executeRootRequest(token, "/api" + path, method);
        Response secondResponse = executeRootRequest(token, "/api/" + anotherPath, method);
        assertUnauthorized("Dual Token for path " + path + " and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for /api request for path " + path,
                firstResponse);
        assertUnauthorized("Dual Token for path " + path + " and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for /api request for path " + anotherPath,
                secondResponse);
    }

    @Test(
            dataProvider = "users"
    )
    @Issue("6052")
    public void dualTokenAccessToUrlParamsDenialTest(String username) throws Exception {
        String firstPath = "/csv";
        String fistParamName = "config";
        String firstParamValue = "not_valid_config";
        String secondPath = "/command";
        String secondParamName = "commit";
        String secondParamValue = "not_valid_commit";
        String method = HttpMethod.POST;
        String token = TokenRepository.getToken(username, method, firstPath);
        Response firstResponse = executeApiRequestWithParams(token, firstPath, method, ImmutableMap.of(fistParamName, firstParamValue));
        Response secondResponse = executeApiRequestWithParams(token, secondPath, method, ImmutableMap.of(secondParamName, secondParamValue));

        assertUnauthorized("Authorisation not failed with not valid parameter on dual token: " + token + " Path: " + firstPath + " Method: " + method + "User: " + username,
                firstResponse);
        assertCode15("Dual Token for path " + firstPath + " and method " + method + "with params " + fistParamName + "=" + firstParamValue + " did not give code 15 in response for user " + username,
                firstResponse);
        assertUnauthorized("Authorisation not failed with not valid parameter on dual token: " + token + " Path: " + secondPath + " Method: " + method + "User: " + username,
                secondResponse);
        assertCode15("Dual Token for path " + secondPath + " and method " + method + "with params " + secondParamName + "=" + secondParamValue + " did not give code 15 in response for user " + username,
                secondResponse);
    }

    private void assertUnauthorized(String message, Response response) {
        assertEquals(message, Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    private void assertNotUnauthorized(String message, Response response) {
        assertTrue(message, Response.Status.UNAUTHORIZED.getStatusCode() != response.getStatus());
    }

    private void assertCode15(String message, Response response) {
        assertTrue(message, response.readEntity(String.class).contains("code 15"));
    }

    private String findAnotherPath(String method) {
        switch (method) {
            case (HttpMethod.GET):
                return "/entities";
            case (HttpMethod.POST):
                return "/csv";
            case (HttpMethod.DELETE):
                return "/entities/entity";
            case (HttpMethod.PUT):
                return "/entities/entity";
            case ("PATCH"):
                return "/entities/entity";
            default:
                throw new IllegalArgumentException(method + " is not a valid Http method");
        }
    }

    private Response executeApiRequest(String token, String path, String method) {
        RequestSenderWithBearerAuthorization sender = new RequestSenderWithBearerAuthorization(token);
        Response response;
        if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE)) {
            response = sender.executeApiRequest(path, method);
        } else {
            response = sender.executeApiRequest(path, method, Mocks.JSON_OBJECT);
        }
        response.bufferEntity();
        return response;
    }

    private Response executeRootRequest(String token, String path, String method) {
        RequestSenderWithBearerAuthorization sender = new RequestSenderWithBearerAuthorization(token);
        Response response;
        if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE)) {
            response = sender.executeRootRequest(path, method);
        } else {
            response = sender.executeRootRequest(path, method, Mocks.JSON_OBJECT);
        }
        response.bufferEntity();
        return response;
    }

    private Response executeApiRequestWithParams(String token, String path, String method, Map<String, Object> params) {
        RequestSenderWithBearerAuthorization sender = new RequestSenderWithBearerAuthorization(token);
        Response response;
        if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE)) {
            response = sender.executeApiRequest(path, Collections.EMPTY_MAP, params, Collections.EMPTY_MAP, method);
        } else {
            response = sender.executeApiRequest(path, Collections.EMPTY_MAP, params, Collections.EMPTY_MAP, method, Mocks.JSON_OBJECT);
        }
        response.bufferEntity();
        return response;
    }
}
