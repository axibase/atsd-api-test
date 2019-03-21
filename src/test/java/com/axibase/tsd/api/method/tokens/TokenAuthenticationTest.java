package com.axibase.tsd.api.method.tokens;

import static org.testng.AssertJUnit.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import com.axibase.tsd.api.method.BaseMethod;

import com.axibase.tsd.api.Config;

import org.testng.annotations.Test;

import io.qameta.allure.Issue;


@Issue("6052")
@Test
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

    @Issue("6052")
    @Test
    public void tokenAccessTest() throws Exception {
        Config config = Config.getInstance();
        String apiPath = config.getApiPath();
        String username = "username";
        createUser(username, "password");
        for (String[] pathAndMethod : availablePaths) {
            String availablePath = pathAndMethod[0];
            String availableMethod = pathAndMethod[1];
            String token = TokenRepository.getToken(username, availableMethod, apiPath+availablePath);
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
                    assertEquals("HTTP return code expected to be 401, but it is " + response.getStatus(), 401, response.getStatus());
                    final String responseBody = response.readEntity(String.class);
                    assertEquals("Wrong response code: " + responseBody, responseBody.contains("code 15"), true);
                }
            }
        }
        deleteUser(username);
    }

    private Response executeMethodWithoutEntity(String token, String path, String method)
    {
        return executeApiRequest(webTarget -> webTarget.path(path)
        .request()
        .header("Authorization: Bearer", token)
        .method(method));
    }
    private Response executeMethodWithEntity(String token, String path, String method)
    {
        return executeApiRequest(webTarget -> webTarget.path(path)
        .request()
        .header("Authorization: Bearer", token)
        .method(method, Entity.json("entity")));
    }

    private void createUser(String username, String password)
    {
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                                        .queryParam("_enabled", "on")
                                        .queryParam("enabled", "on")
                                        .queryParam("userBean.username", username)
                                        .queryParam("userBean.firstName")
                                        .queryParam("userBean.lastName")
                                        .queryParam("userBean.email")
                                        .queryParam("userBean.password", password)
                                        .queryParam("repeatPassword", password)
                                        .queryParam("userBean.ipAddress")
                                        .queryParam("_userBean.ldap", "on")
                                        .queryParam("save", "Save")
                                        .queryParam("userBean.userRoles","ROLE_API_DATA_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_API_META_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_USER")
                                        .queryParam("_userBean.userRoles", "on")
                                        .queryParam("create", "true")
                                        .queryParam("current", "false")
                                        .request()
                                        .method("POST"));
    }

    private void deleteUser(String username)
    {
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                                        .queryParam("_enabled", "on")
                                        .queryParam("enabled", "on")
                                        .queryParam("userBean.username", username)
                                        .queryParam("_userBean.ldap", "on")
                                        .queryParam("delete", "Delete")
                                        .queryParam("userBean.userRoles","ROLE_API_DATA_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_API_META_WRITE")
                                        .queryParam("userBean.userRoles","ROLE_USER")
                                        .queryParam("_userBean.userRoles", "on")
                                        .queryParam("create", "false")
                                        .queryParam("current", "false")
                                        .request()
                                        .method("POST"));
    }
}
