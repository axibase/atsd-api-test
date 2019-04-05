package com.axibase.tsd.api.method.tokens;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;


import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.qameta.allure.Issue;

import static org.testng.AssertJUnit.*;


public class TokenAccessTest extends BaseMethod {

    private static final String[][] availablePaths = new String[][]{
        // Data API
        {"/series/query", "POST"},
        {"/series/insert", "POST"},
        {"/series/csv/entity", "POST"},
        {"/series/json/entity/metric", "GET"},
        {"/series/csv/entity/metric", "GET"},
        {"/series/query", "POST"},
        {"/series/delete", "POST"},
        {"/properties/query", "POST"},
        {"/properties/insert", "POST"},
        {"/properties/delete", "POST"},
        {"/properties/entity/types/type", "GET"},
        {"/properties/entity/types", "GET"},
        {"/messages/query", "POST"},
        {"/messages/insert", "POST"},
        {"/messages/stats/query", "POST"},
        {"/alerts/query", "POST"},
        {"/alerts/update", "POST"},
        {"/alerts/delete", "POST"},
        {"/alerts/history/query", "POST"},
        {"/csv", "POST"},
        {"/nmon", "POST"},
        {"/command", "POST"},
        {"/export", "GET"},
        // Meta API
        {"/metrics", "GET"},
        {"/metrics/metric", "GET"},
        {"/metrics/metric", "PUT"},
        {"/metrics/metric", "PATCH"},
        {"/metrics/metric", "DELETE"},
        {"/metrics/metric/series", "GET"},
        {"/entities", "GET"},
        {"/entities", "POST"},
        {"/entities/entity", "GET"},
        {"/entities/entity", "PUT"},
        {"/entities/entity", "PATCH"},
        {"/entities/entity", "DELETE"},
        {"/entities/entity/groups", "GET"},
        {"/entities/entity/metrics", "GET"},
        {"/entities/entity/property-types", "GET"},
        {"/entity-groups", "GET"},
        {"/entity-groups/group", "GET"},
        {"/entity-groups/group", "PUT"},
        {"/entity-groups/group", "PATCH"},
        {"/entity-groups/group", "DELETE"},
        {"/entity-groups/group/entities", "GET"},
        {"/entity-groups/group/entities/add", "POST"},
        {"/entity-groups/group/entities/set", "POST"},
        {"/entity-groups/group/entities/delete", "POST"},
        {"/search", "GET"},
        {"/version", "GET"},
    };

    private static final String USER_NAME = "APITokenUser";

    @BeforeClass
    private void createUser() {
        String password= RandomStringUtils.random(10, true, true);
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                                        .queryParam("enabled", "on")
                                        .queryParam("userBean.username", USER_NAME)
                                        .queryParam("userBean.password", password)
                                        .queryParam("repeatPassword", password)
                                        .queryParam("save", "Save")
                                        .queryParam("userBean.userRoles","ROLE_API_DATA_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_API_META_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_USER")
                                        .queryParam("userBean.userGroups", "Users")
                                        .queryParam("create", "true")
                                        .request()
                                        .method("POST"));
    }

    @Issue("6052")
    @Test
    public void testTokenAccess() throws Exception {
        Config config = Config.getInstance();
        testTokenAccessForUser(config.getLogin());
        testTokenAccessForUser(USER_NAME);
    }

    @Issue("6052")
    @Test
    public  void testDualTokenAccess() throws Exception {
        Config config = Config.getInstance();
        testDualTokenAccessForUser(config.getLogin());
        testDualTokenAccessForUser(USER_NAME);
    }

    private void testTokenAccessForUser(String username) throws Exception {
        String availablePath="/csv";
        String availableMethod= HttpMethod.POST;
        String token = TokenRepository.getToken(username, availableMethod, availablePath);
        Response response;
        for(String[] testingPathAndMethod : availablePaths) {
            String testingPath = testingPathAndMethod[0];
            String testingMethod = testingPathAndMethod[1];
            if(testingMethod.equals(HttpMethod.GET) || testingMethod.equals(HttpMethod.DELETE)) {
                response = executeMethodWithoutEntity(token, testingPath, testingMethod);
            }
            else {
                response = executeMethodWithEntity(token, testingPath, testingMethod);
            }
            if(testingPath.equals(availablePath) && testingMethod.equals(availableMethod)) {
                assertTrue("Authorisation failed on token " + token + " User: " +username, response.getStatus() != 401);
                continue;
            }
            assertEquals("Authorisation not failed on token: " + token +" Path: " + testingPath + " Method: "+ testingMethod + " User: "+ username, 401, response.getStatus());
            String entity = response.readEntity(String.class);
            assertTrue("Error code expected to be 15. Token: " + token + " Path: " + testingPath + " Method: "+ testingMethod + " User: " + username + " Actual respose: " + entity, entity.contains("code 15"));
        }
        //testing that token can not get access to url with another parameters
        response = executeTokenRequest(webTarget -> webTarget.path(availablePath)
                .queryParam("config", "not_valid_config")
                .request()
                .method(availableMethod));
        assertEquals("Authorisation not failed with not valid parameter on token: " + token +" Path: " + availablePath + " Method: "+ availableMethod + "User: " +username, 401, response.getStatus());
        String entity = response.readEntity(String.class);
        assertTrue("Error code expected to be 15 with not valid parameter. Token: " + token + " Path: " + availablePath + " Method: "+ availableMethod + "User: " + username + " Actual respose: " + entity, entity.contains("code 15"));

    }

    private void testDualTokenAccessForUser(String username) throws Exception {
        String firstAvailablePath = "/csv";
        String secondAvailablePath = "/command";
        String availableMethod = HttpMethod.POST;
        String token = TokenRepository.getToken(username, availableMethod, firstAvailablePath + "\n" + secondAvailablePath);
        Response response;
        for(String[] testingPathAndMethod : availablePaths){
            String testingPath = testingPathAndMethod[0];
            String testingMethod = testingPathAndMethod[1];
            if(testingMethod.equals(HttpMethod.GET) || testingMethod.equals(HttpMethod.DELETE)) {
                response = executeMethodWithoutEntity(token, testingPath, testingMethod);
            }
            else {
                response = executeMethodWithEntity(token, testingPath, testingMethod);
            }
            if(availableMethod.equals(testingMethod) && (firstAvailablePath.equals(testingPath) || secondAvailablePath.equals(testingPath))) {
                assertTrue("Authorisation failed on token " + token + " User: " +username + " Path: " + testingPath + " Method " + testingMethod, response.getStatus() != 401);
                continue;
            }
            assertEquals("Authorisation not failed on token: " + token +" Path: " + testingPath + " Method: "+ testingMethod + " User: "+ username, 401, response.getStatus());
            String entity = response.readEntity(String.class);
            assertTrue("Error code expected to be 15. Token: " + token + " Path: " + testingPath + " Method: "+ testingMethod + " User: " + username + " Actual respose: " + entity, entity.contains("code 15"));
        }
        response = executeTokenRequest(webTarget -> webTarget.path(firstAvailablePath)
                                                        .queryParam("config", "not_valid_config")
                                                        .request()
                                                        .method(availableMethod));
        assertEquals("Authorisation not failed with not valid parameter on token: " + token +" Path: " + firstAvailablePath + " Method: "+ availableMethod + "User: " +username, 401, response.getStatus());
        String entity = response.readEntity(String.class);
        assertTrue("Error code expected to be 15 with not valid parameter. Token: " + token + " Path: " + firstAvailablePath + " Method: "+ availableMethod + "User: " + username + " Actual respose: " + entity, entity.contains("code 15"));

        response = executeTokenRequest(webTarget -> webTarget.path(secondAvailablePath)
                .queryParam("commit", true)
                .request()
                .method(availableMethod));
        assertEquals("Authorisation not failed with not valid parameter on token: " + token +" Path: " + secondAvailablePath + " Method: "+ availableMethod + "User: " +username, 401, response.getStatus());
        entity = response.readEntity(String.class);
        assertTrue("Error code expected to be 15 with not valid parameter. Token: " + token + " Path: " + secondAvailablePath + " Method: "+ availableMethod + "User: " + username + " Actual respose: " + entity, entity.contains("code 15"));
    }

    private Response executeMethodWithoutEntity(String token, String path, String method) {
        final Response response = executeTokenRequest(webTarget -> webTarget.path(path)
        .request()
        .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
        .method(method));
        response.bufferEntity();
        return response;
    }
    private Response executeMethodWithEntity(String token, String path, String method) {
        final Response response = executeTokenRequest(webTarget -> webTarget.path(path)
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                        .method(method, Entity.json("entity")));
        response.bufferEntity();
        return response;
    }
    @AfterClass
    private void deleteUser() {
        String username="APITokenUser";
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                                        .queryParam("userBean.username", username)
                                        .queryParam("delete", "Delete")
                                        .request()
                                        .method("POST"));
    }
}
