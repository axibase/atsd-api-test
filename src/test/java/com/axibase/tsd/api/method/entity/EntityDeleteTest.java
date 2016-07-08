package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityDeleteTest extends EntityMethod {


    @Test //1278
    public void testURLDeleteWhitespace() throws Exception {
        Entity entity = new Entity("deleteentity 1");
        assertEquals(BAD_REQUEST.getStatusCode(), deleteEntity(entity.getName()).getStatus());
    }

    @Test //1278
    public void testURLDeleteSlash() throws Exception {
        final Entity entity = new Entity("deleteentity/2");
        createOrReplaceEntityCheck(entity);

        assertEquals(OK.getStatusCode(), deleteEntity(entity.getName()).getStatus());
        assertFalse(entityExist(entity));

    }

    @Test //1278
    public void testURLDeleteCyrillic() throws Exception {
        Entity entity = new Entity("deleteйёentity3");
        createOrReplaceEntityCheck(entity);

        assertEquals(OK.getStatusCode(), deleteEntity(entity.getName()).getStatus());
        assertFalse(entityExist(entity));
    }

}
