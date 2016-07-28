package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.entity.Entity;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.assertEquals;

public class EntityCommandTest extends EntityMethod{
    private String e_tag_1="e-tag-1";
    private String e_tag_2="e-tag-2";
    private String e_val_1="e-val-1";
    private String e_val_1_upd="e-val-1-upd";
    private String e_val_2="e-val-2";
    //entityExist(EntityNameForTags,true);
    /**
     * Issue #3111
     */

    @Test
    public void testAddNewEntityTagForExistEntity() throws Exception {
        //Create Entity with tags
        Entity EntityWithTags = new Entity("e-with-tags");
        EntityWithTags.addTag(e_tag_1, e_val_1);
        createOrReplaceEntityCheck(EntityWithTags);
        //Add entity tag for exist entity
        StringBuilder sb = new StringBuilder("entity");
        sb.append(" e:").append(EntityWithTags.getName());
        sb.append(" t:").append(e_tag_2).append("=").append(e_val_2);
        tcpSender.send(sb.toString());
        //Add entity tag to model
        EntityWithTags.addTag("e-tag-2", "e-val-2");
        //Get from ATSD entity model
        Response EntityNameForTagsGet = getEntity("e-with-tags");
        Entity EntityNameForTagsGetToEnt = EntityNameForTagsGet.readEntity(Entity.class);
        //Equal models
        assertEquals("Entity tag was added for exist entity",EntityWithTags.getTags(), EntityNameForTagsGetToEnt.getTags());
    }

    /**
     * Issue #3111
     */

    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {
        //Create Entity with tags
        Entity EntityNameForTags = new Entity("e-for-test-update-tags");
        EntityNameForTags.addTag(e_tag_1, e_val_1);
        createOrReplaceEntityCheck(EntityNameForTags);
        //Update entity tag for exist entity
        StringBuilder sb = new StringBuilder("entity");
        sb.append(" e:").append(EntityNameForTags.getName());
        sb.append(" t:").append(e_tag_1).append("=").append(e_val_1_upd);
        tcpSender.send(sb.toString());
        //Update entity tag to model
        EntityNameForTags.addTag(e_tag_1, e_val_1_upd);
        //Get from ATSD entity model
        Response EntityNameForTagsGet = getEntity("e-for-test-update-tags");
        Entity EntityNameForTagsGetToEnt = EntityNameForTagsGet.readEntity(Entity.class);
        //Equal models
        assertEquals("Entity tag are updated.",EntityNameForTags.getTags(), EntityNameForTagsGetToEnt.getTags());
    }

    /**
     * Issue #3111
     */

    @Test
    public void testNewEntityTagsForNewEntity() throws Exception {
        //Create entity with tag
        StringBuilder sb = new StringBuilder("entity");
        sb.append(" e:").append("ent-for-test-add-tags");
        sb.append(" t:").append(e_tag_1).append("=").append(e_val_1);
        tcpSender.send(sb.toString());
        //Create entity model with tag
        Entity EntityNameForTags = new Entity("ent-for-test-add-tags");
        EntityNameForTags.addTag(e_tag_1,e_val_1);
        //Get from ATSD entity model
        Response EntityNameForTagsGet = getEntity("ent-for-test-add-tags");
        Entity EntityNameForTagsGetToEnt = EntityNameForTagsGet.readEntity(Entity.class);
        //Equal models
        assertEquals("New entity with tag create with tag",EntityNameForTags.getTags(), EntityNameForTagsGetToEnt.getTags());
    }

    /**
     * Issue #3111
     */

    @Test
    public void testAddNewEntityTagsMailformedForNewEntity() throws Exception {
        //Create entity with malformed tag
        StringBuilder sb = new StringBuilder("entity");
        sb.append(" e:").append("ent-for-test-add-tags-mailformed");
        sb.append(" t:").append("hello 1").append("=").append("world");
        tcpSender.send(sb.toString());
        final String name = "ent-for-test-add-tags-mailformed";
        Registry.Entity.register(name);
        //Equal error
        assertEquals("Entity not found with ",NOT_FOUND.getStatusCode(),getEntity(name).getStatus());
    }

}