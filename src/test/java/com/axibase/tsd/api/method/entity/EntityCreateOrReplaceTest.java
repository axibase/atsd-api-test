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

    @Test //1278
    public void testURLCreateOrReplaceWhitespace() throws Exception {
        Entity entity = new Entity("createentity 1");

        assertEquals(BAD_REQUEST.getStatusCode(), createOrReplaceEntity(entity).getStatus());
    }

    @Test //1278
    public void testURLCreateOrReplaceSlash() throws Exception {
        Entity entity = new Entity("createentity/2");

        assertEquals(OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue(entityExist(entity));
    }

    @Test //1278
    public void testURLCreateOrReplaceCyrillic() throws Exception {
        Entity entity = new Entity("createйёentity3");

        assertEquals(OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue(entityExist(entity));
    }
}
