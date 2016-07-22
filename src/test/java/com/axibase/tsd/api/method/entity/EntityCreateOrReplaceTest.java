package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.testng.annotations.Test;


import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityCreateOrReplaceTest extends EntityMethod {

    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("createentity 1");

        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), createOrReplaceEntity(entity).getStatus());
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("createentity/2");

        assertEquals("Fail to execute createOrReplaceEntity query", OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue("Fail to get required entity", entityExist(entity));
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("createйёentity3");

        assertEquals("Fail to execute createOrReplaceEntity query", OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue("Fail to get required entity", entityExist(entity));
    }
}
