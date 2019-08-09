package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.entity.EntityTest;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.entity.EntityMethodGroupResponse;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import java.util.Collections;

import static org.testng.AssertJUnit.assertTrue;

public class TokenEntityTest extends EntityTest {
    private final String username;

    @Factory(
            dataProvider = "users", dataProviderClass = TokenUsers.class
    )
    public TokenEntityTest(String username) {
        this.username = username;
    }

    @Test(
            description = "Tests entity get endpoint."
    )
    @Issue("6052")
    public void testGetMethod() throws Exception {
        String entityName = Mocks.entity();
        String url = "/entities/" + entityName;
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Entity entity = new Entity(entityName);
        createOrReplaceEntityCheck(entity);

        Response response = getEntityResponse(entityName, token);
        assertTrue(compareJsonString(Util.prettyPrint(entity), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests entity update endpoint."
    )
    @Issue("6052")
    public void testUpdateMethod() throws Exception {
        String entityName = Mocks.entity();
        String url = "/entities/" + entityName;
        String token = TokenRepository.getToken(username, "PATCH", url);
        Entity entity = new Entity(entityName);
        createOrReplaceEntityCheck(entity);

        entity.setLabel(Mocks.LABEL);
        updateEntity(entity, token);
        Checker.check(new EntityCheck(entity));
    }

    @Test(
            description = "Tests entity create or replace endpoint."
    )
    @Issue("6052")
    public void testCreateMethod() throws Exception {
        String entityName = Mocks.entity();
        String url = "/entities/" + entityName;
        String token = TokenRepository.getToken(username, HttpMethod.PUT, url);
        Entity entity = new Entity(entityName);

        createOrReplaceEntity(entity, token);
        Checker.check(new EntityCheck(entity));
    }

    @Test(
            description = "Tests entity delete endpoint."
    )
    @Issue("6052")
    public void testDeleteMethod() throws Exception {
        String entityName = Mocks.entity();
        String url = "/entities/" + entityName;
        String token = TokenRepository.getToken(username, HttpMethod.DELETE, url);
        Entity entity = new Entity(entityName);
        createOrReplaceEntityCheck(entity);

        deleteEntity(entityName, token);
        Checker.check(new DeletionCheck(new EntityCheck(entity)));
    }

    @Test(
            description = "Tests entity group endpoint."
    )
    @Issue("6052")
    public void testEntityGroupMethod() throws Exception {
        String entityName = Mocks.entity();
        String url = "/entities/" + entityName + "/groups";
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        EntityGroup group = new EntityGroup(Mocks.entityGroup());
        Entity entity = new Entity(entityName);
        createOrReplaceEntityCheck(entity);
        EntityGroupMethod.createOrReplaceEntityGroupCheck(group);
        EntityGroupMethod.addEntities(group.getName(), Collections.singletonList(entityName));

        Response response = queryEntityGroups(entityName, token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(new EntityMethodGroupResponse(group))),
                response.readEntity(String.class)));
    }

    @Test(
            description = "Tests entity metrics endpoint."
    )
    @Issue("6052")
    public void testMetricsMethod() throws Exception {
        String entityName = Mocks.entity();
        String url = "/entities/" + entityName + "/metrics";
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Metric metric = new Metric(Mocks.metric());
        Series series = new Series(entityName, metric.getName())
                .addSamples(Mocks.SAMPLE);
        SeriesMethod.insertSeriesCheck(series);

        Response response = queryEntityMetrics(entityName, token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(metric)), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests entity property types endpoint."
    )
    @Issue("6052")
    public void testPropertyTypesMethod() throws Exception {
        String entityName = Mocks.entity();
        String url = "/entities/" + entityName + "/property-types";
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Property property = new Property(Mocks.propertyType(), entityName)
                .setTags(Mocks.TAGS);
        createOrReplaceEntityCheck(entityName);
        PropertyMethod.insertPropertyCheck(property);

        Response response = queryEntityPropertyTypes(entityName, token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(property.getType())),
                response.readEntity(String.class)));
    }
}
