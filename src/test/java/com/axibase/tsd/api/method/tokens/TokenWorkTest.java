package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.alert.AlertTest;
import com.axibase.tsd.api.method.checks.*;
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
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
    private static final String USER_NAME = "apitokenuser_worktest";
    private static final String USER_PASSWORD = RandomStringUtils.random(10, true, true);
    private static final String ADMIN_NAME = Config.getInstance().getLogin();

    @DataProvider
    private Object[][] users() {
        return new String[][]{
                {ADMIN_NAME},
                {USER_NAME}
        };
    }

    @BeforeClass
    private void createUser() {
        String path = "/admin/users/edit.xhtml";

        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("enabled", "on")
                .queryParam("userBean.username", USER_NAME)
                .queryParam("userBean.password", USER_PASSWORD)
                .queryParam("repeatPassword", USER_PASSWORD)
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
    public void tokenSeriesTest(String username) throws Exception {
        Response responseWithToken;

        String responseTokenEntity;
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
        insert(username, insertURL, seriesList, insertToken);
        Checker.check(new SeriesCheck(seriesList));
        //checking get method
        String getURL = "/series/json/" + entity + "/" + metric;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL + "?startDate=previous_hour&endDate=next_day");
        responseWithToken = executeTokenRequest(webTarget -> webTarget.path(getURL)
                .queryParam("startDate", "previous_hour")
                .queryParam("endDate", "next_day")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method(HttpMethod.GET));
        responseWithToken.bufferEntity();
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("User: " + username + " Response contains warning: " + responseTokenEntity, !(responseTokenEntity.contains("warning")));
        compareJsonString(seriesList.toString(), responseTokenEntity, false);
        //checking queries
        String queryURL = "/series/query";
        SeriesQuery q = new SeriesQuery(entity, metric, startUnixTime, System.currentTimeMillis());
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
        List<SeriesQuery> query = new ArrayList<>();
        query.add(q);
        responseWithToken = query(queryURL, query, queryToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("User: " + username + " Response contains warning: " + responseTokenEntity, !(responseTokenEntity.contains("warning")));
        compareJsonString(query.toString(), responseTokenEntity, false);
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
                .method(HttpMethod.POST, Entity.json(deleteQuery)))
                .bufferEntity();
        //checking that series was successfully deleted
        Checker.check(new NotPassedCheck(new SeriesCheck(seriesList)));
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenPropertiesTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
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
        Checker.check(new PropertyCheck(property));
        //checking get method
        String getURL = "/properties/" + entity + "/types/" + type;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = get(getURL, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("User: " + username + " Properties get response gives empty output after token insertion", !(responseTokenEntity.equals("[]")));
        compareJsonString(propertyList.toString(), responseTokenEntity, false);
        //checking properties get types request
        String getTypesURL = "/properties/" + entity + "/types";
        String getTypesToken = TokenRepository.getToken(username, HttpMethod.GET, getTypesURL);
        responseWithToken = get(getTypesURL, getTypesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("User: " + username + " Properties get types response gives empty output after token insertion", !(responseTokenEntity.equals("[]")));
        compareJsonString("[\"" + type + "\"]", responseTokenEntity, false);
        //checking properties queries
        String queryURL = "/properties/query";
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
        PropertyQuery q = new PropertyQuery(type, entity);
        q.setStartDate(Util.ISOFormat(startUnixTime - 10));
        q.setEndDate(Util.ISOFormat(System.currentTimeMillis()));
        List<PropertyQuery> query = new ArrayList<>();
        query.add(q);
        responseWithToken = query(queryURL, query, queryToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("User: " + username + " Response contains warning: " + responseTokenEntity, !(responseTokenEntity.equals("[]")));
        compareJsonString(query.toString(), responseTokenEntity, false);
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
                .method(HttpMethod.POST, Entity.json(deleteQuery)))
                .bufferEntity();
        //checking that series was successfully deleted
        Checker.check(new NotPassedCheck(new PropertyCheck(property)));
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenMessagesTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
        String entity = "token_test_messages_" + username + "_entity";
        String type = "logger";
        String messageText = "message";
        long startUnixTime = System.currentTimeMillis();
        Message message = new Message(entity, type);
        message.setDate(Util.ISOFormat(startUnixTime));
        List<Message> messageList = new ArrayList<>();
        message.setMessage(messageText);
        messageList.add(message);

        String insertURL = "/messages/insert";
        String insertToken = TokenRepository.getToken(username, HttpMethod.POST, insertURL);
        insert(username, insertURL, messageList, insertToken);
        Checker.check(new EntityCheck(new com.axibase.tsd.api.model.entity.Entity(entity)));
        Checker.check(new MessageCheck(message));
        //check message query
        String queryURL = "/messages/query";
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
        MessageQuery q = new MessageQuery();
        q.setEntity(entity).setStartDate(Util.ISOFormat(startUnixTime - 10)).setEndDate(Util.ISOFormat(System.currentTimeMillis())).setType(type);
        List<MessageQuery> query = new ArrayList<>();
        query.add(q);
        responseWithToken = query(queryURL, query, queryToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("User: " + username + " Message insertion with token failed. Response : " + responseTokenEntity, !(responseTokenEntity.contains("error") || responseTokenEntity.equals(new ArrayList<>().toString())));
        compareJsonString(query.toString(), responseTokenEntity, false);
        //check message count query
        String countURL = "/messages/stats/query";
        String countToken = TokenRepository.getToken(username, HttpMethod.POST, countURL);
        MessageStatsQuery msq = new MessageStatsQuery();
        msq.setEntity(entity);
        msq.setType(type);
        msq.setStartDate(Util.ISOFormat(startUnixTime - 10));
        msq.setEndDate(Util.ISOFormat((System.currentTimeMillis())));
        List<MessageStatsQuery> messageStatsQueryList = new ArrayList<>();
        messageStatsQueryList.add(msq);
        responseWithToken = query(countURL, messageStatsQueryList, countToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString(messageStatsQueryList.toString(), responseTokenEntity, false);
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenAlertsTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
        String entity = "token_test_alerts_" + username + "_entity";
        String metric = AlertTest.RULE_METRIC_NAME;
        MetricMethod.deleteMetric(metric);
        Checker.check(new NotPassedCheck(new MetricCheck(new Metric(metric))));
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
        responseWithToken = query(queryURL, query, queryToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Alert fsiled to get read by token for user " + username, !(responseTokenEntity.equals("[]")));
        compareJsonString(query.toString(), responseTokenEntity, false);
        //reading alert data from entity
        List<LinkedHashMap> alertList = responseWithToken.readEntity(List.class);
        LinkedHashMap alert = alertList.get(0);
        Integer id = (Integer) alert.get("id");
        Boolean acknowledged = (Boolean) alert.get("acknowledged");
        //check history query
        String historyQueryURL = "/alerts/history/query";
        String historyQueryToken = TokenRepository.getToken(username, HttpMethod.POST, historyQueryURL);
        AlertHistoryQuery ahq = new AlertHistoryQuery();
        ahq.setEntity(entity);
        ahq.setStartDate(MIN_QUERYABLE_DATE);
        ahq.setEndDate(MAX_QUERYABLE_DATE);
        List<AlertHistoryQuery> alertHistoryQueryList = new ArrayList<>();
        alertHistoryQueryList.add(ahq);
        responseWithToken = query(historyQueryURL, alertHistoryQueryList, historyQueryToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString(alertHistoryQueryList.toString(), responseTokenEntity, false);
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
                .method(HttpMethod.POST, Entity.json(deleteQuery)))
                .bufferEntity();
        Checker.check(new NotPassedCheck(new AlertsCheck(query)));
        MetricMethod.deleteMetric(metric);
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenMetricTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
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
        Checker.check(new MetricCheck(metric));
        //checking get method
        String getURL = "/metrics/" + metricName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = get(getURL, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Metric was not inserted with token for user " + username, !responseTokenEntity.contains("error"));
        compareJsonString(metric.toString(), responseTokenEntity, false);
        //checking update method
        String updateURL = "/metrics/" + metricName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        metric.addTag(tagName, tagValue);
        update(username, updateURL, metric, updateToken);
        Checker.check(new MetricCheck(metric));
        //checking series and series tags methods
        String defaultMetricName = "entity.count"; //methods will be executed fot built-in metric
        String seriesURL = "/metrics/" + defaultMetricName + "/series";
        String seriesToken = TokenRepository.getToken(username, HttpMethod.GET, seriesURL);
        responseWithToken = get(seriesURL, seriesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString(metric.toString(), responseTokenEntity, false);
        String seriesTagsURL = "/metrics/" + defaultMetricName + "/series/tags";
        String seriesTagsToken = TokenRepository.getToken(username, HttpMethod.GET, seriesTagsURL);
        responseWithToken = get(seriesTagsURL, seriesTagsToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString("{\"" + tagName + "\": [\"" + tagValue + "\"]}", responseTokenEntity, false);
        //checking rename method
        String renameURL = "/metrics/" + metricName + "/rename";
        String renameToken = TokenRepository.getToken(username, HttpMethod.POST, renameURL);
        metricName = metricName + "_1";
        Metric newMetric = new Metric(metricName);
        insert(username, renameURL, newMetric, renameToken);
        Checker.check(new NotPassedCheck(new MetricCheck(metric)));
        Checker.check(new MetricCheck(newMetric));
        //checking delete method
        String deleteURL = "/metrics/" + metricName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method(HttpMethod.DELETE))
                .bufferEntity();
        Checker.check(new NotPassedCheck(new MetricCheck(newMetric)));
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenEntityTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
        String entityName = "token_test_entitytest_" + username + "_entity";
        String tagName = "name";
        String tagValue = "value";
        com.axibase.tsd.api.model.entity.Entity entity = new com.axibase.tsd.api.model.entity.Entity(entityName);
        entity.setEnabled(true);
        //checking create method
        String createURL = "/entities/" + entityName;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        createOrReplace(username, createURL, entity, createToken);
        Checker.check(new EntityCheck(entity));
        //checking get method
        String getURL = "/entities/" + entityName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = get(getURL, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entity was not inserted with token for user " + username, !responseTokenEntity.contains("error"));
        compareJsonString(entity.toString(), responseTokenEntity, false);
        //checking update method
        String updateURL = "/entities/" + entityName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        entity.addTag(tagName, tagValue);
        update(username, updateURL, entity, updateToken);
        Checker.check(new EntityCheck(entity));
        //checking entity groups, metrics and property types methods
        String metricsURL = "/entities/" + entityName + "/metrics";
        String metricsToken = TokenRepository.getToken(username, HttpMethod.GET, metricsURL);
        responseWithToken = get(metricsURL, metricsToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        //TODO comparators
        String entityGroupsURL = "/entities/" + entityName + "/groups";
        String entityGroupsToken = TokenRepository.getToken(username, HttpMethod.GET, entityGroupsURL);
        responseWithToken = get(entityGroupsURL, entityGroupsToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);

        String propertyTypesURL = "/entities/" + entityName + "/property-types";
        String propertyTypesToken = TokenRepository.getToken(username, HttpMethod.GET, propertyTypesURL);
        responseWithToken = get(propertyTypesURL, propertyTypesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);

        //checking delete method
        String deleteURL = "/entities/" + entityName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method(HttpMethod.DELETE))
                .bufferEntity();
        Checker.check(new NotPassedCheck(new EntityCheck(entity)));
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenEntityGroupsTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
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
        Checker.check(new EntityGroupCheck(entityGroup));
        //checking get method
        String getURL = "/entity-groups/" + entityGroupName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = get(getURL, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entity group was not inserted with token for user " + username, !responseTokenEntity.contains("error"));
        compareJsonString(entityGroup.toString(), responseTokenEntity, false);
        //checking update method
        String updateURL = "/entity-groups/" + entityGroupName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        entityGroup.addTag(tagName, tagValue);
        update(username, updateURL, entityGroup, updateToken);
        Checker.check(new EntityGroupCheck(entityGroup));
        //checking get- add- set- and delete- entities methods
        String getEntitiesURL = "/entity-groups/" + entityGroupName + "/entities";
        String getEntitiesToken = TokenRepository.getToken(username, HttpMethod.GET, getEntitiesURL);
        responseWithToken = get(getEntitiesURL, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString("[]", responseTokenEntity, false);

        String responseWithoutEntities = responseTokenEntity; //response body for empty entity group
        String addEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/add";
        List<String> entities = Collections.singletonList(entity);
        String addEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, addEntitiesURL);
        insert(username, addEntitiesURL, entities, addEntitiesToken); //add
        responseWithToken = get(getEntitiesURL, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entities add to entity group failed for user " + username, !responseTokenEntity.equals(responseWithoutEntities));
        String responseWithEntities = responseTokenEntity;

        String deleteEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/delete";
        String deleteEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, deleteEntitiesURL);
        insert(username, deleteEntitiesURL, entities, deleteEntitiesToken); //delete
        responseWithToken = get(getEntitiesURL, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entities delete from entity group failed for user " + username, !responseTokenEntity.equals(responseWithEntities));

        String setEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/set";
        String setEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, setEntitiesURL);
        insert(username, setEntitiesURL, entities, setEntitiesToken);
        responseWithToken = get(getEntitiesURL, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entities set to entity group failed for user " + username, !responseTokenEntity.equals(responseWithoutEntities));
        //checking delete method
        String deleteURL = "/entity-groups/" + entityGroupName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method(HttpMethod.DELETE))
                .bufferEntity();
        Checker.check(new NotPassedCheck(new EntityGroupCheck(entityGroup)));
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenReplacementTablesTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
        String replacementTable = "token_test_replacementtablestest_" + username + "_replacementtable";
        String csvPayload = "1,Ok";
        //checking create method
        String createURL = "/replacement-tables/csv/" + replacementTable;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        createOrReplace(username, createURL, csvPayload, createToken);
        Checker.check(new ReplacementTableCheck(new ReplacementTable().setName(replacementTable)));
        //checking get method
        String getURL = "/replacement-tables/csv/" + replacementTable;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = get(getURL, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Replacement table was not created for user " + username, responseWithToken.getStatus() != 404);
        assertEquals("Replacement table get request executed with token and with API does not equal for user: " + username, "Key,Value\r\n" + csvPayload + "\r\n", responseTokenEntity);
        String oldGetResponse = responseTokenEntity; //buffering get response to check update method
        //checking update method
        /* //TODO finish replacement table updates
        String updateURL = "/replacement-tables/csv/" + replacementTable;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        String newCsvPayload = "0,Unknown";
        update(username, updateURL, newCsvPayload, updateToken);
        ResponsePair getAfterUpdatePair = get(getURL,getToken);
        responseWithAPI = getAfterUpdatePair.getResponseWithApi();
        responseWithToken = getAfterUpdatePair.getResponseWithToken();
        responseAPIEntity = responseWithAPI.readEntity(String.class);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Replacement table was not changed after update request with token for user: " + username, !responseAPIEntity.equals(oldGetResponse)); */
        //checking delete method
        String deleteURL = "/replacement-tables/" + replacementTable;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        executeTokenRequest(webTarget -> webTarget.path(deleteURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method(HttpMethod.DELETE))
                .bufferEntity();
        Checker.check(new NotPassedCheck(new ReplacementTableCheck(new ReplacementTable().setName(replacementTable))));
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void dualTokenTest(String username) throws Exception {
        String entityName = "token_test_dualtoken_" + username + "_entity";
        com.axibase.tsd.api.model.entity.Entity entity = new com.axibase.tsd.api.model.entity.Entity(entityName);
        entity.setEnabled(true);
        String metricName = "token_test_dualtoken_" + username + "_metric";
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

    private Response insert(String username, String insertURL, Object insertData, String insertToken) {
        Response responseWithToken = executeTokenRequest(webTarget -> webTarget.path(insertURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + insertToken)
                .method(HttpMethod.POST, Entity.json(insertData)));
        responseWithToken.bufferEntity();
        assertEquals("User: " + username, Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseWithToken));
        return responseWithToken;
    }

    private Response get(String getURL, String getToken) {
        Response responseWithToken = executeTokenRequest(webTarget -> webTarget.path(getURL)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method(HttpMethod.GET));
        responseWithToken.bufferEntity();
        return responseWithToken;
    }

    private Response query(String queryURL, Object query, String queryToken) {
        Response responseWithToken = executeTokenRequest(webTarget -> webTarget.path(queryURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + queryToken)
                .method(HttpMethod.POST, Entity.json(query)));
        responseWithToken.bufferEntity();
        return responseWithToken;
    }

    private Response createOrReplace(String username, String url, Object data, String token) {
        Response responseWithToken = executeTokenRequest(webTarget -> webTarget.path(url)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method(HttpMethod.PUT, Entity.json(data)));
        responseWithToken.bufferEntity();
        assertEquals("User: " + username, Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseWithToken));
        return responseWithToken;
    }

    private Response update(String username, String url, Object data, String token) {
        Response responseWithToken = executeTokenRequest(webTarget -> webTarget.path(url)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .method("PATCH", Entity.json(data)));
        responseWithToken.bufferEntity();
        assertEquals("User: " + username, Response.Status.Family.SUCCESSFUL, Util.responseFamily(responseWithToken));
        return responseWithToken;
    }

}
