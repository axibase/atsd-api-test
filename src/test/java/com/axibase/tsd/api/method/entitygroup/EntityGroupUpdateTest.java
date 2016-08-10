package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupUpdateTest extends EntityGroupMethod {

    /**
     * #1278
     */
    @Test
    public void testNameContainsWhitespace() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeupdate entitygroup1");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsSlash() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeupdate/entitygroup2");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    /**
     * #1278
     */
    @Test
    public void testNameContainsCyrillic() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeupdateйёentitygroup3");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    public void assertUrlEncodePathHandledCorrectly(final EntityGroup entityGroup) throws Exception {
        entityGroup.addTag("oldtag1", "oldtagvalue1");
        createOrReplaceEntityGroupCheck(entityGroup);

        EntityGroup updatedEntityGroup = new EntityGroup();
        updatedEntityGroup.setName(entityGroup.getName());
        updatedEntityGroup.addTag("oldtag1", "newtagvalue1");

        if (entityGroupExist(updatedEntityGroup)) {
            throw new IllegalArgumentException("Updated entity group should not exist before execution of updateEntityGroup query");
        }
        Response response = updateEntityGroup(updatedEntityGroup);
        assertEquals("Fail to execute updateEntityGroup query", OK.getStatusCode(), response.getStatus());
        assertTrue("Updated entityGroup should exists", entityGroupExist(updatedEntityGroup));
        assertFalse("Old entityGroup should not exists", entityGroupExist(entityGroup));
    }
}
