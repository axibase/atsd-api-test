package com.axibase.tsd.api.method.tokens;

import static org.testng.AssertJUnit.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import com.axibase.tsd.api.method.BaseMethod;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.qameta.allure.Issue;


public class TokenAuthenticationTest extends BaseMethod {

    private static final String[][] availablePaths = new String[][]{
        // Data API
        {"/series/query", "POST"},
        {"/series/insert", "POST"},
        {"/series/csv/entity", "POST"},
        {"/series/format/entity/metric", "GET"},
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

    @BeforeClass
    private void createUser()
    {
        String username="username";
        String password="password";
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                                        .queryParam("enabled", "on")
                                        .queryParam("userBean.username", username)
                                        .queryParam("userBean.password", password)
                                        .queryParam("repeatPassword", password)
                                        .queryParam("save", "Save")
                                        .queryParam("userBean.userRoles","ROLE_API_DATA_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_API_META_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_USER")
                                        .queryParam("create", "true")
                                        .request()
                                        .method("POST"));
    }

    @Issue("6052")
    @Test
    public void tokenAccessTest() throws Exception {
        String username = "username";
        for (String[] pathAndMethod : availablePaths) {
            String availablePath = pathAndMethod[0];
            String availableMethod = pathAndMethod[1];
            String token = TokenRepository.getToken(username, availableMethod, availablePath);
            for (String[] testingPathAndMethod : availablePaths) {
                String testingPath = testingPathAndMethod[0];
                String testingMethod = testingPathAndMethod[1];
                Response response = null;
                if(testingMethod.equals("GET") || testingMethod.equals("DELETE"))
                {
                    response = executeMethodWithoutEntity(token, testingPath, testingMethod);
                }
                else{
                    response = executeMethodWithEntity(token, testingPath, testingMethod);
                }
                
                if(!(testingPath.equals(availablePath) && testingMethod.equals(availableMethod)))
                {
                    assertEquals(testingMethod + " " + testingPath + " failed on token " + token, 401, response.getStatus());
                    final String responseBody = response.readEntity(String.class);
                    assertEquals("Wrong response code: " + responseBody, responseBody.contains("code 15"), true);
                }
            }
        }
    }

    private Response executeMethodWithoutEntity(String token, String path, String method)
    {
        return executeTokenRequest(webTarget -> webTarget.path(path)
        .request()
        .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
        .method(method));
    }
    private Response executeMethodWithEntity(String token, String path, String method)
    {
        return executeTokenRequest(webTarget -> webTarget.path(path)
        .request()
        .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
        .method(method, Entity.json("entity")));
    }

    @AfterClass
    private void deleteUser()
    {
        String username="username";
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                                        .queryParam("userBean.username", username)
                                        .queryParam("delete", "Delete")
                                        .request()
                                        .method("POST"));
    }
}
