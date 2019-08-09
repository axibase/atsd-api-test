package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.alert.AlertTest;
import com.axibase.tsd.api.method.checks.*;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.method.replacementtable.ReplacementTableMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;
import com.axibase.tsd.api.model.alert.AlertQuery;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.model.replacementtable.SupportedFormat;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.*;


import static com.axibase.tsd.api.util.Util.*;
import static org.testng.AssertJUnit.*;

public class UserCreator extends BaseMethod {
    private static final String USER_NAME = "apitokenuser_worktest";
    private static final String ADMIN_NAME = Config.getInstance().getLogin();
    public static final String API_PATH = Config.getInstance().getApiPath();

    static {
        createUser();
    }

    @DataProvider
    public static Object[][] users() {
        return new String[][]{
                {ADMIN_NAME},
                {USER_NAME}
        };
    }

    public static void createUser() {
        String path = "/admin/users/edit.xhtml";
        String userPassword = RandomStringUtils.random(10, true, true);

        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("enabled", "on")
                .queryParam("userBean.username", USER_NAME)
                .queryParam("userBean.password", userPassword)
                .queryParam("repeatPassword", userPassword)
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

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void dualTokenTest(String username) throws Exception {
        String entityName = Mocks.entity();
        com.axibase.tsd.api.model.entity.Entity entity = new com.axibase.tsd.api.model.entity.Entity(entityName);
        entity.setEnabled(true);
        String metricName = Mocks.metric();
        Metric metric = new Metric(metricName);
        metric.setEnabled(true);
        String firstURL = "/entities/" + entityName;
        String secondURL = "/metrics/" + metricName;
        String method = HttpMethod.PUT;
        String token = TokenRepository.getToken(username, method, firstURL + "\n" + secondURL);
        //checking first url work
        createOrReplace(username, firstURL, entity, token);
        Checker.check(new EntityCheck(entity));
        Response firstResponse = EntityMethod.getEntityResponse(entityName);
        assertTrue("Entity was not inserted with dual token for user " + username, !firstResponse.readEntity(String.class).contains("error"));
        //checking second url work
        createOrReplace(username, secondURL, metric, token);
        Checker.check(new MetricCheck(metric));
        Response secondResponse = executeApiRequest(webTarget -> webTarget.path(secondURL)
                .request()
                .method(HttpMethod.GET));
        secondResponse.bufferEntity();
        assertTrue("Metric was not inserted with dual token for user " + username, !secondResponse.readEntity(String.class).contains("error"));
    }

    public static Response insert(String username, String insertURL, Object insertData, String insertToken) {
        Response responseWithToken = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + insertURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + insertToken)
                .method(HttpMethod.POST, Entity.json(insertData)));
        responseWithToken.bufferEntity();
        assertEquals("User: " + username, Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseWithToken));
        return responseWithToken;
    }

    public static Response get(String getURL, String getToken) {
        Response responseWithToken = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + getURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method(HttpMethod.GET));
        responseWithToken.bufferEntity();
        return responseWithToken;
    }

    public static Response query(String queryURL, Object query, String queryToken) {
        Response responseWithToken = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + queryToken)
                .method(HttpMethod.POST, Entity.json(query)));
        responseWithToken.bufferEntity();
        return responseWithToken;
    }

    public static Response createOrReplace(String username, String url, Object data, String token) {
        Response responseWithToken = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + url)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method(HttpMethod.PUT, Entity.json(data)));
        responseWithToken.bufferEntity();
        assertEquals("User: " + username, Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseWithToken));
        return responseWithToken;
    }

    public static Response update(String username, String url, Object data, String token) {
        Response responseWithToken = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + url)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method("PATCH", Entity.json(data)));
        responseWithToken.bufferEntity();
        assertEquals("User: " + username, Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseWithToken));
        return responseWithToken;
    }

}
