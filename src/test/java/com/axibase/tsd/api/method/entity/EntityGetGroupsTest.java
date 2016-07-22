package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGetGroupsTest extends EntityMethod {


    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        final String name = "getgroupsentity 1";
        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), queryEntityGroups(name).getStatus());
    }


    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("getgroups/entity-2");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetGroups(entity);

    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("getgroupsйёentity-3");
        createOrReplaceEntityCheck(entity);
        assertUrlencodedPathHandledSuccessfullyOnGetGroups(entity);
    }

    private void assertUrlencodedPathHandledSuccessfullyOnGetGroups(final Entity entity) throws Exception {
        Response response = queryEntityGroups(entity.getName());
        assertEquals("Fail to execute queryEntityGroups", OK.getStatusCode(), response.getStatus());
        assertTrue("Entity groups should be empty", compareJsonString("[]", formatToJsonString(response)));
    }
}
