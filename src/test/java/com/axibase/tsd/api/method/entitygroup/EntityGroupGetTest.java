package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupGetTest extends EntityGroupMethod {

    /**
     * #1278
     */
    @Test
    public void testNameContainsWhitespace() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeget entitygroup1");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsSlash() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeget/entitygroup2");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsCyrillic() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodegetйёentitygroup3");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    public void assertUrlEncodePathHandledCorrectly(final EntityGroup entityGroup) throws Exception {
        createOrReplaceEntityGroupCheck(entityGroup);
        Response response = getEntityGroup(entityGroup.getName());
        assertEquals("Fail to execute getEntityGroup query", OK.getStatusCode(), response.getStatus());
        assertTrue("Entity group in response does not match to inserted", compareJsonString(jacksonMapper.writeValueAsString(entityGroup), response.readEntity(String.class)));
    }
}
