package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.*;

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
        if (!compareJsonString(expected, formatToJsonString(response))) {
            throw new IllegalStateException("Fail to get added entities");
        }

        response = deleteEntities(entityGroup.getName(), entitiesList);
        assertEquals("Fail to execute deleteEntities query", OK.getStatusCode(), response.getStatus());

        response = getEntities(entityGroup.getName());
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Fail to execute getEntities query");
        }
        assertEquals("Entity list should be empty", "[]", formatToJsonString(response));
    }
}
