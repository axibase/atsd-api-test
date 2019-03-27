package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class TokenWorkTest extends BaseMethod {
    private static final String userName;
    private static final String userPassword;
    private static final String adminName;
    private static final String adminPassword;

    static {
        userName = "APITokenUser";
        userPassword = RandomStringUtils.random(10, true, true);
        try {
            Config config = Config.getInstance();
            adminName = config.getLogin();
            adminPassword = config.getPassword();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    @BeforeClass
    private void createUser() {
        String path ="/admin/users/edit.xhtml";

        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("enabled", "on")
                .queryParam("userBean.username", userName)
                .queryParam("userBean.password", userPassword)
                .queryParam("repeatPassword", userPassword)
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
    public void tokenSeriesTestForUser() throws Exception {
        String entity = "token_test_user_entity";
        String metric = "token_test_user_metric";
        Response responseToken;
        Response responseAPI;
        long currentUnixTime = System.currentTimeMillis();
        int value = 22;
        String insertURL = "/series/insert";
        String insertToken = TokenRepository.getToken(userName, "POST", insertURL);
        String insertData = "[{\"entity\": \"" + entity + "\", \"metric\": \"" + metric + "\", \"data\": " +
                "[{ \"t\": " + currentUnixTime + ", \"v\": " + value + " }]}]";
        List<Series> seriesList = new ArrayList<>();
        Series series = new Series(entity, metric);
        Sample sample = Sample.ofTimeInteger(currentUnixTime, value);
        series.addSamples(sample);
        seriesList.add(series);
        responseToken = executeTokenRequest(webTarget -> webTarget.path(insertURL)
                                    .request()
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + insertToken)
                                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .method("POST", Entity.json(seriesList)));
        assertEquals(Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseToken));

        String getURL = "/series/csv/" + entity +"/" + metric;
        responseAPI = executeApiRequest(webTarget -> webTarget.path(getURL)
                                                .queryParam("startDate", "previous_hour")
                                                .queryParam("endDate", "next_day")
                                                .request()
                                                .method("GET"));
        responseAPI.bufferEntity();
        String getToken = TokenRepository.getToken(userName, "GET", getURL + "?startDate=previous_hour&endDate=next_day");
        responseToken = executeTokenRequest(webTarget -> webTarget.path(getURL)
                .queryParam("startDate", "previous_hour")
                .queryParam("endDate", "next_day")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method("GET"));
        responseToken.bufferEntity();
        assertTrue(!responseAPI.readEntity(String.class).equals("time,entity,metric,value"));
        assertEquals(responseToken.readEntity(String.class), responseAPI.readEntity(String.class));
    }

   @AfterClass
    private void deleteUser() {
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("userBean.username", userName)
                .queryParam("delete", "Delete")
                .request()
                .method("POST"));
    }
}
