
        package com.axibase.tsd.api.method.entity;

        import com.axibase.tsd.api.Registry;
        import com.axibase.tsd.api.model.entity.Entity;
        import org.junit.Test;

        import javax.ws.rs.core.EntityTag;
        import javax.ws.rs.core.Response;
        import javax.ws.rs.core.StreamingOutput;

        import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
        import static javax.ws.rs.core.Response.Status.OK;
        import static org.junit.Assert.assertEquals;
        import static org.junit.Assert.assertTrue;

public class EntityCommandTest extends EntityMethod {

    @Test
    public void testAddNewEntityTagsForNewEntity() throws Exception {
        Entity entity_name_for_tags = new Entity("e-for-test-add-tags");
        createOrReplaceEntity(entity_name_for_tags);
        entity_name_for_tags.addTag("e-tag-1", "e-val-1");
        createOrReplaceEntity(entity_name_for_tags);

        Response entity_name_for_tags_get = getEntity("e-for-test-add-tags");
        Entity entity_name_for_tags_get_to_ent = entity_name_for_tags_get.readEntity(Entity.class);

        assertEquals("Method should fail if new entity tags don't add for existing entity ",entity_name_for_tags.getTags(), entity_name_for_tags_get_to_ent.getTags());

    }

    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {
        Entity entity_name_for_tags = new Entity("e-for-test-update-tags");
        entity_name_for_tags.addTag("e-tag-1", "e-val-1");
        createOrReplaceEntity(entity_name_for_tags);
        entity_name_for_tags.addTag("e-tag-2", "e-val-2");
        createOrReplaceEntity(entity_name_for_tags);

        Response entity_name_for_tags_get = getEntity("e-for-test-update-tags");
        Entity entity_name_for_tags_get_to_ent = entity_name_for_tags_get.readEntity(Entity.class);

        assertEquals("Method should fail if existing entity tags don't updated",entity_name_for_tags.getTags(), entity_name_for_tags_get_to_ent.getTags());

    }

    //test
    @Test
    public void testNewEntityTagsForExistEntity() throws Exception {
        Entity entity_name_for_tags = new Entity("ent-for-test-add-tags");
        entity_name_for_tags.addTag("e-tag-1", "e-val-1");
        createOrReplaceEntity(entity_name_for_tags);

        Response entity_name_for_tags_get = getEntity("ent-for-test-add-tags");
        Entity entity_name_for_tags_get_to_ent = entity_name_for_tags_get.readEntity(Entity.class);

        assertEquals("Method should fail if new entity tags don't create",entity_name_for_tags.getTags(), entity_name_for_tags_get_to_ent.getTags());

    }

    @Test
    public void testAddNewEntityTagsMailformedForNewEntity() throws Exception {
        Entity entity_name_for_tags = new Entity("ent-for-test-add-tags-mailformed");
        entity_name_for_tags.addTag("hello 1", "world");
        assertEquals("Method should fail if entityTag name contains whitespace", BAD_REQUEST.getStatusCode(), createOrReplaceEntity(entity_name_for_tags).getStatus());
    }
}



