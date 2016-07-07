package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.junit.Test;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityCreateOrReplaceTest extends EntityMethod {

    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("createentity 1");

        assertEquals(BAD_REQUEST.getStatusCode(), createOrReplaceEntity(entity).getStatus());
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("createentity/2");

        assertEquals(OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue(entityExist(entity));
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("createйёentity3");

        assertEquals(OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue(entityExist(entity));
    }
}
