package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.entity.EntityTest.assertEntityExisting;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.assertEquals;

public class EntityGetTest extends EntityMethod {

    @Issue("1278")
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        final String name = "getentity 1";
        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), getEntityResponse(name).getStatus());
    }

    @Issue("1278")
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("getentity/2");
        createOrReplaceEntityCheck(entity);
        assertEntityExisting(entity);
    }

    @Issue("1278")
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("getйёentity3");
        createOrReplaceEntityCheck(entity);
        assertEntityExisting(entity);
    }

}
