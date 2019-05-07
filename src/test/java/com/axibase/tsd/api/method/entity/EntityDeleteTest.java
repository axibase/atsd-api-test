package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.*;


public class EntityDeleteTest extends EntityMethod {


    @Issue("1278")
    @Test
    public void testEntityNameContainsWhitespace() {
        final String name = "deleteentity 1";
        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), deleteEntity(name).getStatus());
    }

    @Issue("1278")
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        final Entity entity = new Entity("deleteentity/2");
        createOrReplaceEntityCheck(entity);

        assertSame("Fail to execute deleteEntity query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(deleteEntity(entity.getName())));
        assertFalse("Entity should be deleted", entityExist(entity));

    }

    @Issue("1278")
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("deleteйёentity3");
        createOrReplaceEntityCheck(entity);

        assertSame("Fail to execute deleteEntity query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(deleteEntity(entity.getName())));
        assertFalse("Entity should be deleted", entityExist(entity));
    }

}
