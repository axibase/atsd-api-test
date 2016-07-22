package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.property.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.*;

/**
 * @author Dmitry Korchagin.
 */
@SuppressWarnings("unchecked")
public class PropertyDeleteTest extends PropertyMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testFutureDateExactTrue() throws IOException {
        final Property property = new Property("delete-type9", "delete-entity9");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(Util.getNextDay());
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("exactMatch", true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue("Property should be remain", propertyExist(property));
    }


    @Test
    public void testFutureDateExactFalse() throws IOException {
        final Property property = new Property("delete-type8", "delete-entity8");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(Util.getNextDay());
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("exactMatch", false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testFutureDateExactDefault() throws IOException {
        final Property property = new Property("delete-type7", "delete-entity7");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(Util.getNextDay());
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testCommonTypeEntityTypeEntityKey() throws IOException {
        final Property property = new Property("delete-type-6", "delete-entity6");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Property secondProperty = new Property(null, "delete-entity6-2");
        secondProperty.setType(property.getType());
        secondProperty.setTags(property.getTags());
        secondProperty.addKey("k2", "v2");
        insertPropertyCheck(secondProperty);
        logger.info("secondProperty inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertFalse("First property should be deleted", propertyExist(property));
        assertTrue("Second property should remain", propertyExist(secondProperty));
    }


    @Test
    public void testTypeEntityExactFalse() throws IOException {
        final Property property = new Property("delete-type-5", "delete-entity5");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Property secondProperty = new Property();
        secondProperty.setType(property.getType());
        secondProperty.setEntity(property.getEntity());
        secondProperty.setTags(property.getTags());
        secondProperty.addKey("k2", "v2");
        insertPropertyCheck(secondProperty);
        logger.info("secondProperty inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("exactMatch", false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertFalse("Frist property should be deleted", propertyExist(property));
        assertFalse("Second property should be deleted", propertyExist(secondProperty));

    }


    @Test
    public void testTypeEntityKey() throws IOException {
        final Property property = new Property("delete-type4", "delete-entity4");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testMultipleTypeEntityExactTrue() throws IOException {
        final Property property = new Property("delete-type3", "delete-entity3");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Property secondProperty = new Property();
        secondProperty.setType(property.getType());
        secondProperty.setEntity(property.getEntity());
        secondProperty.addKey("k2", "v2");
        secondProperty.setTags(property.getTags());
        insertPropertyCheck(secondProperty);
        logger.info("Second property inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("exactMatch", true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue("First property should remain", propertyExist(property));
        assertTrue("Second property should remain", propertyExist(secondProperty));
    }


    @Test
    public void testTypeEntityExactTrue() throws IOException {
        final Property property = new Property("delete-type2", "delete-entity2");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("entity", property.getEntity());
        queryObj.put("type", property.getType());
        queryObj.put("exactMatch", true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue("Property should be remain", propertyExist(property));
    }

    @Test
    public void testEndDateEqDate() throws IOException {
        final Property property = new Property("delete-type1", "delete-entity1");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(System.currentTimeMillis());
        insertPropertyCheck(property);
        logger.info("Property inserted");

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("entity", property.getEntity());
        queryObj.put("type", property.getType());
        queryObj.put("key", property.getKey());
        queryObj.put("endDate", property.getDate());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue("Property should be remain", propertyExist(property));
    }

    @Test
    public void testTypeStartEnd() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "testtype");
        request.put("startDate", "2016-06-01T12:04:59.191Z");
        request.put("endDate", "2016-06-01T12:04:59.191Z");


        Response response = deleteProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Entity is required\"}", formatToJsonString(response));
    }

    @Test
    public void testTypeEnd() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "testtype");
        request.put("endDate", "2016-06-01T12:04:59.191Z");

        Response response = deleteProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Entity is required\"}", formatToJsonString(response));
    }

    @Test
    public void testTypeStart() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "testtype");
        request.put("startDate", "2016-06-01T12:04:59.191Z");

        Response response = deleteProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Entity is required\"}", formatToJsonString(response));
    }

    @Test
    public void testTypeException() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "testtype");


        Response response = deleteProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Entity is required\"}", formatToJsonString(response));
    }


    @Test
    public void testEntityTagsExactTrue() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("delete-entity10");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map tags = new HashMap<String, String>() {{
            put("t1", "v1");
            put("t2", "v2");
        }};
        entity.setTags(tags);
        property.setTags(tags);
        EntityMethod.createOrReplaceEntityCheck(entity);
        assertTrue(propertyExist(property));

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", "$entity_tags");
        queryObj.put("entity", entity.getName());
        queryObj.put("exactMatch", true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testEntityTagsExactFalse() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("delete-entity11");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map tags = new HashMap<String, String>() {{
            put("t1", "v1");
            put("t2", "v2");
        }};
        entity.setTags(tags);
        property.setTags(tags);
        EntityMethod.createOrReplaceEntityCheck(entity);
        assertTrue(propertyExist(property));

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", "$entity_tags");
        queryObj.put("entity", entity.getName());
        queryObj.put("exactMatch", false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testExtraKeyExactTrue() throws Exception {
        final Property property = new Property("delete-type12", "delete-entity12");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>(property.getKey()) {{
            put("k2", "kv2");
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testExtraKeyExactFalse() throws Exception {
        final Property property = new Property("delete-type13", "delete-entity13");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>(property.getKey()) {{
            put("k2", "kv2");
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testExtraKeyPropNoKeyExactTrue() throws Exception {
        final Property property = new Property("delete-type14", "delete-entity14");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>() {{
            put("k2", "kv2");
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testExtraKeyPropNoKeyExactFalse() throws Exception {
        final Property property = new Property("delete-type15", "delete-entity15");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>() {{
            put("k2", "kv2");
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testKeyMissmatchExactTrue() throws Exception {
        final Property property = new Property("delete-type16", "delete-entity16");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>() {{
            put("k1", "kv2");
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());

    }

    @Test
    public void testKeyMissmatchExactFalse() throws Exception {
        final Property property = new Property("delete-type17", "delete-entity17");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>() {{
            put("k1", "kv2");
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(queryObj).getStatus());
        assertTrue(propertyExist(property));
    }


}
