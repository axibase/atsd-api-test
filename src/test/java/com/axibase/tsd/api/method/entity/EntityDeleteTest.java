package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.junit.Test;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Dmitry Korchagin.
 */
public class EntityDeleteTest extends EntityMethod {


    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("deleteentity 1");
        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), deleteEntity(entity.getName()).getStatus());
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        final Entity entity = new Entity("deleteentity/2");
        createOrReplaceEntityCheck(entity);

        assertEquals("Fail to execute deleteEntity query", OK.getStatusCode(), deleteEntity(entity.getName()).getStatus());
        assertFalse("Entity should be deleted", entityExist(entity));

    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("deleteйёentity3");
        createOrReplaceEntityCheck(entity);

        assertEquals("Fail to execute deleteEntity query", OK.getStatusCode(), deleteEntity(entity.getName()).getStatus());
        assertFalse("Entity should be deleted", entityExist(entity));
    }

}
