package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.*;

import static com.axibase.tsd.api.util.ErrorTemplate.CANNOT_MODIFY_ENTITY_TPL;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupEntitiesDeleteTest extends EntityGroupMethod {

    /**
     * #1278
     */
    @Test
    public void testNameContainsWhitespace() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodedelentities entitygroup1");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsSlash() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodedelentities/entitygroup2");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsCyrillic() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodedelentitiesйёentitygroup3");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #3041
     */
    @Test
    public void testUnableMofifyEntitiesWhileExpressionNotEmpty() throws Exception {
        EntityGroup entityGroup = new EntityGroup("deleteentities-entitygroup-4");
        entityGroup.setExpression(SYNTAX_ALLOWED_ENTITYGROUP_EXPRESSION);
        createOrReplaceEntityGroupCheck(entityGroup);

        Response response = deleteEntities(entityGroup.getName(), Collections.singletonList("test-entity"));
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

        final String expected = String.format(CANNOT_MODIFY_ENTITY_TPL, entityGroup.getName());
        final String actual = extractErrorMessage(response);
        assertEquals(expected, actual);
    }

    public void assertUrlEncodePathHandledCorrectly(final EntityGroup entityGroup) throws Exception {
        createOrReplaceEntityGroupCheck(entityGroup);

        List<String> entitiesList = Arrays.asList("entity1", "entity2");
        List<Map> expectedResponse = new ArrayList<>();
        for (String s : entitiesList) {
            Map<String, Object> element = new HashMap<>();
            element.put("name", s);
            element.put("enabled", true);
            expectedResponse.add(element);
        }

        Response response = addEntities(entityGroup.getName(), entitiesList);
        if (OK.getStatusCode() != response.getStatus()) {
            throw new IllegalStateException("Fail to execute addEntities query");
        }

        String expected = jacksonMapper.writeValueAsString(expectedResponse);
        response = getEntities(entityGroup.getName());
        if (OK.getStatusCode() != response.getStatus()) {
            throw new IllegalArgumentException("Fail to execute getEntities query");
        }
        if (!compareJsonString(expected, response.readEntity(String.class))) {
            throw new IllegalStateException("Fail to get added entities");
        }

        response = deleteEntities(entityGroup.getName(), entitiesList);
        assertEquals("Fail to execute deleteEntities query", OK.getStatusCode(), response.getStatus());

        response = getEntities(entityGroup.getName());
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Fail to execute getEntities query");
        }
        assertEquals("Entity list should be empty", "[]", response.readEntity(String.class));
    }
}
