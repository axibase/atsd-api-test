package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.alert.AlertTest;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;
import com.axibase.tsd.api.model.alert.AlertQuery;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.model.message.MessageStatsQuery;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.*;


import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.*;

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
                .queryParam("userBean.userRoles", "ROLE_ENTITY_GROUP_ADMIN")
                .queryParam("userBean.userGroups", "Users")
                .queryParam("create", "true")
                .request()
                .method(HttpMethod.POST));
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

    @Issue("6052")
    @Test
    public void tokenMetricTest() throws Exception {
        tokenMetricTestForUser(ADMIN_NAME);
        tokenMetricTestForUser(USER_NAME);
    }

    @Issue("6052")
    @Test
    public void tokenEntityTest() throws Exception {
        tokenEntityTestForUer(ADMIN_NAME);
        tokenEntityTestForUer(USER_NAME);
    }

    @Issue("6052")
    @Test
    public void tokenEntityGroupsTest() throws Exception {
        tokenEntityGroupsTestForUser(ADMIN_NAME);
        tokenEntityGroupsTestForUser(USER_NAME);
    }

    @Issue("6052")
    @Test
    public void tokenReplacementTablesTest() throws Exception {
        tokenReplacementTablesTestForUser(ADMIN_NAME);
        tokenReplacementTablesTestForUser(USER_NAME);
    }

    @Issue("6052")
    @Test
    public void dualTokenTest() throws Exception {
        dualTokenWorkTestForUser(ADMIN_NAME);
        dualTokenWorkTestForUser(USER_NAME);
    }

    private void tokenSeriesTestForUser(String username) throws Exception {
        String entity = "token_test_series_" + username + "_entity";
        String metric = "token_test_series_" + username + "_metric";
        long startUnixTime = System.currentTimeMillis();
        int value = 22;

        String insertURL = "/series/insert";
        String insertToken = TokenRepository.getToken(username, HttpMethod.POST, insertURL);
        List<Series> seriesList = new ArrayList<>();
        Series series = new Series(entity, metric);
        Sample sample = Sample.ofTimeInteger(startUnixTime, value);
        series.addSamples(sample);
        seriesList.add(series);
        insert(username,insertURL,seriesList,insertToken);
        //checking get method
        Thread.sleep(500); //getting timeout for ATSD to insert series
        String getURL = "/series/json/" + entity +"/" + metric;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL + "?startDate=previous_hour&endDate=next_day");
        responseAPI = executeApiRequest(webTarget -> webTarget.path(getURL)
                .queryParam("startDate", "previous_hour")
                .queryParam("endDate", "next_day")
                .request()
                .method(HttpMethod.GET));
        responseAPI.bufferEntity();
        responseToken = executeTokenRequest(webTarget -> webTarget.path(getURL)
                .queryParam("startDate", "previous_hour")
                .queryParam("endDate", "next_day")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method(HttpMethod.GET));
        responseToken.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        responseTokenEntity = responseToken.readEntity(String.class);
        assertTrue("User: " + username + " Response contains warning: " + responseAPIEntity,!(responseAPIEntity.contains("warning")));
        assertEquals("User: " + username + " token series get response does not equal api series get response", responseAPIEntity , responseTokenEntity);
        //checking queries
        String queryURL = "/series/query";
        SeriesQuery q = new SeriesQuery(entity, metric, startUnixTime, System.currentTimeMillis());
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
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
        String deleteToken = TokenRepository.getToken(username, HttpMethod.POST, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                                                .request()
                                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                                                .method(HttpMethod.POST, Entity.json(deleteQuery)));
        //checking that series was successfully deleted
        responseAPI = executeApiRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .method(HttpMethod.POST, Entity.json(query)));
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
        String insertToken = TokenRepository.getToken(username, HttpMethod.POST, insertURL);
        insert(username, insertURL, propertyList, insertToken);
        //checking get method
        Thread.sleep(500); //getting timeout for ATSD to insert property
        String getURL = "/properties/" + entity + "/types/" + type;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        get(getURL, getToken);
        assertTrue("User: " + username + " Properties get response gives empty output after token insertion", !(responseAPIEntity.equals("[]")));
        assertEquals("User: " + username + " Properties get response with token and API methods give different outputs", responseAPIEntity, responseTokenEntity);
        //checking properties get types request
        String getTypesURL = "/properties/" + entity + "/types";
        String getTypesToken = TokenRepository.getToken(username, HttpMethod.GET, getTypesURL);
        get(getTypesURL, getTypesToken);
        assertTrue("User: " + username + " Properties get types response gives empty output after token insertion", !(responseAPIEntity.equals("[]")));
        assertEquals("User: " + username + " Properties get types response with token and API methods give different outputs", responseAPIEntity, responseTokenEntity);
        //checking properties queries
        String queryURL = "/properties/query";
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
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
        String deleteToken = TokenRepository.getToken(username, HttpMethod.POST, deleteURL);
        PropertyQuery delete = new PropertyQuery(type, entity);
        delete.setStartDate(Util.ISOFormat(startUnixTime));
        delete.setEndDate(Util.ISOFormat(System.currentTimeMillis()));
        List<PropertyQuery> deleteQuery = new ArrayList<>();
        deleteQuery.add(delete);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method(HttpMethod.POST, Entity.json(deleteQuery)));
        //checking that series was successfully deleted
        responseAPI = executeApiRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .method(HttpMethod.POST, Entity.json(query)));
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
        String insertToken = TokenRepository.getToken(username, HttpMethod.POST, insertURL);
        insert(username,insertURL, messageList, insertToken);
        //check message query
        Thread.sleep(500); //getting timeout for ATSD to insert message
        String queryURL = "/messages/query";
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
        MessageQuery q = new MessageQuery();
        q.setEntity(entity).setStartDate(Util.ISOFormat(startUnixTime-10)).setEndDate(Util.ISOFormat(System.currentTimeMillis())).setType(type);
        List<MessageQuery> query = new ArrayList<>();
        query.add(q);
        query(queryURL, query, queryToken);
        assertTrue("User: " + username + " Response contains warning: " + responseAPIEntity,!(responseAPIEntity.contains("error")));
        assertEquals("User: " + username + " token message query response does not equal api message query response", responseAPIEntity , responseTokenEntity);
        //check message count query
        String countURL = "/messages/stats/query";
        String countToken = TokenRepository.getToken(username, HttpMethod.POST, countURL);
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
        MetricMethod.deleteMetric(metric);
        Registry.Metric.checkExists(metric);
        AlertTest.generateAlertForEntity(entity);
        //checking alerts query method
        String queryURL = "/alerts/query";
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
        AlertQuery q = new AlertQuery(entity);
        q.setStartDate(MIN_QUERYABLE_DATE);
        q.setEndDate(MAX_QUERYABLE_DATE);
        q.addMetric(metric);
        List<AlertQuery> query = new ArrayList<>();
        query.add(q);
        query(queryURL, query, queryToken);
        assertTrue("Alert was not inserted for user " + username, !(responseAPIEntity.equals("[]")));
        assertEquals("User: " + username + " Token Alert query response does not equal API Alert query response", responseAPIEntity, responseAPIEntity);
        //reading alert data from entity
        List<LinkedHashMap> alertList = responseToken.readEntity(List.class);
        LinkedHashMap alert = alertList.get(0);
        Integer id = (Integer)alert.get("id");
        Boolean acknowledged = (Boolean)alert.get("acknowledged");
        //check history query
        String historyQueryURL = "/alerts/history/query";
        String historyQueryToken = TokenRepository.getToken(username, HttpMethod.POST, historyQueryURL);
        AlertHistoryQuery ahq = new AlertHistoryQuery();
        ahq.setEntity(entity);
        ahq.setStartDate(MIN_QUERYABLE_DATE);
        ahq.setEndDate(MAX_QUERYABLE_DATE);
        List<AlertHistoryQuery> alertHistoryQueryList = new ArrayList<>();
        alertHistoryQueryList.add(ahq);
        query(historyQueryURL, alertHistoryQueryList, historyQueryToken);
        assertEquals("User: " + username + " Token alert history query response does not equal API alert history query response", responseAPIEntity, responseTokenEntity);
        //check delete query
        String deleteURL = "/alerts/delete";
        String deleteToken = TokenRepository.getToken(username, HttpMethod.POST, deleteURL);
        AlertQuery delete = new AlertQuery();
        delete.setId(id);
        List<AlertQuery> deleteQuery = new ArrayList<>();
        deleteQuery.add(delete);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method(HttpMethod.POST, Entity.json(deleteQuery)));
        //checking that series was successfully deleted
        responseAPI = executeApiRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .method(HttpMethod.POST, Entity.json(query)));
        responseAPI.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        assertTrue("User: " + username + " Property was not deleted with token response. Response body: " + responseAPIEntity, responseAPIEntity.equals("[]"));
        MetricMethod.deleteMetric(metric);
    }

    private void tokenMetricTestForUser(String username) throws Exception {
        String metricName = "token_test_metrictest_" + username + "_metric";
        String tagName = "name";
        String tagValue = "value";
        Metric metric = new Metric();
        metric.setEnabled(true);
        metric.setName(metricName);
        //checking create method
        String createURL = "/metrics/" + metricName;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        createOrReplace(username, createURL, metric, createToken);
        //checking get method
        String getURL = "/metrics/" + metricName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        get(getURL, getToken);
        assertTrue("Metric was not inserted with token for user "+username, !responseAPIEntity.contains("error"));
        assertEquals("Metric get request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking list method
        String listURL = "/metrics/";
        String listToken = TokenRepository.getToken(username, HttpMethod.GET, listURL);
        get(listURL,listToken);
        assertEquals("Metric list request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking update method
        String updateURL = "/metrics/" + metricName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        metric.addTag(tagName, tagValue);
        update(username, updateURL, metric,updateToken);
        String oldResponseEntity = responseTokenEntity;
        get(getURL,getToken); // checking that metric was updated
        assertTrue("Metric update method with token did not work for user " + username, !responseTokenEntity.equals( oldResponseEntity));
        //checking series and series tags methods
        String defaultMetricName = "entity.count"; //methods will be executed fot built-in metric
        String seriesURL ="/metrics/" + defaultMetricName + "/series";
        String seriesToken = TokenRepository.getToken(username, HttpMethod.GET, seriesURL);
        get(seriesURL, seriesToken);
        assertEquals("Metric series request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        String seriesTagsURL = "/metrics/" + defaultMetricName + "/series/tags";
        String seriesTagsToken = TokenRepository.getToken(username, HttpMethod.GET, seriesTagsURL);
        get(seriesTagsURL,seriesTagsToken);
        assertEquals("Metric series tags request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking rename method
        String renameURL = "/metrics/" + metricName + "/rename";
        String renameToken = TokenRepository.getToken(username, HttpMethod.POST, renameURL);
        metricName = metricName + "_1";
        Metric newMetric = new Metric(metricName);
        insert(username, renameURL, newMetric,renameToken);
        getURL=getURL+"_1";
        getToken = TokenRepository.getToken(username,HttpMethod.GET, getURL);
        get(getURL,getToken); //checking that metric was renamed successfully
        assertTrue("Metric was not renamed with token for user "+username, !responseAPIEntity.contains("error"));
        //checking delete method
        String deleteURL = "/metrics/" + metricName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                                                            .request()
                                                            .header(HttpHeaders.AUTHORIZATION, "Bearer "+deleteToken)
                                                            .method(HttpMethod.DELETE));
        //checking that metric was deleted
        get(getURL,getToken);
        assertTrue("Metric was not deleted with token for user "+username, responseAPIEntity.contains("error"));
    }

    private void tokenEntityTestForUer(String username) throws Exception {
        String entityName = "token_test_entitytest_" + username + "_entity";
        String tagName = "name";
        String tagValue = "value";
        com.axibase.tsd.api.model.entity.Entity entity = new com.axibase.tsd.api.model.entity.Entity(entityName);
        entity.setEnabled(true);
        //checking create method
        String createURL = "/entities/" + entityName;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        createOrReplace(username, createURL, entity, createToken);
        //checking get method
        String getURL = "/entities/" + entityName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        get(getURL, getToken);
        assertTrue("Entity was not inserted with token for user "+username, !responseAPIEntity.contains("error"));
        assertEquals("Entity get request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking list method
        String listURL = "/entities/";
        String listToken = TokenRepository.getToken(username, HttpMethod.GET, listURL);
        get(listURL,listToken);
        assertEquals("Entity list request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking update method
        String updateURL = "/entities/" + entityName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        entity.addTag(tagName, tagValue);
        update(username, updateURL, entity,updateToken);
        String oldResponseEntity = responseTokenEntity;
        get(getURL,getToken); // checking that metric was updated
        assertTrue("Entity update method with token did not work for user " + username, !responseTokenEntity.equals(oldResponseEntity));
        //checking entity groups, metrics and property types methods
        String defaultEntityName = "atsd"; //methods will be executed fot built-in entity
        String metricsURL = "/entities/" + defaultEntityName + "/metrics";
        String metricsToken = TokenRepository.getToken(username, HttpMethod.GET, metricsURL);
        get(metricsURL, metricsToken);
        assertEquals("Entity metrics request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        String entityGroupsURL = "/entities/" + defaultEntityName + "/groups";
        String entityGroupsToken = TokenRepository.getToken(username, HttpMethod.GET, entityGroupsURL);
        get(entityGroupsURL, entityGroupsToken);
        assertEquals("Entity \"entity groups \" request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        String propertyTypesURL = "/entities/" + defaultEntityName + "/property-types";
        String propertyTypesToken = TokenRepository.getToken(username, HttpMethod.GET, propertyTypesURL);
        get(propertyTypesURL, propertyTypesToken);
        assertEquals("Entity property-types request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking delete method
        String deleteURL = "/entities/" + entityName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+deleteToken)
                .method(HttpMethod.DELETE));
        get(getURL,getToken);
        assertTrue("Entity was not deleted with token for user "+username, responseAPIEntity.contains("error"));
    }

    private void tokenEntityGroupsTestForUser(String username) throws Exception {
        String entityGroupName = "token_test_entitygroupstest_" + username + "_entitygroup";
        String entity = "token_test_entitygrouptest_" + username + "_entity";
        EntityMethod.createOrReplaceEntity(new com.axibase.tsd.api.model.entity.Entity(entity).setEnabled(true));
        String tagName = "name";
        String tagValue = "value";
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        entityGroup.setEnabled(true);
        //checking create method
        String createURL = "/entity-groups/" + entityGroupName;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        createOrReplace(username, createURL, entityGroup, createToken);
        //checking get method
        String getURL = "/entity-groups/" + entityGroupName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        get(getURL, getToken);
        assertTrue("Entity group was not inserted with token for user "+username, !responseAPIEntity.contains("error"));
        assertEquals("Entity group get request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking list method
        String listURL = "/entity-groups";
        String listToken = TokenRepository.getToken(username, HttpMethod.GET, listURL);
        get(listURL,listToken);
        assertEquals("Entity group list request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking update method
        String updateURL = "/entity-groups/" + entityGroupName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        entityGroup.addTag(tagName, tagValue);
        update(username, updateURL, entityGroup, updateToken);
        String oldResponseEntity = responseTokenEntity;
        get(getURL,getToken); // checking that metric was updated
        assertTrue("Entity group update method with token did not work for user " + username, !responseTokenEntity.equals(oldResponseEntity));
        //checking get- add- set- and delete- entities methods
        String getEntitiesURL = "/entity-groups/" + entityGroupName + "/entities";
        String getEntitiesToken = TokenRepository.getToken(username, HttpMethod.GET, getEntitiesURL);
        get(getEntitiesURL, getEntitiesToken);
        assertEquals("Entities get from entity group failed for user " + username, responseAPIEntity, responseTokenEntity);

        String responseWithoutEntities = responseAPIEntity; //response body for empty entity group
        String addEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/add";
        List<String> entities = Collections.singletonList(entity);
        String addEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, addEntitiesURL);
        insert(username, addEntitiesURL, entities, addEntitiesToken); //add
        get(getEntitiesURL, getEntitiesToken);
        assertTrue("Entities add to entity group failed for user " + username, !responseAPIEntity.equals(responseWithoutEntities));
        String responseWithEntities = responseAPIEntity;

        String deleteEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/delete";
        String deleteEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, deleteEntitiesURL);
        insert(username, deleteEntitiesURL, entities, deleteEntitiesToken); //delete
        get(getEntitiesURL, getEntitiesToken);
        assertTrue("Entities delete from entity group failed for user " + username, !responseAPIEntity.equals(responseWithEntities));

        String setEntitiesURL = "/entity-groups/" + entityGroupName +"/entities/set";
        String setEntitiesToken = TokenRepository.getToken(username,HttpMethod.POST,setEntitiesURL);
        insert(username, setEntitiesURL, entities, setEntitiesToken);
        get(getEntitiesURL, getEntitiesToken);
        assertTrue("Entities set to entity group failed for user "+username, !responseAPIEntity.equals(responseWithoutEntities));
        //checking delete method
        String deleteURL = "/entity-groups/" + entityGroupName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+deleteToken)
                .method(HttpMethod.DELETE));
        get(getURL,getToken);
        assertTrue("Entity Group was not deleted with token for user "+username, responseAPIEntity.contains("error"));
    }

    private void tokenReplacementTablesTestForUser(String username) throws Exception {
        String replacementTable = "token_test_replacementtablestest_" + username + "_replacementtable";
        String csvPayload = "-1,Error";
        //checking create method
        String createURL = "/replacement-tables/csv/" + replacementTable;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        createOrReplace(username,createURL, csvPayload, createToken);
        //checking get method
        String getURL = "/replacement-tables/csv/" +replacementTable;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET,getURL);
        get(getURL, getToken);
        assertTrue("Replacement table was not created for user " + username, responseAPI.getStatus()!=404);
        assertEquals("Replacement table get request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        String oldGetRespnse = responseAPIEntity; //buffering get response to check update method
        //checking list method
        String listURL = "/replacement-tables/csv/";
        String listToken = TokenRepository.getToken(username, HttpMethod.GET, listURL);
        get(listURL, listToken);
        assertEquals("Replacement tables list request executed with token and with API does not equal for user: " + username, responseAPIEntity, responseTokenEntity);
        //checking update method
        String updateURL = "/replacement-tables/csv/" + replacementTable;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        String newCsvPayload = "0,Unknown";
        update(username, updateURL, newCsvPayload, updateToken);
        get(getURL,getToken);
        assertTrue("Replacement table was not changed after update request with token for user: " + username, !responseAPIEntity.equals(oldGetRespnse));
        //checking delete method
        String deleteURL = "/replacement-tables/" + replacementTable;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+deleteToken)
                .method(HttpMethod.DELETE));
        get(getURL,getToken);
        assertEquals("Replacement table was not deleted with token request for user: " + username, 404, responseToken.getStatus());
    }

    private void dualTokenWorkTestForUser(String username) throws Exception {
        String firstURL = "/entities";
        String secondURL = "/metrics";
        String method = HttpMethod.GET;
        String token = TokenRepository.getToken(username, method, firstURL + "\n" + secondURL);
        //checking first url work
        get(firstURL, token);
        assertEquals("Token work test with multiple urls (first url) failed for user " + username, responseAPIEntity, responseTokenEntity);
        //checking second url work
        get(secondURL, token);
        assertEquals("Token work test with multiple urls (second url) failed for user " + username, responseAPIEntity, responseTokenEntity);
    }

    private void insert(String username, String insertURL, Object insertData, String insertToken) {
        responseToken = executeTokenRequest(webTarget -> webTarget.path(insertURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + insertToken)
                .method(HttpMethod.POST, Entity.json(insertData)));
        assertEquals("User: " + username,Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseToken));
    }

    private void get(String getURL, String getToken) {
        responseAPI = executeApiRequest(webTarget -> webTarget.path(getURL)
                .request()
                .method(HttpMethod.GET));
        responseAPI.bufferEntity();
        responseToken = executeTokenRequest(webTarget -> webTarget.path(getURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method(HttpMethod.GET));
        responseToken.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        responseTokenEntity = responseToken.readEntity(String.class);
    }

    private void query(String queryURL, Object query, String queryToken) {
        responseAPI = executeApiRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .method(HttpMethod.POST, Entity.json(query)));
        responseAPI.bufferEntity();
        responseToken = executeTokenRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + queryToken)
                .method(HttpMethod.POST, Entity.json(query)));
        responseToken.bufferEntity();
        responseAPIEntity = responseAPI.readEntity(String.class);
        responseTokenEntity = responseToken.readEntity(String.class);
    }

    private void createOrReplace(String username, String url, Object data, String token) {
        responseToken =  executeTokenRequest(webTarget -> webTarget.path(url)
                                                            .request()
                                                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                                            .method(HttpMethod.PUT, Entity.json(data)));

        assertEquals("User: " + username,Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseToken));
    }

    private void update(String username, String url, Object data, String token) {
        responseToken =  executeTokenRequest(webTarget -> webTarget.path(url)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method("PATCH", Entity.json(data)));

        assertEquals("User: " + username,Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseToken));
    }


   @AfterClass
    private void deleteUser() {
        String path ="/admin/users/edit.xhtml";
        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("userBean.username", USER_NAME)
                .queryParam("delete", "Delete")
                .request()
                .method(HttpMethod.POST));
    }
}
