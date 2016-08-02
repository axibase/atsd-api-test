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

        Entity storedEntityWithTags = new Entity("e-with-tags");
        storedEntityWithTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityWithTags);
        
        String command = String.format("entity e:%s t:%s=%s",storedEntityWithTags.getName(),E_TAG_2,E_VAL_2);
        tcpSender.send(command);
        
        Entity expectedEntityWithTags = getEntity("e-with-tags").readEntity(Entity.class);
        storedEntityWithTags.addTag("e-tag-2", "e-val-2");

        assertEquals("Entity tag isn't add for existing entity",storedEntityWithTags.getTags(), expectedEntityWithTags.getTags());
    }

    /* #3111 */

    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {

        Entity storedEntityUpdateTags = new Entity("e-for-test-update-tags");
        storedEntityUpdateTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityUpdateTags);

        String command = String.format("entity e:%s t:%s=%s",storedEntityUpdateTags.getName(),E_TAG_1,E_VAL_1_UPD);
        tcpSender.send(command);

        Entity expectedEntityWithTags = getEntity("e-for-test-update-tags").readEntity(Entity.class);
        storedEntityUpdateTags.addTag(E_TAG_1, E_VAL_1_UPD);

        assertEquals("Entity tag isn't update for existing entity.",storedEntityUpdateTags.getTags(), expectedEntityWithTags.getTags());
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

        Entity storedEntityForTags = new Entity("e-for-test-add-tags");
        storedEntityForTags.addTag(E_TAG_1,E_VAL_1);

        Entity expectedEntityWithTags = getEntity("e-for-test-add-tags").readEntity(Entity.class);

        assertEquals("New entity with tag isn't create with entity tag",storedEntityForTags.getTags(), expectedEntityWithTags.getTags());
    }

}