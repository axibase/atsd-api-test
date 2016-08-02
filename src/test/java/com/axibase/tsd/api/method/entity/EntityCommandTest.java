package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.assertEquals;

public class EntityCommandTest extends EntityMethod{
    private static String E_TAG_1="e-tag-1";
    private static String E_TAG_2="e-tag-2";
    private static String E_VAL_1="e-val-1";
    private static String E_VAL_1_UPD="e-val-1-upd";
    private static String E_VAL_2="e-val-2";

    /* #3111 */

    @Test
    public void testAddNewEntityTagForExistEntity() throws Exception {

        Entity entityStoredWithTags = new Entity("e-with-tags");
        entityStoredWithTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(entityStoredWithTags);
        
        String command = String.format("entity e:%s t:%s=%s",entityStoredWithTags.getName(),E_TAG_2,E_VAL_2);
        tcpSender.send(command);
        
        Entity entityExpectedWithTags = getEntity("e-with-tags").readEntity(Entity.class);
        entityStoredWithTags.addTag("e-tag-2", "e-val-2");

        assertEquals("Entity tag didn't added for exist entity",entityStoredWithTags.getTags(), entityExpectedWithTags.getTags());
    }

    /* #3111 */

    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {

        Entity entityStoredUpdateTags = new Entity("e-for-test-update-tags");
        entityStoredUpdateTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(entityStoredUpdateTags);

        String command = String.format("entity e:%s t:%s=%s",entityStoredUpdateTags.getName(),E_TAG_1,E_VAL_1_UPD);
        tcpSender.send(command);

        Entity entityExpectedWithTags = getEntity("e-for-test-update-tags").readEntity(Entity.class);
        entityStoredUpdateTags.addTag(E_TAG_1, E_VAL_1_UPD);

        assertEquals("Entity tag didn't updated for exist entity.",entityStoredUpdateTags.getTags(), entityExpectedWithTags.getTags());
    }

    /* #3111 */

    @Test
    public void testAddNewEntityTagsMailformedForNewEntity() throws Exception {

        String command = String.format("entity e:%s t:%s=%s","ent-for-test-add-tags-mailformed","hello 1","world");
        tcpSender.send(command);

        String name = "ent-for-test-add-tags-mailformed";
        Registry.Entity.register(name);

        assertEquals("Entity not found with mailformed ",NOT_FOUND.getStatusCode(),getEntity(name).getStatus());
    }

    /* #3111 */

    @Test
    public void testNewEntityTagsForNewEntity() throws Exception {

        String command = String.format("entity e:%s t:%s=%s","e-for-test-add-tags",E_TAG_1,E_VAL_1);
        tcpSender.send(command);

        Thread.sleep(500L);

        Entity entityStoredForTags = new Entity("e-for-test-add-tags");
        entityStoredForTags.addTag(E_TAG_1,E_VAL_1);

        Entity entityExpectedWithTags = getEntity("e-for-test-add-tags").readEntity(Entity.class);

        assertEquals("New entity with tag didn't created with entity tag",entityStoredForTags.getTags(), entityExpectedWithTags.getTags());
    }

}