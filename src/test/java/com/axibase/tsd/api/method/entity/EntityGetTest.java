package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.entity.Entity;
import org.testng.annotations.Test;


import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGetTest extends EntityMethod {

    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        final String name = "getentity 1";
        Registry.Entity.register(name);

        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), getEntity(name).getStatus());
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("getentity/2");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGet(entity);
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("getйёentity3");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGet(entity);
    }

    private void assertUrlencodedPathHandledSuccessfullyOnGet(final Entity entity) throws Exception {
        Response response = getEntity(entity.getName());
        assertEquals("Fail to execute getEntity", OK.getStatusCode(), response.getStatus());

        String expected = jacksonMapper.writeValueAsString(entity);
        assertTrue("Entity in response does not match to inserted entity", compareJsonString(expected, formatToJsonString(response)));
    }
}
