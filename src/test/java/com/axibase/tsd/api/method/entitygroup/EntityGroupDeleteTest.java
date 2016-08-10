package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupDeleteTest extends EntityGroupMethod {

    /**
     * #1278
     */
    @Test
    public void testNameContainsWhitespace() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodedelete entitygroup1");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsSlash() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodedelete/entitygroup2");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsCyrillic() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodedeleteйёentitygroup3");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    public void assertUrlEncodePathHandledCorrectly(final EntityGroup entityGroup) throws Exception {
        createOrReplaceEntityGroupCheck(entityGroup);
        Response response = deleteEntityGroup(entityGroup.getName());
        assertEquals("Fail to execute deleteEntityGroup query", OK.getStatusCode(), response.getStatus());
        assertFalse("Entity should be deleted", entityGroupExist(entityGroup));
    }
}
