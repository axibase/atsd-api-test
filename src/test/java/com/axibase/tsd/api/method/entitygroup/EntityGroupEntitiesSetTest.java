package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.*;

import static com.axibase.tsd.api.util.ErrorTemplate.CANNOT_MODIFY_ENTITY_TPL;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupEntitiesSetTest extends EntityGroupMethod {

    @Issue("1278")
    @Test
    public void testNameContainsWhitespace() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodesetentities entitygroup1");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    @Issue("1278")
    @Test
    public void testNameContainsSlash() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodesetentities/entitygroup2");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    @Issue("1278")
    @Test
    public void testNameContainsCyrillic() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodesetentitiesйёentitygroup3");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    @Issue("3041")
    @Test
    public void testUnableModifyEntitiesWhileExpressionNotEmpty() throws Exception {
        EntityGroup entityGroup = new EntityGroup("setentities-entitygroup-4");
        entityGroup.setExpression(SYNTAX_ALLOWED_ENTITYGROUP_EXPRESSION);
        createOrReplaceEntityGroupCheck(entityGroup);

        Response response = setEntities(entityGroup.getName(), Collections.singletonList("test-entity"));
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

        final String expected = String.format(CANNOT_MODIFY_ENTITY_TPL, entityGroup.getName());
        final String actual = extractErrorMessage(response);
        assertEquals("Error does not match", expected, actual);
    }

    public void assertUrlEncodePathHandledCorrectly(final EntityGroup entityGroup) throws Exception {
        createOrReplaceEntityGroupCheck(entityGroup);

        List<String> entitiesDefault = Arrays.asList("entity1", "entity2");
        List<String> entitiesToSet = Arrays.asList("entity3", "entity4");
        deleteEntities(entityGroup.getName(), entitiesToSet);

        List<Map> expectedResponse = new ArrayList<>();
        for (String s : entitiesDefault) {
            Map<String, Object> element = new HashMap<>();
            element.put("name", s);
            element.put("enabled", true);
            expectedResponse.add(element);
        }

        Response response = addEntities(entityGroup.getName(), entitiesDefault);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute addEntities query");
        }

        String expected = jacksonMapper.writeValueAsString(expectedResponse);
        response = getEntities(entityGroup.getName());
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalArgumentException("Fail to execute getEntities query");
        }
        if (!compareJsonString(expected, response.readEntity(String.class))) {
            throw new IllegalStateException("Fail to get added entities");
        }

        expectedResponse = new ArrayList<>();
        for (String s : entitiesToSet) {
            Map<String, Object> element = new HashMap<>();
            element.put("name", s);
            element.put("enabled", true);
            expectedResponse.add(element);
        }

        response = setEntities(entityGroup.getName(), entitiesToSet);
        assertSame("Fail to execute deleteEntities query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));

        expected = jacksonMapper.writeValueAsString(expectedResponse);
        response = getEntities(entityGroup.getName());
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalArgumentException("Fail to execute getEntities query");
        }
        if (!compareJsonString(expected, response.readEntity(String.class))) {
            throw new IllegalStateException("Fail to get added entities");
        }
    }
}
