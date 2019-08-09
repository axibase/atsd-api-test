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
    public void tokenEntityTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
        String entityName = Mocks.entity();
        String tagName = "name";
        String tagValue = "value";
        com.axibase.tsd.api.model.entity.Entity entity = new com.axibase.tsd.api.model.entity.Entity().setName(entityName);
        entity.setEnabled(true);
        //checking create method
        String createURL = "/entities/" + entityName;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        EntityMethod.createOrReplaceEntity(entity, createToken);
        Checker.check(new EntityCheck(entity));
        //checking get method
        String getURL = "/entities/" + entityName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = EntityMethod.getEntityResponse(entityName, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entity was not inserted with token for user " + username, !responseTokenEntity.contains("error"));
        compareJsonString(entity.toString(), responseTokenEntity, false);
        //checking update method
        String updateURL = "/entities/" + entityName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        entity.addTag(tagName, tagValue);
        EntityMethod.updateEntity(entity, updateToken);
        Checker.check(new EntityCheck(entity));
        //checking entity groups, metrics and property types methods
        Metric metric = new Metric()
                .setName(Mocks.metric())
                .setEnabled(true);
        Series series = new Series();
        series.setMetric(metric.getName());
        series.setEntity(entityName);
        series.addSamples(Sample.ofTimeInteger(System.currentTimeMillis(), 60));
        SeriesMethod.insertSeriesCheck(series);
        String metricsURL = "/entities/" + entityName + "/metrics";
        String metricsToken = TokenRepository.getToken(username, HttpMethod.GET, metricsURL);
        responseWithToken = EntityMethod.queryEntityMetrics(entityName, metricsToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString(metric.toString(), responseTokenEntity, false);

        EntityGroup entityGroup = new EntityGroup();
        entityGroup.setName(Mocks.entityGroup());
        entityGroup.setEnabled(true);
        EntityGroupMethod.createOrReplaceEntityGroupCheck(entityGroup);
        EntityGroupMethod.addEntities(entityGroup.getName(), Collections.singletonList(entityName));
        String entityGroupsURL = "/entities/" + entityName + "/groups";
        String entityGroupsToken = TokenRepository.getToken(username, HttpMethod.GET, entityGroupsURL);
        responseWithToken = EntityMethod.queryEntityGroups(entityName, entityGroupsToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString(entityGroup.toString(), responseTokenEntity, false);

        Property property = new Property();
        property.setType(Mocks.propertyType());
        property.setEntity(entityName);
        property.addTag("name", "value");
        PropertyMethod.insertPropertyCheck(property);
        String propertyTypesURL = "/entities/" + entityName + "/property-types";
        String propertyTypesToken = TokenRepository.getToken(username, HttpMethod.GET, propertyTypesURL);
        responseWithToken = EntityMethod.queryEntityPropertyTypes(entityName, propertyTypesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertEquals("Entity Property Types method does not work with tokens for user " + username, "[\"" + property.getType() + "\"]", responseTokenEntity);

        //checking delete method
        String deleteURL = "/entities/" + entityName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        EntityMethod.deleteEntity(entityName, deleteToken);
        Checker.check(new DeletionCheck(new EntityCheck(entity)));

    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenEntityGroupsTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
        String entityGroupName = Mocks.entityGroup();
        String entity = Mocks.entity();
        EntityMethod.createOrReplaceEntity(new com.axibase.tsd.api.model.entity.Entity(entity).setEnabled(true));
        String tagName = "name";
        String tagValue = "value";
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        entityGroup.setEnabled(true);
        //checking create method
        String createURL = "/entity-groups/" + entityGroupName;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        EntityGroupMethod.createOrReplaceEntityGroup(entityGroup, createToken);
        Checker.check(new EntityGroupCheck(entityGroup));
        //checking get method
        String getURL = "/entity-groups/" + entityGroupName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = EntityGroupMethod.getEntityGroup(entityGroupName, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entity group was not inserted with token for user " + username, !responseTokenEntity.contains("error"));
        compareJsonString(entityGroup.toString(), responseTokenEntity, false);
        //checking update method
        String updateURL = "/entity-groups/" + entityGroupName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        entityGroup.addTag(tagName, tagValue);
        EntityGroupMethod.updateEntityGroup(entityGroup, updateToken);
        Checker.check(new EntityGroupCheck(entityGroup));
        //checking get- add- set- and delete- entities methods
        String getEntitiesURL = "/entity-groups/" + entityGroupName + "/entities";
        String getEntitiesToken = TokenRepository.getToken(username, HttpMethod.GET, getEntitiesURL);
        responseWithToken = EntityGroupMethod.getEntities(entityGroupName, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        compareJsonString("[]", responseTokenEntity, false);

        String responseWithoutEntities = responseTokenEntity; //response body for empty entity group
        String addEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/add?createEntities=true";
        List<String> entities = Collections.singletonList(entity);
        String addEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, addEntitiesURL);
        EntityGroupMethod.addEntities(entityGroupName, entities, addEntitiesToken); //add
        responseWithToken = EntityGroupMethod.getEntities(entityGroupName, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entities add to entity group failed for user " + username, !responseTokenEntity.equals(responseWithoutEntities));
        String responseWithEntities = responseTokenEntity;

        String deleteEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/delete";
        String deleteEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, deleteEntitiesURL);
        EntityGroupMethod.deleteEntities(entityGroupName, entities, deleteEntitiesToken); //delete
        responseWithToken = get(getEntitiesURL, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entities delete from entity group failed for user " + username, !responseTokenEntity.equals(responseWithEntities));

        String setEntitiesURL = "/entity-groups/" + entityGroupName + "/entities/set?createEntities=true";
        String setEntitiesToken = TokenRepository.getToken(username, HttpMethod.POST, setEntitiesURL);
        EntityGroupMethod.setEntities(entityGroupName, entities, setEntitiesToken);
        responseWithToken = get(getEntitiesURL, getEntitiesToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Entities set to entity group failed for user " + username, !responseTokenEntity.equals(responseWithoutEntities));
        //checking delete method
        String deleteURL = "/entity-groups/" + entityGroupName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        EntityGroupMethod.deleteEntityGroup(entityGroupName, deleteToken);
        Checker.check(new DeletionCheck(new EntityGroupCheck(entityGroup)));
    }

    @Issue("6052")
    @Test(
            dataProvider = "users"
    )
    public void tokenReplacementTablesTest(String username) throws Exception {
        Response responseWithToken;
        String responseTokenEntity;
        String replacementTableName = Mocks.replacementTable();
        String csvPayload = "1,Ok";
        //checking create method
        ReplacementTable replacementTable = new ReplacementTable().setName(replacementTableName).setAuthor(username).setValueFormat(SupportedFormat.LIST);
        replacementTable.addValue(StringUtils.substringBefore(csvPayload, ","), StringUtils.substringAfterLast(csvPayload, ","));
        String createURL = "/replacement-tables/json/" + replacementTableName;
        String createToken = TokenRepository.getToken(username, HttpMethod.PUT, createURL);
        ReplacementTableMethod.createResponse(replacementTable, createToken);
        Checker.check(new ReplacementTableCheck(replacementTable));
        //checking get method
        String getURL = "/replacement-tables/json/" + replacementTableName;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        responseWithToken = ReplacementTableMethod.getReplacementTablesResponse(replacementTableName, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        assertTrue("Replacement table was not created for user " + username, responseWithToken.getStatus() != 404);
        compareJsonString(Util.prettyPrint(replacementTable), responseTokenEntity);
        String oldGetResponse = responseTokenEntity; //buffering get response to check update method
        //checking update method
        String updateURL = "/replacement-tables/json/" + replacementTableName;
        String updateToken = TokenRepository.getToken(username, "PATCH", updateURL);
        String newCsvPayload = "0,Unknown";
        replacementTable.addValue(StringUtils.substringBefore(newCsvPayload, ","), StringUtils.substringAfterLast(newCsvPayload, ","));
        ReplacementTableMethod.updateReplacementTableResponse(replacementTable, updateToken);
        responseWithToken = get(getURL, getToken);
        responseTokenEntity = responseWithToken.readEntity(String.class);
        Checker.check(new ReplacementTableCheck(replacementTable));
        //checking delete method
        String deleteURL = "/replacement-tables/" + replacementTableName;
        String deleteToken = TokenRepository.getToken(username, HttpMethod.DELETE, deleteURL);
        ReplacementTableMethod.deleteReplacementTableResponse(replacementTableName, deleteToken);
        Checker.check(new DeletionCheck(new ReplacementTableCheck(new ReplacementTable().setName(replacementTableName))));
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
