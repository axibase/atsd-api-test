
package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.model.entity.Entity;
import org.junit.Test;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.Assert.assertEquals;

public class EntityCommandTest extends EntityMethod {

    /**
     * Issue #3111
     */

    @Test
    public void testAddNewEntityTagsForNewEntity() throws Exception {
        Entity EntityNameForTags = new Entity("e-for-test-add-tags");
        createOrReplaceEntity(EntityNameForTags);
        EntityNameForTags.addTag("e-tag-1", "e-val-1");
        createOrReplaceEntity(EntityNameForTags);

        Response EntityNameForTagsGet = getEntity("e-for-test-add-tags");
        Entity EntityNameForTagsGetToEnt = EntityNameForTagsGet.readEntity(Entity.class);

        assertEquals("Method should fail if new entity tags don't add for existing entity ",EntityNameForTags.getTags(), EntityNameForTagsGetToEnt.getTags());

    }

    /**
     * Issue #3111
     */

    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {
        Entity EntityNameForTags = new Entity("e-for-test-update-tags");
        EntityNameForTags.addTag("e-tag-1", "e-val-1");
        createOrReplaceEntity(EntityNameForTags);
        EntityNameForTags.addTag("e-tag-2", "e-val-2");
        createOrReplaceEntity(EntityNameForTags);

        Response EntityNameForTagsGet = getEntity("e-for-test-update-tags");
        Entity EntityNameForTagsGetToEnt = EntityNameForTagsGet.readEntity(Entity.class);

        assertEquals("Entity tags are not updated.",EntityNameForTags.getTags(), EntityNameForTagsGetToEnt.getTags());

    }

    /**
     * Issue #3111
     */

    @Test
    public void testNewEntityTagsForExistEntity() throws Exception {
        Entity EntityNameForTags = new Entity("ent-for-test-add-tags");
        EntityNameForTags.addTag("e-tag-1", "e-val-1");
        createOrReplaceEntity(EntityNameForTags);

        Response EntityNameForTagsGet = getEntity("ent-for-test-add-tags");
        Entity EntityNameForTagsGetToEnt = EntityNameForTagsGet.readEntity(Entity.class);

        assertEquals("Method should fail if new entity tags don't create",EntityNameForTags.getTags(), EntityNameForTagsGetToEnt.getTags());

    }

    /**
     * Issue #3111
     */

    @Test
    public void testAddNewEntityTagsMailformedForNewEntity() throws Exception {
        Entity EntityNameForTags = new Entity("ent-for-test-add-tags-mailformed");
        EntityNameForTags.addTag("hello 1", "world");
        assertEquals("EntityTag name contains whitespace. Insert Entity tag is failed.", BAD_REQUEST.getStatusCode(), createOrReplaceEntity(EntityNameForTags).getStatus());
    }
}



