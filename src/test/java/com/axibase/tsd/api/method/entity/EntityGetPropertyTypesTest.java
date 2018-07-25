package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.*;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGetPropertyTypesTest extends EntityMethod {


    @Issue("1278")
    @Test
    public void testEntityNameContainsWhitespace() {
        final String name = "get_property_types_entity 1";
        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), queryEntityPropertyTypes(name).getStatus());
    }


    @Issue("1278")
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("get_property_types_/entity-2");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(entity);

    }

    @Issue("1278")
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("get_property_types_йёentity-3");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(entity);
    }

    private void assertUrlencodedPathHandledSuccessfullyOnGetPropertyTypes(final Entity entity) throws Exception {
        Response response = queryEntityPropertyTypes(entity.getName());
        assertSame("Fail to execute queryEntityGroups", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("List of entity groups should be empty", compareJsonString("[]", response.readEntity(String.class)));
    }
}
