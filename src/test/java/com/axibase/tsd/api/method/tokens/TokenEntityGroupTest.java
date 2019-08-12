package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.method.checks.EntityGroupCheck;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class TokenEntityGroupTest extends EntityGroupMethod {
    private final String username;

    @Factory(
            dataProvider = "users", dataProviderClass = TokenUsers.class
    )
    public TokenEntityGroupTest(String username) {
        this.username = username;
    }

    @Test(
            description = "Tests entity group get endpoint."
    )
    @Issue("6052")
    public void testGetMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName;
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        createOrReplaceEntityGroupCheck(entityGroup);

        Response response = getEntityGroup(entityGroupName, token);
        assertTrue(compareJsonString(Util.prettyPrint(entityGroup), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests entity group update endpoint."
    )
    @Issue("6052")
    public void testUpdateMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName;
        String token = TokenRepository.getToken(username, "PATCH", url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        createOrReplaceEntityGroupCheck(entityGroup);

        entityGroup.setTags(Mocks.TAGS);
        updateEntityGroup(entityGroup, token);
        Checker.check(new EntityGroupCheck(entityGroup));
    }

    @Test(
            description = "Tests entity group create or replace endpoint."
    )
    @Issue("6052")
    public void testCreateMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName;
        String token = TokenRepository.getToken(username, HttpMethod.PUT, url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);

        createOrReplaceEntityGroup(entityGroup, token);
        Checker.check(new EntityGroupCheck(entityGroup));
    }

    @Test(
            description = "Tests entity group delete endpoint."
    )
    @Issue("6052")
    public void testDeleteMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName;
        String token = TokenRepository.getToken(username, HttpMethod.DELETE, url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        createOrReplaceEntityGroupCheck(entityGroup);

        deleteEntityGroup(entityGroupName, token);
        Checker.check(new DeletionCheck(new EntityGroupCheck(entityGroup)));
    }

    @Test(
            description = "Tests entity group get entities endpoint."
    )
    @Issue("6052")
    public void testGetEntitiesMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName + "/entities";
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        Entity entity = new Entity(Mocks.entity());
        createOrReplaceEntityGroupCheck(entityGroup);

        addEntities(entityGroupName, Collections.singletonList(entity.getName()));
        Response response = getEntities(entityGroupName, token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(entity)),
                response.readEntity(String.class)));
    }

    @Test(
            description = "Tests entity group add entities endpoint."
    )
    @Issue("6052")
    public void testAddEntitiesMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName + "/entities/add?createEntities=true";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        Entity entity = new Entity(Mocks.entity());
        createOrReplaceEntityGroupCheck(entityGroup);

        addEntities(entityGroupName, Collections.singletonList(entity.getName()), token);
        Response response = getEntities(entityGroupName);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(entity)),
                response.readEntity(String.class)));
    }

    @Test(
            description = "Tests entity group set entities endpoint."
    )
    @Issue("6052")
    public void testSetEntitiesMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName + "/entities/set?createEntities=true";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        Entity entity = new Entity(Mocks.entity());
        createOrReplaceEntityGroupCheck(entityGroup);

        setEntities(entityGroupName, Collections.singletonList(entity.getName()), token);
        Response response = getEntities(entityGroupName);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(entity)),
                response.readEntity(String.class)));
    }

    @Test(
            description = "Tests entity group delete entities endpoint."
    )
    @Issue("6052")
    public void testDeleteEntitiesMethod() throws Exception {
        String entityGroupName = Mocks.entityGroup();
        String url = "/entity-groups/" + entityGroupName + "/entities/delete";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        EntityGroup entityGroup = new EntityGroup(entityGroupName);
        Entity entity = new Entity(Mocks.entity());
        createOrReplaceEntityGroupCheck(entityGroup);

        setEntities(entityGroupName, Collections.singletonList(entity.getName()));
        deleteEntities(entityGroupName, Collections.singletonList(entity.getName()), token);
        Response response = getEntities(entityGroupName);
        assertEquals("Failed to delete entities from entity group using tokens", "[]", response.readEntity(String.class));
    }
}
