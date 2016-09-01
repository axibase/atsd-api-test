package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */
public class EntityCreateOrReplaceTest extends EntityMethod {

    /**
     * #1278
     */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("createentity 1");

        assertEquals("Method should fail if entityName contains whitespace", BAD_REQUEST.getStatusCode(), createOrReplaceEntity(entity).getStatus());
    }

    /**
     * #1278
     */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        Entity entity = new Entity("createentity/2");

        assertEquals("Fail to execute createOrReplaceEntity query", OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue("Fail to get required entity", entityExist(entity));
    }

    /**
     * #1278
     */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        Entity entity = new Entity("createйёentity3");

        assertEquals("Fail to execute createOrReplaceEntity query", OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());
        assertTrue("Fail to get required entity", entityExist(entity));
    }

    /**
     * #1968
     */
    @Test
    public void testTagNameConvertedToLowerCase() throws Exception {
        Entity entity = new Entity("createentity4");
        entity.addTag("TagKey", "tagvalue");

        createOrReplaceEntity(entity);

        Entity savedEntity = getEntity(entity.getName()).readEntity(Entity.class);

        Map<String, String> formattedTags = new HashMap<>();
        for(Map.Entry<String, String> e: entity.getTags().entrySet()) {
            formattedTags.put(e.getKey().toLowerCase(), e.getValue());
        }

        assertEquals("Tags name should be converted to lower case", formattedTags, savedEntity.getTags());
    }

    /**
     * #1968
     */
    @Test
    public void testTagValueRetainCase() throws Exception {
        Entity entity = new Entity("createentity5");
        entity.addTag("tag-key", "TaValue");

        createOrReplaceEntity(entity);

        Entity savedEntity = getEntity(entity.getName()).readEntity(Entity.class);

        assertEquals("Tags Value should retain case", entity.getTags(), savedEntity.getTags());
    }

    /**
     * #1968
     */
    @Test
    public void testEntityNameConvertedToLowerCase() throws Exception {
        Entity entity = new Entity("CreateEntity6");

        createOrReplaceEntity(entity);

        Entity savedEntity = getEntity(entity.getName()).readEntity(Entity.class);

        assertEquals("Entity name should be converted to lower case", entity.getName().toLowerCase(), savedEntity.getName());
    }

    /**
     * #1968
     */
    @Test
    public void testUnknownFieldsRaiseError() throws Exception {
        final String entityName = "create-entity-7";
        final String unknownField = "unknownfield";
        Registry.Entity.register(entityName);

        Map<String, Object> insertQuery = new HashMap<>();

        insertQuery.put(unknownField, "value");

        Response response = createOrReplaceEntity(entityName, insertQuery);

        final String givenErrorMessage = extractErrorMessage(response);
        assertTrue("Fail to check error message", givenErrorMessage.startsWith(UNKNOWN_ENTITY_FIELD_ERROR_PREFIX));
    }

    /**
     * #1968
     */
    @Test
    public void testNoTagsReplaceExisting() throws Exception {
        Entity entity = new Entity("create-entity-8");
        entity.addTag("tagkey", "tagvalue");
        createOrReplaceEntityCheck(entity);

        entity.setTags(null);
        try {
            createOrReplaceEntityCheck(entity);
        } catch (IllegalStateException e) {
            fail("Fail to replace tags if new tags are absent");
        }
    }

    /**
     * #1968
     */
    @Test
    public void testEmptyTagsReplaceExisting() throws Exception {
        Entity entity = new Entity("create-entity-9");
        entity.addTag("tagkey", "tagvalue");
        createOrReplaceEntityCheck(entity);

        entity.setTags(new HashMap<String, String>());
        try {
            createOrReplaceEntityCheck(entity);
        } catch (IllegalStateException e) {
            fail("Fail to replace tags if new tags are empty");
        }
    }

    /**
     * #1968
     */
    @Test
    public void testAreplaceAF() throws Exception {
        Entity entity = new Entity("create-entity-10");
        entity.addTag("a", "c");
        entity.addTag("f", "g");
        createOrReplaceEntityCheck(entity);

        entity.setTags(null);
        entity.addTag("a", "b");
        createOrReplaceEntityCheck(entity);

        Entity storedEntity = getEntity(entity.getName()).readEntity(Entity.class);

        assertEquals("Stored tags are incorrect", entity.getTags(), storedEntity.getTags());
    }

    /**
     * #1968
     */
    @Test
    public void testNullTagValIgnored() throws Exception {
        Entity entity = new Entity("create-entity-11");
        entity.addTag("a", null);
        entity.addTag("b", "c");
        assertEquals("Fail to execute createOrReplaceEntity", OK.getStatusCode(), createOrReplaceEntity(entity).getStatus());

        Entity storedEntity = getEntity(entity.getName()).readEntity(Entity.class);
        Map<String, String> expectedTags = new HashMap<>();
        expectedTags.put("b", "c");

        assertEquals("Stored tags are incorrect", expectedTags, storedEntity.getTags());
    }

    /**
     * #1968
     */
    @Test
    public void testTagValBoolean() throws Exception {
        final String entityName = "create-entity-12";
        Registry.Entity.register(entityName);
        Map<String, Object> createOrReplaceEntityQuery = new HashMap<>();
        Map<String, Object> tags = new HashMap<>();
        tags.put("a", true);
        createOrReplaceEntityQuery.put("tags", tags);
        assertEquals("Fail to execute createOrReplaceEntity query", OK.getStatusCode(), createOrReplaceEntity(entityName, createOrReplaceEntityQuery).getStatus());

        Entity storedEntity = getEntity(entityName).readEntity(Entity.class);
        Map<String, String> expectedTags = new HashMap<>();
        expectedTags.put("a", "true");

        assertEquals("Stored tags are incorrect", expectedTags, storedEntity.getTags());
    }

    /**
     * #1968
     */
    @Test
    public void testTagValInteger() throws Exception {
        final String entityName = "create-entity-13";
        Registry.Entity.register(entityName);
        Map<String, Object> createOrReplaceEntityQuery = new HashMap<>();
        Map<String, Object> tags = new HashMap<>();
        tags.put("a", 123);
        createOrReplaceEntityQuery.put("tags", tags);
        assertEquals("Fail to execute createOrReplaceEntity query", OK.getStatusCode(), createOrReplaceEntity(entityName, createOrReplaceEntityQuery).getStatus());

        Entity storedEntity = getEntity(entityName).readEntity(Entity.class);
        Map<String, String> expectedTags = new HashMap<>();
        expectedTags.put("a", "123");

        assertEquals("Stored tags are incorrect", expectedTags, storedEntity.getTags());
    }

    /**
     * #1968
     */
    @Test
    public void testTagValBooleanInteger() throws Exception {
        final String entityName = "create-entity-14";
        Registry.Entity.register(entityName);
        Map<String, Object> createOrReplaceEntityQuery = new HashMap<>();
        Map<String, Object> tags = new HashMap<>();
        tags.put("a", 123);
        tags.put("b", true);
        createOrReplaceEntityQuery.put("tags", tags);
        assertEquals("Fail to execute createOrReplaceEntity query", OK.getStatusCode(), createOrReplaceEntity(entityName, createOrReplaceEntityQuery).getStatus());

        Entity storedEntity = getEntity(entityName).readEntity(Entity.class);
        Map<String, String> expectedTags = new HashMap<>();
        expectedTags.put("a", "123");
        expectedTags.put("b", "true");

        assertEquals("Stored tags are incorrect", expectedTags, storedEntity.getTags());
    }

    /**
     * #1968
     */
    @Test
    public void testTagValArrayRaiseError() throws Exception {
        final String entityName = "create-entity-15";
        Registry.Entity.register(entityName);
        Map<String, Object> createOrReplaceEntityQuery = new HashMap<>();
        Map<String, Object> tags = new HashMap<>();
        tags.put("a", "aval");
        tags.put("b", Arrays.asList("c", "d"));

        createOrReplaceEntityQuery.put("tags", tags);
        final Response response = createOrReplaceEntity(entityName, createOrReplaceEntityQuery);
        assertEquals("Request should be failed", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue("Error message mismatch", extractErrorMessage(response).startsWith(TAG_VALUE_ARRAY_ERROR_PREFIX));
        assertEquals("Entity should not be created", NOT_FOUND.getStatusCode(), getEntity(entityName).getStatus());

    }

    /**
     * #1968
     */
    @Test
    public void testTagValObjectRaiseError() throws Exception {
        final String entityName = "create-entity-16";
        Registry.Entity.register(entityName);
        Map<String, Object> createOrReplaceEntityQuery = new HashMap<>();
        Map<String, Object> tags = new HashMap<>();
        Map<String, String> tagValue = new HashMap<>();
        tagValue.put("c", "d");
        tags.put("a", "aval");
        tags.put("b", tagValue);

        createOrReplaceEntityQuery.put("tags", tags);
        final Response response = createOrReplaceEntity(entityName, createOrReplaceEntityQuery);
        assertEquals("Request should be failed", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue("Error message mismatch", extractErrorMessage(response).startsWith(TAG_VALUE_ARRAY_ERROR_PREFIX));
        assertEquals("Entity should not be created", NOT_FOUND.getStatusCode(), getEntity(entityName).getStatus());
    }

    /**
     * #1968
     */
    @Test
    public void testSeriesRemain() throws Exception {
        Series series = new Series("create-entity-17","create-entity-metric-17");
        series.addData(new Sample(MIN_STORABLE_DATE, 0));
        SeriesMethod.insertSeriesCheck(series);

        Entity entity = new Entity();
        entity.setName(series.getEntity());
        createOrReplaceEntityCheck(entity);

        assertTrue("Inserted Series should remain", SeriesMethod.seriesListIsInserted(Collections.singletonList(series)));
    }
}
