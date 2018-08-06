package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.*;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupUpdateTest extends EntityGroupMethod {

    @Issue("1278")
    @Test
    public void testNameContainsWhitespace() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeupdate entitygroup1");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    @Issue("1278")
    @Test
    public void testNameContainsSlash() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeupdate/entitygroup2");
        assertUrlEncodePathHandledCorrectly(entityGroup);

    }

    @Issue("1278")
    @Test
    public void testNameContainsCyrillic() throws Exception {
        EntityGroup entityGroup = new EntityGroup("urlencodeupdateйёentitygroup3");
        assertUrlEncodePathHandledCorrectly(entityGroup);
    }

    @Issue("3301")
    @Test
    public void testCanSetEmptyExpression() throws Exception {
        EntityGroup entityGroup = new EntityGroup("update-entitygroup-4");
        entityGroup.setExpression(SYNTAX_ALLOWED_ENTITYGROUP_EXPRESSION);
        createOrReplaceEntityGroupCheck(entityGroup);

        entityGroup.setExpression(" ");

        assertSame("Fail to execute updateEntityGroup query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(updateEntityGroup(entityGroup)));
        entityGroup.setExpression(entityGroup.getExpression().trim());
        assertTrue("Specified entityGroup does not exist", entityGroupExist(entityGroup));
    }

    @Issue("3301")
    @Test(enabled = false) //TODO wait for solution about tag matcher
    public void testCanSetEmptyTags() throws Exception {
        EntityGroup entityGroup = new EntityGroup("update-entitygroup-5");
        entityGroup.addTag("tagName", "tagValue");
        createOrReplaceEntityGroupCheck(entityGroup);

        entityGroup.setTags(null);
        entityGroup.addTag("*", "");

        assertSame("Fail to execute updateEntityGroup query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(updateEntityGroup(entityGroup)));

        entityGroup.setTags(null);
        assertTrue("Specified entityGroup should not have any tag", entityGroupExist(entityGroup));
    }



    public void assertUrlEncodePathHandledCorrectly(final EntityGroup entityGroup) throws Exception {
        entityGroup.addTag("oldtag1", "oldtagvalue1");
        createOrReplaceEntityGroupCheck(entityGroup);

        EntityGroup updatedEntityGroup = new EntityGroup();
        updatedEntityGroup.setName(entityGroup.getName());
        updatedEntityGroup.addTag("oldtag1", "newtagvalue1");
        updatedEntityGroup.setEnabled(true);

        if (entityGroupExist(updatedEntityGroup)) {
            throw new IllegalArgumentException("Updated entity group should not exist before execution of updateEntityGroup query");
        }
        Response response = updateEntityGroup(updatedEntityGroup);
        assertSame("Fail to execute updateEntityGroup query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Updated entityGroup should exist", entityGroupExist(updatedEntityGroup));
        assertFalse("Old entityGroup should not exist", entityGroupExist(entityGroup));
    }
}
