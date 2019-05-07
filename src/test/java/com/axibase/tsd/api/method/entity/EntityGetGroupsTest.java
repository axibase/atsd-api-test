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
public class EntityGetGroupsTest extends EntityMethod {


    @Issue("1278")
    @Test
    public void testEntityNameContainsWhitespace() {
        final String name = "getgroupsentity 1";
        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), queryEntityGroups(name).getStatus());
    }


    @Issue("1278")
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("getgroups/entity-2");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetGroups(entity);

    }

    @Issue("1278")
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("getgroupsйёentity-3");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetGroups(entity);
    }

    private void assertUrlencodedPathHandledSuccessfullyOnGetGroups(final Entity entity) throws Exception {
        Response response = queryEntityGroups(entity.getName());
        assertSame("Fail to execute queryEntityGroups", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Entity groups should be empty", compareJsonString("[]", response.readEntity(String.class)));
    }
}
