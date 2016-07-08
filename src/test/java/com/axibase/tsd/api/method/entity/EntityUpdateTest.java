package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityUpdateTest extends EntityMethod {


    @Test //1278
    public void testURLUpdateWhitespace() throws Exception {
        Entity entity = new Entity("updateentity 1");
        assertEquals(BAD_REQUEST.getStatusCode(), updateEntity(entity).getStatus());
    }

    @Test //1278
    public void testURLUpdateSlash() throws Exception {
        final Entity entity = new Entity("update_entity/2");
        entity.addTag("t1", "tv1");
        createOrReplaceEntityCheck(entity);

        Map<String, String> newTags = new HashMap<>();
        newTags.put("t2", "tv2");
        checkUrlencodedPathHandledSuccessfullyOnUpdate(entity, newTags);
    }

    @Test //1278
    public void testURLUpdateCyrillic() throws Exception {
        Entity entity = new Entity("update_йёentity3");
        entity.addTag("t1", "tv1");
        createOrReplaceEntityCheck(entity);

        Map<String, String> newTags = new HashMap<>();
        newTags.put("t2", "tv2");
        checkUrlencodedPathHandledSuccessfullyOnUpdate(entity, newTags);
    }

    private void checkUrlencodedPathHandledSuccessfullyOnUpdate(final Entity entity, Map newTags) throws Exception {
        entity.setTags(newTags);
        assertEquals(OK.getStatusCode(), updateEntity(entity).getStatus());
        assertTrue(entityExist(entity));
    }
}
