package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGetTest extends EntityMethod {

    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("getentity 1");

        assertEquals(BAD_REQUEST.getStatusCode(), createOrReplaceEntity(entity).getStatus());
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
        assertEquals(OK.getStatusCode(), response.getStatus());

        String expected = jacksonMapper.writeValueAsString(entity);
        assertTrue(compareJsonString(expected, formatToJsonString(response)));
    }
}
