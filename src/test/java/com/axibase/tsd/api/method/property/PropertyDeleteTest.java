package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.TestUtil.appendChar;
import static com.axibase.tsd.api.util.TestUtil.getNextDay;
import static com.axibase.tsd.api.util.Util.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.*;

public class PropertyDeleteTest extends PropertyMethod {


    @Test
    public void testFutureDateExactTrue() throws Exception {
        final Property property = new Property("delete-type9", "delete-entity9");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(getNextDay());
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should be remain", propertyExist(property));
    }


    @Test
    public void testFutureDateExactFalse() throws Exception {
        final Property property = new Property("delete-type8", "delete-entity8");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(getNextDay());
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testFutureDateExactDefault() throws Exception {
        final Property property = new Property("delete-type7", "delete-entity7");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(getNextDay());
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should remain", propertyExist(property));
    }

    @Test
    public void testCommonTypeEntityTypeEntityKey() throws Exception {
        final Property property = new Property("delete-type-6", "delete-entity6");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);

        Property secondProperty = new Property(null, "delete-entity6-2");
        secondProperty.setType(property.getType());
        secondProperty.setTags(property.getTags());
        secondProperty.addKey("k2", "v2");
        insertPropertyCheck(secondProperty);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("First property should be deleted", propertyExist(property));
        assertTrue("Second property should remain", propertyExist(secondProperty));
    }

    @Test
    public void testEmptyKeyExactTrue() throws Exception {
        final Property property = new Property("delete-type-6.1", "delete-entity6.1");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        Property secondProperty = new Property();
        secondProperty.setType(property.getType());
        secondProperty.setEntity(property.getEntity());
        secondProperty.setType(property.getType());
        secondProperty.setTags(property.getTags());
        secondProperty.addKey("k2", "v2");
        insertPropertyCheck(secondProperty);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("First property should be deleted", propertyExist(property));
        assertTrue("Second property should remain", propertyExist(secondProperty));
    }

    @Test
    public void testEmptyKeyExactFalse() throws Exception {
        final Property property = new Property("delete-type-6.11", "delete-entity6.11");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        Property secondProperty = new Property();
        secondProperty.setType(property.getType());
        secondProperty.setEntity(property.getEntity());
        secondProperty.setType(property.getType());
        secondProperty.setTags(property.getTags());
        secondProperty.addKey("k2", "v2");
        insertPropertyCheck(secondProperty);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("First property should be deleted", propertyExist(property));
        assertFalse("Second property should be deleted", propertyExist(secondProperty));
    }


    @Test
    public void testKeyNameTooLong() throws Exception {
        final Property property = new Property("delete-type-6.4", "delete-entity6.4");
        property.addTag("t1", "v1");
        property.addKey(appendChar(new StringBuilder(), 'a', 10000).append("-key").toString(), "kv1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testKeyValueTooLong() throws Exception {
        final Property property = new Property("delete-type-6.5", "delete-entity6.5");
        property.addTag("t1", "v1");
        property.addKey("kv1", appendChar(new StringBuilder(), 'a', 10000).append("-value").toString());
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testKeyCountTooMuch() throws Exception {
        final Integer count = 1000;
        final Property property = new Property("delete-type-6.6", "delete-entity6.6");
        property.addTag("t1", "v1");
        for (int i = 0; i < count; i++) {
            property.addKey("key_name-" + String.valueOf(i), "key_value-" + String.valueOf(i));
        }
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }


    @Test
    public void testTypeEntityExactFalse() throws Exception {
        final Property property = new Property("delete-type-5", "delete-entity5");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);

        Property secondProperty = new Property();
        secondProperty.setType(property.getType());
        secondProperty.setEntity(property.getEntity());
        secondProperty.setTags(property.getTags());
        secondProperty.addKey("k2", "v2");
        insertPropertyCheck(secondProperty);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("First property should be deleted", propertyExist(property));
        assertFalse("Second property should be deleted", propertyExist(secondProperty));

    }


    @Test
    public void testTypeEntityKey() throws Exception {
        final Property property = new Property("delete-type4", "delete-entity4");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testTypeObjectRaiseError() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", new HashMap<>());
        queryObj.put("entity", "mock-entity");
        queryObj.put("key", new HashMap<>());

        assertEquals("Delete query should fail if type is object", BAD_REQUEST.getStatusCode(), deleteProperty(queryObj).getStatus());
    }

    @Test
    public void testTypeAbsentRaiseError() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("entity", "mock-entity");
        queryObj.put("key", new HashMap<>());

        assertEquals("Delete query should fail if type is absent", BAD_REQUEST.getStatusCode(), deleteProperty(queryObj).getStatus());
    }

    @Test
    public void testTypeTooLong() throws Exception {
        final Property property = new Property(appendChar(new StringBuilder(), 'a', 10000).append("type-161").toString(), "delete-entity161");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testTypeNullRaiseError() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", null);
        queryObj.put("entity", "mock-entity");

        assertEquals("Delete query should fail if type is null", BAD_REQUEST.getStatusCode(), deleteProperty(queryObj).getStatus());
    }

    @Test
    public void testTypeArrayRaiseError() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", Collections.singletonList("mock-type"));
        queryObj.put("entity", "mock-entity");

        assertEquals("Delete query should fail if type is array", BAD_REQUEST.getStatusCode(), deleteProperty(queryObj).getStatus());
    }

    @Test
    public void testMultipleTypeEntityExactTrue() throws Exception {
        final Property property = new Property("delete-type3", "delete-entity3");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);

        Property secondProperty = new Property();
        secondProperty.setType(property.getType());
        secondProperty.setEntity(property.getEntity());
        secondProperty.addKey("k2", "v2");
        secondProperty.setTags(property.getTags());
        insertPropertyCheck(secondProperty);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("First property should remain", propertyExist(property));
        assertTrue("Second property should remain", propertyExist(secondProperty));
    }


    @Test
    public void testTypeEntityExactTrue() throws Exception {
        final Property property = new Property("delete-type2", "delete-entity2");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should be remain", propertyExist(property));
    }

    @Test
    public void testEndDateEqDate() throws Exception {
        final Property property = new Property("delete-type1", "delete-entity1");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate("2016-06-01T12:00:00.000Z");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setEndDate(property.getDate());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should be remain", propertyExist(property));
    }

    @Test
    public void testTypeStartEndRaiseError() throws Exception {
        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType("mock-type");
        deleteQuery.setStartDate(MIN_QUERYABLE_DATE);
        deleteQuery.setEndDate(MAX_QUERYABLE_DATE);


        assertEquals("Fail to execute delete query", BAD_REQUEST.getStatusCode(), deleteProperty(deleteQuery).getStatus());
    }

    @Test
    public void testTypeOnlyRaiseError() throws Exception {
        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType("mock-type");

        assertEquals("Fail to execute delete query", BAD_REQUEST.getStatusCode(), deleteProperty(deleteQuery).getStatus());
    }


    @Test
    public void testEntityTagsExactTrue() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("delete-entity10");
        entity.addTag("t1", "v1");
        entity.addTag("t2", "v2");

        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());
        EntityMethod.createOrReplaceEntityCheck(entity);
        assertTrue(propertyExist(property));

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType("$entity_tags");
        deleteQuery.setEntity(entity.getName());
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testEntityTagsExactFalse() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("delete-entity11");
        entity.addTag("t1", "v1");
        entity.addTag("t2", "v2");

        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());

        EntityMethod.createOrReplaceEntityCheck(entity);
        assertTrue(propertyExist(property));

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType("$entity_tags");
        deleteQuery.setEntity(entity.getName());
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue(propertyExist(property));
    }

    @Test
    public void testExtraKeyExactTrue() throws Exception {
        final Property property = new Property("delete-type12", "delete-entity12");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);


        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());
        deleteQuery.addKey("k2", "kv2");
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should remain", propertyExist(property));
    }

    @Test
    public void testExtraKeyExactFalse() throws Exception {
        final Property property = new Property("delete-type13", "delete-entity13");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());
        deleteQuery.addKey("k2", "kv2");
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should remain", propertyExist(property));
    }

    @Test
    public void testExtraKeyPropNoKeyExactTrue() throws Exception {
        final Property property = new Property("delete-type14", "delete-entity14");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k2", "kv2");
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should remain", propertyExist(property));
    }

    @Test
    public void testExtraKeyPropNoKeyExactFalse() throws Exception {
        final Property property = new Property("delete-type15", "delete-entity15");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k2", "kv2");
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should remain", propertyExist(property));
    }

    @Test
    public void testKeyValueMismatchExactTrue() throws Exception {
        final Property property = new Property("delete-type16", "delete-entity16");
        final String keyName = "k1";
        property.addTag("t1", "v1");
        property.addKey(keyName, "kv1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey(keyName, "kv2");
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should remain", propertyExist(property));
    }

    @Test
    public void testKeyValueMismatchExactFalse() throws Exception {
        final Property property = new Property("delete-type17", "delete-entity17");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k2", "kv2");
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertTrue("Property should remain", propertyExist(property));
    }

    @Test
    public void testEntityEmptyRaiseError() throws Exception {
        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType("mock-type");
        deleteQuery.setEntity("");

        assertEquals("Delete query should fail if entity is absent", BAD_REQUEST.getStatusCode(), deleteProperty(deleteQuery).getStatus());
    }

    @Test
    public void testEntityObjectRaiseError() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("entity", new HashMap<>());
        queryObj.put("key", "mock-type");

        assertEquals("Delete query should fail if entity is object", BAD_REQUEST.getStatusCode(), deleteProperty(queryObj).getStatus());
    }

    @Test
    public void testEntityArrayRaiseError() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("entity", Collections.singletonList("mock-entity"));
        queryObj.put("key", "mock-type");

        assertEquals("Delete query should fail if entity is object", BAD_REQUEST.getStatusCode(), deleteProperty(queryObj).getStatus());
    }

    @Test
    public void testEntityNullRaiseError() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("entity", null);
        queryObj.put("key", "mock-type");

        assertEquals("Delete query should fail if entity is object", BAD_REQUEST.getStatusCode(), deleteProperty(queryObj).getStatus());
    }

    @Test
    public void testEntityTooLong() throws Exception {
        final Property property = new Property("delete-type-18", appendChar(new StringBuilder(), 'a', 5000).append("type-18").toString());

        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setKey(property.getKey());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    @Test
    public void testTimestampZeroDateFilterMatch() throws Exception {
        final Property property = new Property("delete-type19", "delete-entity19");
        property.addTag("t1", "v1");
        property.setDate(MIN_STORABLE_DATE);
        insertPropertyCheck(property);
        assertTrue(propertyExist(property));
        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setStartDate(property.getDate());
        deleteQuery.setEndDate(Util.addOneMS(property.getDate()));

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be removed", propertyExist(property));
    }

    @Test
    public void testTimestampZeroNoDateFilter() throws Exception {
        final Property property = new Property("delete-type20", "delete-entity20");
        property.addTag("t1", "v1");
        property.setDate(MIN_STORABLE_DATE);
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be removed", propertyExist(property));
    }

    @Test
    public void testTimestampZeroNoDateFilterExactMatch() throws Exception {
        final Property property = new Property("delete-type21", "delete-entity21");
        property.addTag("t1", "v1");
        property.setDate(MIN_STORABLE_DATE);
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be removed", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueNullExactTrue() throws Exception {
        final Property property = new Property("delete-type-22", "delete-entity22");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k1", null);
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueNullExactFalse() throws Exception {
        final Property property = new Property("delete-type-23", "delete-entity23");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k1", null);
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueEmptyExactTrue() throws Exception {
        final Property property = new Property("delete-type-24", "delete-entity24");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k1", "");
        deleteQuery.setExactMatch(true);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueEmptyExactFalse() throws Exception {
        final Property property = new Property("delete-type-25", "delete-entity25");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k1", "");
        deleteQuery.setExactMatch(false);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueSpaces() throws Exception {
        final Property property = new Property("delete-type-26", "delete-entity26");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k1", "    ");

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueContainsSpaces() throws Exception {
        final Property property = new Property("delete-type-27", "delete-entity27");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery deleteQuery = new PropertyQuery();
        deleteQuery.setType(property.getType());
        deleteQuery.setEntity(property.getEntity());
        deleteQuery.addKey("k1", "  kv1   ");

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueInteger() throws Exception {
        final Property property = new Property("delete-type-28", "delete-entity28");
        property.addTag("t1", "v1");
        property.addKey("k1", "111");
        insertPropertyCheck(property);

        Map<String, Object> deleteQuery = new HashMap<>();
        deleteQuery.put("type", property.getType());
        deleteQuery.put("entity", property.getEntity());
        deleteQuery.put("startDate", MIN_QUERYABLE_DATE);
        deleteQuery.put("endDate", MAX_QUERYABLE_DATE);

        Map<String, Object> key = new HashMap<>();
        key.put("k1", 111);
        deleteQuery.put("key", key);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueBoolean() throws Exception {
        final Property property = new Property("delete-type-29", "delete-entity29");
        property.addTag("t1", "v1");
        property.addKey("k1", "true");
        insertPropertyCheck(property);

        Map<String, Object> deleteQuery = new HashMap<>();
        deleteQuery.put("type", property.getType());
        deleteQuery.put("entity", property.getEntity());
        deleteQuery.put("startDate", MIN_QUERYABLE_DATE);
        deleteQuery.put("endDate", MAX_QUERYABLE_DATE);

        Map<String, Object> key = new HashMap<>();
        key.put("k1", true);
        deleteQuery.put("key", key);

        assertEquals("Fail to execute delete query", OK.getStatusCode(), deleteProperty(deleteQuery).getStatus());
        assertFalse("Property should be deleted", propertyExist(property));
    }
}
