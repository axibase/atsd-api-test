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
public class EntityGetPropertyTypesTest extends EntityMethod {


    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("get_property_types_entity 1");
        assertEquals(BAD_REQUEST.getStatusCode(), queryEntityPropertyTypes(entity.getName()).getStatus());
    }


    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("get_property_types_/entity-2");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(entity);

    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("get_property_types_йёentity-3");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(entity);
    }

    private void assertUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(final Entity entity) throws Exception {
        Response response = queryEntityGroups(entity.getName());
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertTrue(compareJsonString("[]", formatToJsonString(response)));
    }
}
