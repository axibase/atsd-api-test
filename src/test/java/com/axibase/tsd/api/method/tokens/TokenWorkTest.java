package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.alert.AlertMethod;
import com.axibase.tsd.api.method.alert.AlertTest;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;
import com.axibase.tsd.api.model.alert.AlertQuery;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.model.message.MessageStatsQuery;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.*;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class TokenWorkTest extends BaseMethod {
    private static final String USER_NAME;
    private static final String USER_PASSWORD;
    private static final String ADMIN_NAME;

    private Response responseToken;
    private Response responseAPI;
    private String responseAPIEntity;
    private String responseTokenEntity;

    static {
        USER_NAME = "APITokenUser";
        USER_PASSWORD = RandomStringUtils.random(10, true, true);
        try {
            Config config = Config.getInstance();
            ADMIN_NAME = config.getLogin();
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
                .queryParam("userBean.username", USER_NAME)
                .queryParam("userBean.password", USER_PASSWORD)
                .queryParam("repeatPassword", USER_PASSWORD)
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
    public void tokenSeriesTest() throws  Exception {
        tokenSeriesTestForUser(ADMIN_NAME);
        tokenSeriesTestForUser(USER_NAME);
    }

    @Issue("6052")
    @Test
    public void tokenPropertiesTest() throws Exception {
        tokenPropertiesTestForUser(ADMIN_NAME);
        tokenPropertiesTestForUser(USER_NAME);
    }

    @Issue("6052")
    @Test
    public void tokenMessagesTest() throws Exception {
        tokenMessagesTestForUser(ADMIN_NAME);
        tokenMessagesTestForUser(USER_NAME);
    }

    @Issue("6052")
    @Test
    public void tokenAlertsTest() throws Exception {
        tokenAlertsTestForUser(ADMIN_NAME);
        tokenAlertsTestForUser(USER_NAME);
    }

    private void tokenSeriesTestForUser(String username) throws Exception {
        String entity = "token_test_series_" + username + "_entity";
        String metric = "token_test_series_" + username + "_metric";
        long startUnixTime = System.currentTimeMillis();
        int value = 22;

        String insertURL = "/series/insert";
        String insertToken = TokenRepository.getToken(username, "POST", insertURL);
        List<Series> seriesList = new ArrayList<>();
        Series series = new Series(entity, metric);
        Sample sample = Sample.ofTimeInteger(startUnixTime, value);
        series.addSamples(sample);
        seriesList.add(series);
        insert(username,insertURL,seriesList,insertToken);
        //checking get method
        Thread.sleep(500); //getting timeout for ATSD to insert series
        String getURL = "/series/json/" + entity +"/" + metric;
        String getToken = TokenRepository.getToken(username, "GET", getURL + "?startDate=previous_hour&endDate=next_day");
        get(getURL, getToken);
        assertTrue("User: " + username + " Response contains warning: " + responseAPIEntity,!(responseAPIEntity.contains("warning")));
        assertEquals("User: " + username + " token series get response does not equal api series get response", responseAPIEntity , responseTokenEntity);
        //checking queries
        String queryURL = "/series/query";
        SeriesQuery q = new SeriesQuery(entity, metric, startUnixTime, System.currentTimeMillis());
        String queryToken = TokenRepository.getToken(username, "POST", queryURL);
        List<SeriesQuery> query = new ArrayList<>();
        query.add(q);
        query(queryURL,query,queryToken);
        assertTrue("User: " + username + " Response contains warning: " + responseAPIEntity,!(responseAPIEntity.contains("warning")));
        assertEquals("User: " + username + " token series query response does not equal api series query response", responseAPIEntity , responseTokenEntity);
        //checking delete method
        String deleteURL = "/series/delete";
        SeriesQuery delete = new SeriesQuery(entity, metric);
        delete.setExactMatch(false);
        List<SeriesQuery> deleteQuery = new ArrayList<>();
        deleteQuery.add(delete);
        String deleteToken = TokenRepository.getToken(username, "POST", deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                                                .request()
                                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                                                .method("POST", Entity.json(deleteQuery)));
        //checking that series was successfully deleted
        responseAPI = executeApiRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .method("POST", Entity.json(query)));
        responseAPI.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        assertTrue("User: " + username + " Response does not contain warning after delete: " + responseAPIEntity,responseAPIEntity.contains("warning"));

    }

    private void tokenPropertiesTestForUser(String username) throws Exception {
        String entity = "token_test_properties_" + username + "_entity";
        String type = "token_test_properties_" + username + "_type";
        String tagName = "name";
        String tagValue = "value";
        long startUnixTime = System.currentTimeMillis();
        Property property = new Property(type, entity);
        property.addTag(tagName, tagValue);
        property.setDate(startUnixTime);
        List<Property> propertyList = new ArrayList<>();
        propertyList.add(property);

        String insertURL = "/properties/insert";
        String insertToken = TokenRepository.getToken(username, "POST", insertURL);
        insert(username, insertURL, propertyList, insertToken);
        //checking get method
        Thread.sleep(500); //getting timeout for ATSD to insert property
        String getURL = "/properties/" + entity + "/types/" + type;
        String getToken = TokenRepository.getToken(username, "GET", getURL);
        get(getURL, getToken);
        assertTrue("User: " + username + " Properties get response gives empty output after token insertion", !(responseAPIEntity.equals("[]")));
        assertEquals("User: " + username + " Properties get response with token and API methods give different outputs", responseAPIEntity, responseTokenEntity);
        //checking properties get types request
        String getTypesURL = "/properties/" + entity + "/types";
        String getTypesToken = TokenRepository.getToken(username, "GET", getTypesURL);
        get(getTypesURL, getTypesToken);
        assertTrue("User: " + username + " Properties get types response gives empty output after token insertion", !(responseAPIEntity.equals("[]")));
        assertEquals("User: " + username + " Properties get types response with token and API methods give different outputs", responseAPIEntity, responseTokenEntity);
        //checking properties queries
        String queryURL = "/properties/query";
        String queryToken = TokenRepository.getToken(username, "POST", queryURL);
        PropertyQuery q = new PropertyQuery(type, entity);
        q.setStartDate(Util.ISOFormat(startUnixTime-10));
        q.setEndDate(Util.ISOFormat( System.currentTimeMillis()));
        List<PropertyQuery> query = new ArrayList<>();
        query.add(q);
        query(queryURL,query,queryToken);
        assertTrue("User: " + username + " Response contains warning: " + responseAPIEntity,!(responseAPIEntity.equals("[]")));
        assertEquals("User: " + username + " token property query response does not equal api property query response", responseAPIEntity , responseTokenEntity);
        //checking delete method
        String deleteURL = "/properties/delete";
        String deleteToken = TokenRepository.getToken(username, "POST", deleteURL);
        PropertyQuery delete = new PropertyQuery(type, entity);
        delete.setStartDate(Util.ISOFormat(startUnixTime));
        delete.setEndDate(Util.ISOFormat(System.currentTimeMillis()));
        List<PropertyQuery> deleteQuery = new ArrayList<>();
        deleteQuery.add(delete);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method("POST", Entity.json(deleteQuery)));
        //checking that series was successfully deleted
        responseAPI = executeApiRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .method("POST", Entity.json(query)));
        responseAPI.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        assertTrue("User: " + username + " Property was not deleted with token response. Response body: " + responseAPIEntity, responseAPIEntity.equals("[]"));
    }

    private void tokenMessagesTestForUser(String username) throws Exception {
        String entity = "token_test_messages_" +username + "_entity";
        String type = "logger";
        long startUnixTime = System.currentTimeMillis();
        Message message = new Message(entity, type);
        message.setDate(Util.ISOFormat(startUnixTime));
        List<Message> messageList = new ArrayList<>();
        messageList.add(message);

        String insertURL = "/messages/insert";
        String insertToken = TokenRepository.getToken(username, "POST", insertURL);
        insert(username,insertURL, messageList, insertToken);
        //check message query
        Thread.sleep(500); //getting timeout for ATSD to insert message
        String queryURL = "/messages/query";
        String queryToken = TokenRepository.getToken(username, "POST", queryURL);
        MessageQuery q = new MessageQuery();
        q.setEntity(entity).setStartDate(Util.ISOFormat(startUnixTime-10)).setEndDate(Util.ISOFormat(System.currentTimeMillis())).setType(type);
        List<MessageQuery> query = new ArrayList<>();
        query.add(q);
        query(queryURL, query, queryToken);
        assertTrue("User: " + username + " Response contains warning: " + responseAPIEntity,!(responseAPIEntity.contains("error")));
        assertEquals("User: " + username + " token message query response does not equal api message query response", responseAPIEntity , responseTokenEntity);
        //check message count query
        String countURL = "/messages/stats/query";
        String countToken = TokenRepository.getToken(username, "POST", countURL);
        MessageStatsQuery msq = new MessageStatsQuery();
        msq.setEntity(entity);
        msq.setType(type);
        msq.setStartDate(Util.ISOFormat(startUnixTime-10));
        msq.setEndDate(Util.ISOFormat((System.currentTimeMillis())));
        List<MessageStatsQuery> messageStatsQueryList = new ArrayList<>();
        messageStatsQueryList.add(msq);
        query(countURL, messageStatsQueryList, countToken);
        assertEquals("User: " + username + " Token message count response entity does not equal API message count response entity", responseAPIEntity, responseTokenEntity);
    }

    private void tokenAlertsTestForUser(String username) throws Exception {
        String entity = "token_test_alerts_" + username + "_entity";
        String metric = AlertTest.RULE_METRIC_NAME;
        AlertTest.generateAlertForEntity(entity); //generating alert in ATSD
        //checking alerts query method
        String queryURL = "/alerts/query";
        String queryToken = TokenRepository.getToken(username, "POST", queryURL);
        AlertQuery q = new AlertQuery(entity);
        q.setStartDate(MIN_QUERYABLE_DATE);
        q.setEndDate(MAX_QUERYABLE_DATE);
        q.addMetric(metric);
        List<AlertQuery> query = new ArrayList<>();
        query.add(q);
        query(queryURL, query, queryToken);
        assertEquals("User: " + username + " Token Alert query response does not equal API Alert query response", responseAPIEntity, responseAPIEntity);
        //reading alert data from entity
        List<LinkedHashMap> alertList = responseToken.readEntity(List.class);
        LinkedHashMap alert = alertList.get(0);
        Integer id = (Integer)alert.get("id");
        Boolean acknowledged = (Boolean)alert.get("acknowledged");
        //check history query
        String historyQueryURL = "/alerts/history/query";
        String historyQueryToken = TokenRepository.getToken(username, "POST", historyQueryURL);
        AlertHistoryQuery ahq = new AlertHistoryQuery();
        ahq.setEntity(entity);
        ahq.setStartDate(MIN_QUERYABLE_DATE);
        ahq.setEndDate(MAX_QUERYABLE_DATE);
        List<AlertHistoryQuery> alertHistoryQueryList = new ArrayList<>();
        alertHistoryQueryList.add(ahq);
        query(historyQueryURL, alertHistoryQueryList, historyQueryToken);
        assertEquals("User: " + username + " Token alert history query response does not equal API alert history query response", responseAPIEntity, responseTokenEntity);
        //TODO delete method
    }


    private void insert(String username, String insertURL, Object insertData, String insertToken) {
        responseToken = executeTokenRequest(webTarget -> webTarget.path(insertURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + insertToken)
                .method("POST", Entity.json(insertData)));
        assertEquals("User: " + username,Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseToken));
    }

    private void get(String getURL, String getToken) {
        responseAPI = executeApiRequest(webTarget -> webTarget.path(getURL)
                .queryParam("startDate", "previous_hour")
                .queryParam("endDate", "next_day")
                .request()
                .method("GET"));
        responseAPI.bufferEntity();
        responseToken = executeTokenRequest(webTarget -> webTarget.path(getURL)
                .queryParam("startDate", "previous_hour")
                .queryParam("endDate", "next_day")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method("GET"));
        responseToken.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        responseTokenEntity = responseToken.readEntity(String.class);
    }

    private void query(String queryURL, Object query, String queryToken) {
        responseAPI = executeApiRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .method("POST", Entity.json(query)));
        responseAPI.bufferEntity();
        responseToken = executeTokenRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + queryToken)
                .method("POST", Entity.json(query)));
        responseToken.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        responseTokenEntity = responseToken.readEntity(String.class);
    }


   @AfterClass
    private void deleteUser() {
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("userBean.username", USER_NAME)
                .queryParam("delete", "Delete")
                .request()
                .method("POST"));
    }
}
