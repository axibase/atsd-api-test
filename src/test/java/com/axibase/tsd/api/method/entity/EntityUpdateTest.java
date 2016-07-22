package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.testng.annotations.Test;


import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityUpdateTest extends EntityMethod {


    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("updateentity 1");
        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), updateEntity(entity).getStatus());
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        final Entity entity = new Entity("update_entity/2");
        entity.addTag("t1", "tv1");
        createOrReplaceEntityCheck(entity);

        Map<String, String> newTags = new HashMap<>();
        newTags.put("t2", "tv2");
        assertUrlencodedPathHandledSuccessfullyOnUpdate(entity, newTags);
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("update_йёentity3");
        entity.addTag("t1", "tv1");
        createOrReplaceEntityCheck(entity);

        Map<String, String> newTags = new HashMap<>();
        newTags.put("t2", "tv2");
        assertUrlencodedPathHandledSuccessfullyOnUpdate(entity, newTags);
    }

    private void assertUrlencodedPathHandledSuccessfullyOnUpdate(final Entity entity, Map newTags) throws Exception {
        entity.setTags(newTags);
        assertEquals("Fail to execute updateEntity", OK.getStatusCode(), updateEntity(entity).getStatus());
        assertTrue("Entity in response does not match to updated entity", entityExist(entity));
    }
}
