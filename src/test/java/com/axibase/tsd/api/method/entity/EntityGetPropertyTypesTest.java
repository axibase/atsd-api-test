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


    @Test //1278
    public void testURLGetPropertyTypesWhitespace() throws Exception {
        Entity entity = new Entity("get_property_types_entity 1");
        assertEquals(BAD_REQUEST.getStatusCode(), getPropertyTypes(entity.getName()).getStatus());
    }


    @Test //1278
    public void testURLGetPropertyTypesSlash() throws Exception {
        Entity entity = new Entity("get_property_types_/entity-2");
        createOrReplaceEntityCheck(entity);
        checkUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(entity);

    }

    @Test //1278
    public void testURLGetPropertyTypesCyrillic() throws Exception {
        Entity entity = new Entity("get_property_types_йёentity-3");
        createOrReplaceEntityCheck(entity);
        checkUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(entity);
    }

    private void checkUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(final Entity entity) throws Exception {
        Response response = getGroups(entity.getName());
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertTrue(compareJsonString("[]", formatToJsonString(response)));
    }
}
