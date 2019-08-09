package com.axibase.tsd.api.method.tokens;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;


import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.qameta.allure.Issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.*;


public class TokenAccessTest extends BaseMethod {
    private static final String USER_NAME = "apitokenuser_accesstest";
    private static final String ADMIN_NAME = Config.getInstance().getLogin();
    private static final String API_PATH = Config.getInstance().getApiPath();

    //TODO use request senders, separate into two classes - for data API and meta API

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
                {ADMIN_NAME},
                {USER_NAME}
        };
    }

    @DataProvider
    private Object[][] userAndPathAndMethod() {
        List<Object[]> pathsAndMethods = Arrays.asList(availablePaths());
        List<Object[]> result = new ArrayList<>();

        for (Object[] arr : pathsAndMethods) {
            result.add(ArrayUtils.addAll(arr, new String[]{ADMIN_NAME}));
            result.add(ArrayUtils.addAll(arr, new String[]{USER_NAME}));
        }

        return result.toArray(new Object[pathsAndMethods.size()][3]);
    }

    @BeforeClass
    private void createUser() {
        String password = RandomStringUtils.random(10, true, true);
        String path = "/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("enabled", "on")
                .queryParam("userBean.username", USER_NAME)
                .queryParam("userBean.password", password)
                .queryParam("repeatPassword", password)
                .queryParam("save", "Save")
                .queryParam("userBean.userRoles", "ROLE_API_DATA_WRITE")
                .queryParam("userBean.userRoles", "ROLE_API_META_WRITE")
                .queryParam("userBean.userRoles", "ROLE_USER")
                .queryParam("userBean.userRoles", "ROLE_ENTITY_GROUP_ADMIN")
                .queryParam("userBean.userGroups", "Users")
                .queryParam("create", "true")
                .request()
                .method(HttpMethod.POST))
                .bufferEntity();
    }


    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void tokenAccessToItsUrlTest(String path, String method, String username) throws Exception {
        String token = TokenRepository.getToken(username, method, path);
        Response response;
        if (method.equals(HttpMethod.DELETE) || method.equals(HttpMethod.GET)) {
            response = executeMethodWithoutEntity(token, path, method);
        } else {
            response = executeMethodWithEntity(token, path, method);
        }
        assertTrue("Token for path " + path + " and method " + method + " failed to authorise user " + username + " token: " + token + " response: " + response.readEntity(String.class), response.getStatus() != 401);
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void tokenAccessToOtherUrlsTest(String path, String method, String username) throws Exception {
        String availablePath = "/csv";
        String availableMethod = HttpMethod.POST;
        String token = TokenRepository.getToken(username, availableMethod, availablePath);
        Response response;
        if (method.equals(HttpMethod.DELETE) || method.equals(HttpMethod.GET)) {
            response = executeMethodWithoutEntity(token, path, method);
        } else {
            response = executeMethodWithEntity(token, path, method);
        }
        if (!(path.equals(availablePath) && method.equals(availableMethod))) {
            String entity = response.readEntity(String.class);
            assertEquals("Token for path " + path + " and method " + method + " did not fail to authorise user " + username + " Response: " + entity, 401, response.getStatus());
            assertTrue("Token for path " + path + " and method " + method + " did not give code 15 in response for user " + username + "actual Response: " + entity, entity.contains("code 15"));
        }

    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void tokenAccessToSimilarRootUrlsDenialTest(String path, String method, String username) throws Exception {
        String token = TokenRepository.getToken(username, method, path);
        Response responseRoot; //response from root request
        Response responseAPI; //response from /api/path
        if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE)) {
            responseRoot = executeRootTokenMethodWithoutEntity(token, path, method);
            responseAPI = executeMethodWithoutEntity(token, "/api" + path, method);
        } else {
            responseRoot = executeRootTokenMethodWithEntity(token, path, method);
            responseAPI = executeMethodWithEntity(token, "/api" + path, method);
        }
        assertEquals("Token for path " + path + " and method " + method + " did not fail to authorise user " + username + " for root request.", 401, responseRoot.getStatus());
        assertEquals("Token for path " + path + " and method " + method + " did not fail to authorise user " + username + " for /api request.", 401, responseAPI.getStatus());
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
        Response response = executeParamsWithEntity(token, path, method, paramName, paramValue);

        String entity = response.readEntity(String.class);
        assertEquals("Authorisation not failed with not valid parameter on token: " + token + " Path: " + path + " Method: " + method + "User: " + username + "Response: " + entity, 401, response.getStatus());
        assertTrue("Token for path " + path + " and method " + method + "with params " + paramName + "=" + paramValue + " did not give code 15 in response for user " + username + " Response: " + entity, entity.contains("code 15"));
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void dualTokenAccessToItsUrlsTest(String path, String method, String username) throws Exception {
        String anotherPath;
        switch (method) {
            case (HttpMethod.GET):
                anotherPath = "/entities";
                break;
            case (HttpMethod.POST):
                anotherPath = "/csv";
                break;
            case (HttpMethod.DELETE):
                anotherPath = "/entities/entity";
                break;
            case (HttpMethod.PUT):
                anotherPath = "/entities/entity";
                break;
            case ("PATCH"):
                anotherPath = "/entities/entity";
                break;
            default:
                throw new RuntimeException(method + " is not a valid Http method");
        }
        if (path.equals(anotherPath)) {
            return;
        }
        String token = TokenRepository.getToken(username, method, path + "," + anotherPath);
        Response firstResponse;
        Response secondResponse;
        if (method.equals(HttpMethod.DELETE) || method.equals(HttpMethod.GET)) {
            firstResponse = executeMethodWithoutEntity(token, path, method);
            secondResponse = executeMethodWithoutEntity(token, anotherPath, method);
        } else {
            firstResponse = executeMethodWithEntity(token, path, method);
            secondResponse = executeMethodWithEntity(token, anotherPath, method);
        }

        assertTrue("Dual token for urls " + path + "," + anotherPath + " failed to grand access to " + path + " for user " + username, 401 != firstResponse.getStatus());
        assertTrue("Dual token for urls " + path + "," + anotherPath + " failed to grand access to " + anotherPath + " for user " + username, 401 != secondResponse.getStatus());

    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void dualTokenAccessToOtherUrlsTest(String path, String method, String username) throws Exception {
        String firstPath = "/csv";
        String secondPath = "/command";
        String availableMethod = HttpMethod.POST;
        String token = TokenRepository.getToken(username, availableMethod, firstPath + "," + secondPath);
        Response response;
        if (method.equals(HttpMethod.DELETE) || method.equals(HttpMethod.GET)) {
            response = executeMethodWithoutEntity(token, path, method);
        } else {
            response = executeMethodWithEntity(token, path, method);
        }
        if (!((path.equals(firstPath) || path.equals(secondPath)) && method.equals(availableMethod))) {
            String entity = response.readEntity(String.class);
            assertEquals("Dual token for urls " + firstPath + "," + secondPath + " and method " + method + " did not fail to authorise user " + username + " to path " + path + " Response: " + entity, 401, response.getStatus());
            assertTrue("Dual Token for path " + path + " and method " + method + " did not give code 15 in response for user " + username + " Response: " + entity, entity.contains("code 15"));
        }
    }

    @Test(
            dataProvider = "userAndPathAndMethod"
    )
    @Issue("6052")
    public void dualTokenAccessToSimilarRootUrlsDenialTest(String path, String method, String username) throws Exception {
        String anotherPath;
        switch (method) {
            case (HttpMethod.GET):
                anotherPath = "/entities";
                break;
            case (HttpMethod.POST):
                anotherPath = "/csv";
                break;
            case (HttpMethod.DELETE):
                anotherPath = "/entities/entity";
                break;
            case (HttpMethod.PUT):
                anotherPath = "/entities/entity";
                break;
            case ("PATCH"):
                anotherPath = "/entities/entity";
                break;
            default:
                throw new RuntimeException(method + " is not a valid Http method");
        }
        if (path.equals(anotherPath)) {
            return;
        }
        String token = TokenRepository.getToken(username, method, path + "," + anotherPath);
        Response firstResponseRoot;
        Response secondResponseRoot;
        Response firstResponseAPI;
        Response secondResponseAPI;

        if (method.equals(HttpMethod.DELETE) || method.equals(HttpMethod.GET)) {
            firstResponseRoot = executeRootTokenMethodWithoutEntity(token, path, method);
            firstResponseAPI = executeRootTokenMethodWithoutEntity(token, "/api" + path, method);
            secondResponseRoot = executeRootTokenMethodWithoutEntity(token, path, method);
            secondResponseAPI = executeRootTokenMethodWithoutEntity(token, "/api" + path, method);
        } else {
            firstResponseRoot = executeRootTokenMethodWithEntity(token, path, method);
            firstResponseAPI = executeRootTokenMethodWithEntity(token, "/api" + path, method);
            secondResponseRoot = executeRootTokenMethodWithEntity(token, path, method);
            secondResponseAPI = executeRootTokenMethodWithEntity(token, "/api" + path, method);
        }

        assertEquals("Dual Token for path " + path + "and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for root request for path " + path, 401, firstResponseRoot.getStatus());
        assertEquals("Dual Token for path " + path + " and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for /api request for path " + path, 401, firstResponseAPI.getStatus());

        assertEquals("Dual Token for path " + path + "and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for root request for path " + anotherPath, 401, secondResponseRoot.getStatus());
        assertEquals("Dual Token for path " + path + " and " + anotherPath + " and method " + method + " did not fail to authorise user " + username + " for /api request for path " + anotherPath, 401, secondResponseAPI.getStatus());
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
        Response firstResponse = executeParamsWithEntity(token, firstPath, method, fistParamName, firstParamValue);
        Response secondResponse = executeParamsWithEntity(token, secondPath, method, secondParamName, secondParamValue);

        assertEquals("Authorisation not failed with not valid parameter on dual token: " + token + " Path: " + firstPath + " Method: " + method + "User: " + username, 401, firstResponse.getStatus());
        String entity = firstResponse.readEntity(String.class);
        assertTrue("Dual Token for path " + firstPath + " and method " + method + "with params " + fistParamName + "=" + firstParamValue + " did not give code 15 in response for user " + username, entity.contains("code 15"));

        assertEquals("Authorisation not failed with not valid parameter on dual token: " + token + " Path: " + secondPath + " Method: " + method + "User: " + username, 401, secondResponse.getStatus());
        entity = secondResponse.readEntity(String.class);
        assertTrue("Dual Token for path " + secondPath + " and method " + method + "with params " + secondParamName + "=" + secondParamValue + " did not give code 15 in response for user " + username, entity.contains("code 15"));
    }

    private Response executeParamsWithEntity(String token, String path, String method, String paramName, String paramValue) {
        final Response response = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + path)
                .queryParam(paramName, paramValue)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method(method, Entity.json("entity")));
        response.bufferEntity();
        return response;
    }

    private Response executeRootTokenMethodWithoutEntity(String token, String path, String method) {
        final Response response = executeTokenRootRequest(webTarget -> webTarget.path(path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method(method));
        response.bufferEntity();
        return response;
    }

    private Response executeRootTokenMethodWithEntity(String token, String path, String method) {
        final Response response = executeTokenRootRequest(webTarget -> webTarget.path(path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method(method, Entity.json("entity")));
        response.bufferEntity();
        return response;
    }

    private Response executeMethodWithoutEntity(String token, String path, String method) {
        final Response response = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method(method));
        response.bufferEntity();
        return response;
    }

    private Response executeMethodWithEntity(String token, String path, String method) {
        final Response response = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method(method, Entity.json("entity")));
        response.bufferEntity();
        return response;
    }

}
