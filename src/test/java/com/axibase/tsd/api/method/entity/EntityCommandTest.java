package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.entity.Entity;
import org.testng.annotations.Test;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.assertEquals;

public class EntityCommandTest extends EntityMethod {
    private final static String E_TAG_1 = "e-tag-1";
    private final static String E_TAG_2 = "e-tag-2";
    private final static String E_VAL_1 = "e-val-1";
    private final static String E_VAL_1_UPD = "e-val-1-upd";
    private final static String E_VAL_2 = "e-val-2";

    /**
     * #3111
     */

    @Test
    public void testAddNewEntityTagForExistEntity() throws Exception {
        String entityWithTags = "e-with-tags";

        Entity storedEntityWithTags = new Entity(entityWithTags);
        storedEntityWithTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityWithTags);

        String command = String.format("entity e:%s t:%s=%s", storedEntityWithTags.getName(), E_TAG_2, E_VAL_2);
        tcpSender.sendCheck(command);

        storedEntityWithTags.addTag(E_TAG_2, E_VAL_2);

        assertEquals("Entity tag isn't add for existing entity",
                storedEntityWithTags.getTags(), getEntity(entityWithTags).readEntity(Entity.class).getTags()
        );
    }

    /**
     * #3111
     */

    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {
        String entityForTestUpdateTags = "e-for-test-update-tags";

        Entity storedEntityUpdateTags = new Entity(entityForTestUpdateTags);
        storedEntityUpdateTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityUpdateTags);

        String command = String.format("entity e:%s t:%s=%s", storedEntityUpdateTags.getName(), E_TAG_1, E_VAL_1_UPD);
        tcpSender.sendCheck(command);

        storedEntityUpdateTags.addTag(E_TAG_1, E_VAL_1_UPD);

        assertEquals("Entity tag isn't update for existing entity.",
                storedEntityUpdateTags.getTags(), getEntity(entityForTestUpdateTags).readEntity(Entity.class).getTags()
        );
    }

    /**
     * #3111
     */

    @Test
    public void testAddNewEntityTagsMailformedForNewEntity() throws Exception {
        String entityNameForTestMailformed = "ent-for-test-add-tags-mailformed";
        String entityTagNameForMailformed = "hello 1";
        String entityTagValueForMailformed = "world";

        String command = String.format("entity e:%s t:%s=%s", entityNameForTestMailformed, entityTagNameForMailformed, entityTagValueForMailformed);
        tcpSender.send(command);

        Registry.Entity.register(entityNameForTestMailformed);

        assertEquals("Entity not found with mailformed ",
                NOT_FOUND.getStatusCode(), getEntity(entityNameForTestMailformed).getStatus()
        );
    }

    /**
     * #3111
     */

    @Test
    public void testNewEntityTagsForNewEntity() throws Exception {
        String entityNameForTestAddTags = "e-for-test-add-tags";

        String command = String.format("entity e:%s t:%s=%s", entityNameForTestAddTags, E_TAG_1, E_VAL_1);

        tcpSender.sendCheck(command, Util.EXPECTED_PROCESSING_TIME);

        Entity storedEntityForTags = new Entity(entityNameForTestAddTags);
        storedEntityForTags.addTag(E_TAG_1, E_VAL_1);

        assertEquals("New entity with tag isn't create with entity tag", storedEntityForTags.getTags(),
                getEntity(entityNameForTestAddTags).readEntity(Entity.class).getTags()
        );
    }

}