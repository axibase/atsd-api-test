package com.axibase.tsd.api.method.property;


import com.axibase.tsd.api.util.ErrorTemplate;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class PropertyQueryTest extends PropertyMethod {
    /**
     * #NoTicket
     */
    @Test
    public static void testEntityTags() throws Exception {
        final Entity entity = new Entity("query-entity20");
        entity.addTag("t1", "tv1");
        entity.addTag("t2", "tv2");

        final Property property = new Property();
        property.setType(ENTITY_TAGS_PROPERTY_TYPE);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());

        EntityMethod.createOrReplaceEntityCheck(entity);

        assertTrue("Properties with entityTag should be specified", propertyExist(property));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testStartDateInFuture() throws Exception {
        final Property property = new Property("query-type19", "query-entity19");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getNextDay());
        insertPropertyCheck(property);

        PropertyQuery query = new PropertyQuery(property.getType(), property.getEntity(), property.getKey());
        query.setStartDate(property.getDate());
        query.setInterval(new Interval(2, TimeUnit.DAY));

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityWildcardExactFalse() throws Exception {
        final Property property = new Property("query-type18", "query-entity18");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity18-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");
        secondProperty.addKey("k2", "kv2");
        insertPropertyCheck(secondProperty);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.setExactMatch(false);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, secondProperty));
        String given = queryProperty(query).readEntity(String.class);

        assertTrue("Stored series do not match to inserted", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testMultipleEntityWildcardKeyNotMatchExactTrue() throws Exception {
        final Property property = new Property("query-type17", "query-entity17");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity17-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");
        secondProperty.addKey("k2", "kv2");
        insertPropertyCheck(secondProperty);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.setExactMatch(true);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        String given = queryProperty(query).readEntity(String.class);

        assertTrue("Only first property should be returned", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testMultipleEntityWildcardKeyPartMatchExactFalse() throws Exception {
        final Property property = new Property("query-type16", "query-entity16");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity16-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");
        secondProperty.setKey(property.getKey());
        secondProperty.addKey("k2", "kv2");
        insertPropertyCheck(secondProperty);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.setExactMatch(false);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, secondProperty));
        String given = queryProperty(query).readEntity(String.class);

        assertTrue("Stored series do not match to inserted", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testMultipleEntityWildcardKeyPartMatchExactTrue() throws Exception {
        final Property property = new Property("query-type15", "query-entity15");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity15-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");
        secondProperty.setKey(property.getKey());
        secondProperty.addKey("k2", "kv2");
        insertPropertyCheck(secondProperty);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.setKey(property.getKey());
        query.setExactMatch(true);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        String given = queryProperty(query).readEntity(String.class);

        assertTrue("Stored property do not match to inserted", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testExtraKeyExactTrue() throws Exception {
        final Property property = new Property("query-type14", "query-entity14");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.setKey(property.getKey());
        query.addKey("extra_key", "extra_key_val");
        query.setExactMatch(true);

        String emptyJsonList = "[]";
        Response response = queryProperty(query);
        assertTrue("Stored series do not match to inserted", compareJsonString(emptyJsonList, response.readEntity(String.class)));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testDiffKeyExactTrue() throws Exception {
        final Property property = new Property("query-type13", "query-entity13");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.addKey("k2", "k2_val");
        query.setExactMatch(true);

        String emptyJsonList = "[]";
        Response response = queryProperty(query);
        assertTrue("Should not receive any properties", compareJsonString(emptyJsonList, response.readEntity(String.class)));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testExactTruePartialMatch() throws Exception {
        final Property property = new Property("query-type12", "query-entity12");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.addKey("k2", "kv2");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.addKey("k1", "kv1");
        query.setExactMatch(true);

        String emptyJsonList = "[]";
        Response response = queryProperty(query);
        assertTrue("Should not receive any properties", compareJsonString(emptyJsonList, response.readEntity(String.class)));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testKeyMatchExactFalse() throws Exception {
        final Property property = new Property("query-type11", "query-entity11");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.setKey(property.getKey());
        query.setExactMatch(false);

        assertInsertedPropertyReturned(property, query);

    }

    @Test
    public void testKeyMatchExactTrue() throws Exception {
        final Property property = new Property("query-type10", "query-entity10");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "*");
        query.setKey(property.getKey());
        query.setExactMatch(true);

        assertInsertedPropertyReturned(property, query);

    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntitiesField() throws Exception {
        final Property property = new Property("query-type9", "query-entity9");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity9-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");


        insertPropertyCheck(secondProperty);

        final Property wrongProperty = new Property(null, "query-wrong-entity-9");
        wrongProperty.setType(property.getType());
        wrongProperty.addTag("tw1", "twv1");
        insertPropertyCheck(wrongProperty);

        PropertyQuery query = new PropertyQuery();
        query.setType(property.getType());
        query.setEntities(Arrays.asList(property.getEntity(), secondProperty.getEntity()));
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, secondProperty));
        String given = queryProperty(query).readEntity(String.class);
        assertTrue("Stored properties does not match to expected", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testLastDefault() throws Exception {
        final Property property = new Property("query-type8", "query-entity8");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getPreviousDay());
        insertPropertyCheck(property);

        final Property lastProperty = new Property();
        lastProperty.setType(property.getType());
        lastProperty.setEntity(property.getEntity());
        lastProperty.addTag("t2", "tv2");
        lastProperty.setDate(Util.getCurrentDate());
        insertPropertyCheck(lastProperty);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, lastProperty));
        String given = queryProperty(query).readEntity(String.class);
        assertTrue("Both properties should be returned if 'last' field is not specified", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testLastFalse() throws Exception {
        final Property property = new Property("query-type7", "query-entity7");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getPreviousDay());
        insertPropertyCheck(property);

        final Property lastProperty = new Property();
        lastProperty.setType(property.getType());
        lastProperty.setEntity(property.getEntity());
        lastProperty.addTag("t1l", "tv1l");
        lastProperty.setDate(Util.getCurrentDate());
        insertPropertyCheck(lastProperty);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setLast(false);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, lastProperty));
        String given = queryProperty(query).readEntity(String.class);
        assertTrue("Both properties should be returned if 'last' field is not specified", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testLastTrue() throws Exception {
        final Property property = new Property("query-type6", "query-entity6");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getPreviousDay());
        insertPropertyCheck(property);

        final Property lastProperty = new Property();
        lastProperty.setType(property.getType());
        lastProperty.setEntity(property.getEntity());
        lastProperty.addTag("t1l", "tv1l");
        lastProperty.setDate(Util.getCurrentDate());
        insertPropertyCheck(lastProperty);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setLast(true);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(lastProperty));
        String given = queryProperty(query).readEntity(String.class);
        assertTrue("Only last property should be returned if 'last' field is set to 'true'", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testLastTrueReturnMultipleProperty() throws Exception {
        final Property property = new Property("query-type6.1", "query-entity6.1");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getPreviousDay());
        insertPropertyCheck(property);

        final Property lastProperty = new Property();
        lastProperty.setType(property.getType());
        lastProperty.setEntity(property.getEntity());
        lastProperty.addTag("t1l", "tv1l");
        lastProperty.setDate(Util.getCurrentDate());
        insertPropertyCheck(lastProperty);

        final Property lastPropertySecond = new Property();
        lastPropertySecond.setType(property.getType());
        lastPropertySecond.setEntity(property.getEntity());
        lastPropertySecond.addTag("t1l", "tv1l");
        lastPropertySecond.addKey("k2", "kv2");
        lastPropertySecond.setDate(lastProperty.getDate());
        insertPropertyCheck(lastPropertySecond);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setLast(true);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(lastProperty, lastPropertySecond));
        String given = queryProperty(query).readEntity(String.class);
        assertTrue("Only two last properties should be in response", compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testStartPastIntervalGiveFuture() throws Exception {
        final Property property = new Property("query-type5", "query-entity5");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        PropertyQuery query = new PropertyQuery(property.getType(), property.getEntity());
        query.setKey(property.getKey());
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setInterval(new Interval(1999, TimeUnit.YEAR));

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #NoTicket
     */
    @Test
    public void testStartEQDate() throws Exception {
        final Property property = new Property("query-type4", "query-entity4");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertProperty(property);

        PropertyQuery query = new PropertyQuery(property.getType(), property.getEntity());
        query.setStartDate(property.getDate());
        query.setEndDate(Util.addOneMS(property.getDate()));

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #NoTicket
     */
    @Test
    public void testStartPastEndFuture() throws Exception {
        final Property property = new Property("query-type3", "query-entity3");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        PropertyQuery query = new PropertyQuery(property.getType(), property.getEntity());
        query.setStartDate(Util.ISOFormat(Util.getPreviousDay()));
        query.setEndDate(Util.ISOFormat(Util.getNextDay()));

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #NoTicket
     */
    @Test
    public void testDefaultQueryExactDefault() throws Exception {
        final Property property = new Property("query-type2", "query-entity2");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #NoTicket
     */
    @Test
    public void testTypeEntityStartEndExactFalse() throws Exception {
        final Property property = new Property("query-type1", "query-entity1");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setExactMatch(false);

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #NoTicket
     */
    @Test
    public void testPartKey() throws Exception {
        final Property property = new Property("query-type41.5", "query-entity41.5");
        property.addTag("fs_type", "ext4");
        property.addKey("file_system", "/");
        property.addKey("mount_point", "/sda1");
        property.setDate("2016-05-25T04:00:00.000Z");
        insertPropertyCheck(property);

        PropertyQuery query = new PropertyQuery(property.getType(), property.getEntity());
        query.addKey("file_system", "/");
        query.setStartDate("2016-05-25T04:00:00Z");
        query.setEndDate("2016-05-25T05:00:00Z");

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEndDateAbsent() throws Exception {
        PropertyQuery query = new PropertyQuery("mock-type", "mock-entity");
        query.setStartDate("2016-05-25T05:00:00Z");

        Response response = queryProperty(query);
        assertEquals("Query should fail if endDate is not specified", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", ErrorTemplate.DATE_FILTER_COMBINATION_REQUIRED, extractErrorMessage(response));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testDateFilterAbsent() throws Exception {
        PropertyQuery query = new PropertyQuery("mock-type", "mock-entity");

        Response response = queryProperty(query);
        assertEquals("Query should fail if DateFiled is not specified", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", ErrorTemplate.DATE_FILTER_COMBINATION_REQUIRED, extractErrorMessage(response));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testOnlyTypeSpecified() throws Exception {
        PropertyQuery query = new PropertyQuery();
        query.setType("mock-type");

        Response response = queryProperty(query);
        assertEquals("Query should fail if DateFiled is not specified", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", ErrorTemplate.DATE_FILTER_COMBINATION_REQUIRED, extractErrorMessage(response));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityFilterAbsent() throws Exception {
        PropertyQuery query = new PropertyQuery();
        query.setType("mock-type");
        query.setStartDate("2016-06-01T12:04:59.191Z");
        query.setEndDate("2016-06-01T13:04:59.191Z");

        Response response = queryProperty(query);
        assertEquals("Query EntityFilter is not specified", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", ErrorTemplate.ENTITY_FILTER_REQUIRED, extractErrorMessage(response));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsTagsAsKeyExactTrue() throws Exception {
        final Entity entity = new Entity("query-entity21");
        entity.addTag("t1", "tv1");
        entity.addTag("t2", "tv2");

        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.setKey(entity.getTags());
        query.setExactMatch(true);

        String given = queryProperty(query).readEntity(String.class);
        String emptyJsonList = "[]";
        assertTrue("No property should be returned", compareJsonString(emptyJsonList, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsTagsAsPartKeyExactFalse() throws Exception {
        final Entity entity = new Entity("query-entity22");
        entity.addTag("t1", "tv1");
        entity.addTag("t2", "tv2");

        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.addKey("t1", "tv1");
        query.setExactMatch(false);

        String given = queryProperty(query).readEntity(String.class);
        String emptyJsonList = "[]";
        assertTrue("No property should be returned", compareJsonString(emptyJsonList, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsEmptyKeyExactFalse() throws Exception {
        final Entity entity = new Entity("query-entity23");
        entity.addTag("t1", "tv1");
        entity.addTag("t2", "tv2");

        final Property property = new Property();
        property.setType(ENTITY_TAGS_PROPERTY_TYPE);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());

        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.setExactMatch(false);

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue(String.format("Property with type %s for inserted entity should be returned", ENTITY_TAGS_PROPERTY_TYPE), compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsEmptyKeyExactTrue() throws Exception {
        final Entity entity = new Entity("query-entity24");
        entity.addTag("t1", "tv1");
        entity.addTag("t2", "tv2");

        final Property property = new Property();
        property.setType(ENTITY_TAGS_PROPERTY_TYPE);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());

        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.setExactMatch(true);

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue(String.format("Property with type %s for inserted entity should be returned", ENTITY_TAGS_PROPERTY_TYPE), compareJsonString(expected, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsKeyExpression() throws Exception {
        final Entity entity = new Entity("query-entity25");
        entity.addTag("t1", "tv1");
        entity.addTag("t2", "tv2");

        final Property property = new Property();
        property.setType(ENTITY_TAGS_PROPERTY_TYPE);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());

        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.setKeyTagExpression("tags.t1 == 'tv1'");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue(String.format("Property with type %s for inserted entity should be returned", ENTITY_TAGS_PROPERTY_TYPE), compareJsonString(expected, given));

    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsKeyExpressionNoMatch() throws Exception {
        final Entity entity = new Entity("query-entity26");
        entity.addTag("t1", "tv1");
        entity.addTag("t2", "tv2");

        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.setKeyTagExpression("tags.t1 == 'v2'");

        String given = queryProperty(query).readEntity(String.class);
        String emptyJsonList = "[]";
        assertTrue("No property should be returned", compareJsonString(emptyJsonList, given));
    }


    /**
     * #NoTicket
     */
    @Test
    public void testEntityWildcardTagsAsKey() throws Exception {
        final Entity entity = new Entity("wck-query-entity33");
        entity.addTag("wct1", "wcv1");
        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, "wck-*");
        query.setKey(entity.getTags());

        String given = queryProperty(query).readEntity(String.class);
        String emptyJsonList = "[]";
        assertTrue("No property should be returned", compareJsonString(emptyJsonList, given));
    }


    /**
     * #NoTicket
     */
    @Test
    public void testEntityWildcardExpression() throws Exception {
        Entity entity1 = new Entity("wcke-query-entity37");
        entity1.addTag("wc2t1", "wc2v1");
        Entity entity2 = new Entity("wcke-query-entity38");
        entity2.addTag("wc2t1", "wc2V1");
        Entity entity3 = new Entity("wcke-query-entity39");
        entity3.addTag("wc2t1", "wc2v1");
        entity3.addTag("wc2t2", "wc2v2");
        Entity entity4 = new Entity("wcke-query-entity40");
        entity4.addTag("wc2t2", "wc2V2");
        EntityMethod.createOrReplaceEntityCheck(entity1);
        EntityMethod.createOrReplaceEntityCheck(entity2);
        EntityMethod.createOrReplaceEntityCheck(entity3);
        EntityMethod.createOrReplaceEntityCheck(entity4);

        final Property property3 = new Property();
        property3.setType(ENTITY_TAGS_PROPERTY_TYPE);
        property3.setEntity(entity3.getName());
        property3.setTags(entity3.getTags());

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, "wcke-*");
        query.setKeyTagExpression("keys.wc2t1 = 'wc2V1' OR tags.wc2t2 = 'wc2v2'");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property3));
        assertTrue(String.format("Property with type %s for inserted entity should be returned", ENTITY_TAGS_PROPERTY_TYPE), compareJsonString(expected, given));

    }


    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsExpressionCaseSensitiveValue() throws Exception {
        Entity entity = new Entity("query-entity41");
        entity.addTag("t1", "tv1");
        EntityMethod.createOrReplaceEntityCheck(entity);

        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.setKeyTagExpression("tags.t1 == 'tV1'");

        String given = queryProperty(query).readEntity(String.class);
        String emptyJsonList = "[]";
        assertTrue("No property should be returned", compareJsonString(emptyJsonList, given));
    }

    /**
     * #NoTicket
     */
    @Test
    public void testEntityTagsKeyTagExpressionCaseInsensitiveName() throws Exception {
        Entity entity = new Entity("query-entity42");
        entity.addTag("t1", "tv1");
        EntityMethod.createOrReplaceEntityCheck(entity);


        final Property property = new Property();
        property.setType(ENTITY_TAGS_PROPERTY_TYPE);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());


        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, entity.getName());
        query.setKeyTagExpression("tags.T1 == 'tv1'");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue(String.format("Property with type %s for inserted entity should be returned", ENTITY_TAGS_PROPERTY_TYPE), compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionOR() throws Exception {
        final Property property = new Property("query-type43", "query-entity43");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);
        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t1", "tv1");
        insertPropertyCheck(property2);

        final Property property3 = new Property();
        property3.setType(property.getType());
        property3.setEntity(property.getEntity());
        property3.addTag("t3", "tv3");
        property3.addKey("k3", "kv3");
        insertPropertyCheck(property3);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("tags.t1 == 'tv1' OR keys.k3 == 'kv3'");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, property2, property3));
        assertTrue("All inserted properties should be returned", compareJsonString(expected, given));
    }


    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionAND() throws Exception {
        final Property property = new Property("query-type44", "query-entity44");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t1", "tv1");
        property2.addKey("k2", "kv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("tags.t1 == 'tv1' AND keys.k2 == 'kv2'");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));
        assertTrue("Only second property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionTagsLIKE() throws Exception {
        final Property property = new Property("query-type45", "query-entity45");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t1", "tg1");
        property2.addKey("k2", "kv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("tags.t1 LIKE 'tv*'");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue("Only first property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionKeysLIKE() throws Exception {
        final Property property = new Property("query-type45.5", "query-entity45.5");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t1", "tg1");
        property2.addKey("k1", "kg2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("keys.k1 LIKE 'kg*'");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));
        assertTrue("Only second property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionKeyTagCompareEQ() throws Exception {
        final Property property = new Property("query-type46", "query-entity46");
        property.addTag("t1", "tv1");
        property.addKey("k1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t1", "tv1");
        property2.addKey("k1", "tv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("tags.t1 == keys.k1");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue("Only first property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionKeyTagCompareNotEQ() throws Exception {
        final Property property = new Property("query-type47", "query-entity47");
        property.addTag("t1", "tv1");
        property.addKey("k1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t1", "tv1");
        property2.addKey("k1", "tv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("tags.t1 != keys.k1");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));
        assertTrue("Only second property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionKeyEmpty() throws Exception {
        final Property property = new Property("query-type48", "query-entity48");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "tv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("keys.k2 == ''");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue("Only first property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionKeyNotEmpty() throws Exception {
        final Property property = new Property("query-type49", "query-entity49");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "tv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("keys.k2 != ''");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));
        assertTrue("Only second property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionTagEmpty() throws Exception {
        final Property property = new Property("query-type49.1", "query-entity49.1");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "tv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("tags.t2 == ''");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue("Only first property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionTagNotEmpty() throws Exception {
        final Property property = new Property("query-type50", "query-entity50");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "tv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("tags.t2 != ''");

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));
        assertTrue("Only second property should be returned", compareJsonString(expected, given));
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionLowerTag() throws Exception {
        final Property property = new Property("query-type51", "query-entity51");
        property.addTag("t1", "TV1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("lower(tags.t1) == 'tv1'");

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionLowerKey() throws Exception {
        final Property property = new Property("query-type52", "query-entity52");
        property.addTag("t1", "tv1");
        property.addKey("k1", "KV1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("lower(keys.k1) == 'kv1'");

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionUpperTag() throws Exception {
        final Property property = new Property("query-type53", "query-entity53");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("upper(tags.t1) == 'TV1'");

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #2908
     */
    @Test
    public void testKeyTagExpressionUpperKey() throws Exception {
        final Property property = new Property("query-type54", "query-entity54");
        property.addTag("t1", "tv1");
        property.addKey("k1", "KV1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("upper(keys.k1) == 'KV1'");

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #2946
     */
    @Test
    public void testLimit1() throws Exception {
        final int limit = 1;
        final Property property = new Property("query-type55", "query-entity55");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "kv2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setLimit(limit);

        int actual = calculateJsonArraySize(queryProperty(query).readEntity(String.class));
        assertEquals("One property should be received", limit, actual);
    }


    /**
     * #2946
     */
    @Test
    public void testLimit2() throws Exception {
        final int limit = 2;
        final Property property = new Property("query-type56", "query-entity56");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "kv2");
        insertPropertyCheck(property2);

        final Property property3 = new Property();
        property3.setType(property.getType());
        property3.setEntity(property.getEntity());
        property3.addTag("t3", "tv3");
        property3.addKey("k3", "kv3");
        insertPropertyCheck(property3);


        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setLimit(limit);

        int actual = calculateJsonArraySize(queryProperty(query).readEntity(String.class));
        assertEquals("Two property should be received", limit, actual);
    }

    /**
     * #2946
     */
    @Test
    public void testLimit0() throws Exception {
        final int limit = 0;
        final Property property = new Property("query-type57", "query-entity57");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "kv2");
        insertPropertyCheck(property2);

        final Property property3 = new Property();
        property3.setType(property.getType());
        property3.setEntity(property.getEntity());
        property3.addTag("t3", "tv3");
        property3.addKey("k3", "kv3");
        insertPropertyCheck(property3);


        PropertyQuery query = prepareSimplePropertyQuery(ENTITY_TAGS_PROPERTY_TYPE, property.getEntity());
        query.setLimit(limit);

        assertEquals("Three property should be received", limit, calculateJsonArraySize(queryProperty(query).readEntity(String.class)));
    }


    /**
     * #3110
     */
    @Test
    public void testLimitWithDateFilter() throws Exception {
        final int limit = 1;
        final Property property1 = new Property("query-type57-a", "query-entity57-a");
        property1.setDate("2016-07-18T02:35:00.000Z");
        property1.addKey("uniq", "key1");
        property1.addTag("tag_key", "tag_value");
        insertPropertyCheck(property1);

        final Property property2 = new Property();
        property2.setType(property1.getType());
        property2.setEntity(property1.getEntity());
        property2.setDate(Util.addOneMS(property1.getDate()));
        property2.addKey("uniq", "key2");
        property2.addTag("tag_key2", "tag_value2");
        insertPropertyCheck(property2);

        PropertyQuery query = new PropertyQuery(property1.getType(), property1.getEntity());
        query.setStartDate(property2.getDate());
        query.setEndDate(MAX_QUERYABLE_DATE);
        query.setLimit(limit);

        String given = queryProperty(query).readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));
        assertTrue("Property2 should be returned", compareJsonString(expected, given));

        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(Util.addOneMS(property1.getDate()));

        given = queryProperty(query).readEntity(String.class);
        expected = jacksonMapper.writeValueAsString(Collections.singletonList(property1));
        assertTrue("Property1 should be returned", compareJsonString(expected, given));
    }

    /**
     * #3110
     */
    @Test
    public void testLimitWithEntityFilter() throws Exception {
        final Property property = new Property("query-type57-b", "query-entity57-b-limitentityold");
        property.setDate("2016-07-18T02:35:00.000Z");
        property.addKey("uniq", "key1");
        property.addTag("tag_key", "tag_value");
        insertPropertyCheck(property);

        final Property property2 = new Property(null, "query-entity57-b-limitentity-2");
        property2.setType(property.getType());
        property2.setDate(Util.addOneMS(property.getDate()));
        property2.addKey("uniq", "key2");
        property2.addTag("tag_key2", "tag_value2");
        insertPropertyCheck(property2);

        final Property property3 = new Property(null, "query-entity57-b-limitentity-3");
        property3.setType(property.getType());
        property3.setDate(Util.addOneMS(property.getDate()));
        property3.addKey("uniq", "key3");
        property3.addTag("tag_key2", "tag_value2");
        insertPropertyCheck(property3);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), "query-entity57-b-limitentity-*");

        query.setLimit(2);

        assertEquals("Two property should be received", 2, calculateJsonArraySize(queryProperty(query).readEntity(String.class)));

        query.setEntity("query-entity57-b-limitentityo*");
        query.setLimit(1);
        assertEquals("One property should be received", 1, calculateJsonArraySize(queryProperty(query).readEntity(String.class)));
    }


    /**
     * #3110
     */
    @Test
    public void testLimitWithKeyExpression() throws Exception {
        final int limit = 1;
        final Property property = new Property("query-type57-c", "query-entity57-c");
        final String keyValue = "key1";
        property.addKey("uniq", keyValue);
        property.addTag("tag_key", "tag_value");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addKey("uniq", keyValue.toUpperCase());
        property2.addTag("tag_key2", "tag_value2");
        insertPropertyCheck(property2);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setKeyTagExpression("keys.uniq = '" + keyValue + "'");
        query.setLimit(limit);

        assertEquals("One property should be received", limit, calculateJsonArraySize(queryProperty(query).readEntity(String.class)));

        query.setKeyTagExpression("keys.uniq = '" + keyValue.toUpperCase() + "'");
        assertEquals("One property should be received", limit, calculateJsonArraySize(queryProperty(query).readEntity(String.class)));
    }


    /**
     * #2946
     */
    @Test
    public void testLimitNegative() throws Exception {
        final Property property = new Property("query-type58", "query-entity58");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "kv2");
        insertPropertyCheck(property2);

        final Property property3 = new Property();
        property3.setType(property.getType());
        property3.setEntity(property.getEntity());
        property3.addTag("t3", "tv3");
        property3.addKey("k3", "kv3");
        insertPropertyCheck(property3);


        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.setLimit(-1);
        assertEquals("Three property should be received", 3, calculateJsonArraySize(queryProperty(query).readEntity(String.class)));

        query.setLimit(-5);
        assertEquals("Three property should be received", 3, calculateJsonArraySize(queryProperty(query).readEntity(String.class)));
    }

    /**
     * 2979
     */
    @Test
    public void testEntitiesWildcardStartChar() throws Exception {
        final Property property = new Property("query-type59", "query-entity59");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        PropertyQuery query = new PropertyQuery();
        query.setType(property.getType());
        query.setEntities(Collections.singletonList("query-entity*"));
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * 2979
     */
    @Test
    public void testEntitiesWildcardQuestionChar() throws Exception {
        final Property property = new Property("query-type60", "query-entity60");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        PropertyQuery query = new PropertyQuery();
        query.setType(property.getType());
        query.setEntities(Collections.singletonList("query-entity6?"));
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);

        assertInsertedPropertyReturned(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueNullExactTrue() throws Exception {
        final Property property = new Property("query-type-61", "query-entity61");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.addKey("k1", null);
        query.setExactMatch(true);

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueNullExactFalse() throws Exception {
        final Property property = new Property("query-type-62", "query-entity62");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.addKey("k1", null);
        query.setExactMatch(false);

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueEmptyExactTrue() throws Exception {
        final Property property = new Property("query-type-63", "query-entity63");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.addKey("k1", "");
        query.setExactMatch(true);

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueEmptyExactFalse() throws Exception {
        final Property property = new Property("query-type-64", "query-entity64");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.addKey("k1", "");
        query.setExactMatch(false);

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueSpaces() throws Exception {
        final Property property = new Property("query-type-65", "query-entity65");
        property.addTag("t1", "v1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.addKey("k1", "      ");

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueContainsSpaces() throws Exception {
        final Property property = new Property("query-type-66", "query-entity66");
        property.addTag("t1", "v1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        PropertyQuery query = prepareSimplePropertyQuery(property.getType(), property.getEntity());
        query.addKey("k1", " kv1 ");

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueBoolean() throws Exception {
        final Property property = new Property("query-type-67", "query-entity67");
        property.addTag("t1", "v1");
        property.addKey("k1", "true");
        insertPropertyCheck(property);

        Map<String, Object> query = new HashMap<>();
        query.put("type", property.getType());
        query.put("entity", property.getEntity());
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);

        Map<String, Object> key = new HashMap<>();
        key.put("k1", true);
        query.put("key", key);

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueInteger() throws Exception {
        final Property property = new Property("query-type-68", "query-entity68");
        property.addTag("t1", "v1");
        property.addKey("k1", "111");
        insertPropertyCheck(property);

        Map<String, Object> query = new HashMap<>();
        query.put("type", property.getType());
        query.put("entity", property.getEntity());
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);

        Map<String, Object> key = new HashMap<>();
        key.put("k1", 111);
        query.put("key", key);

        assertStoredPropertyDoesNotMatchToInserted(property, query);
    }

    private PropertyQuery prepareSimplePropertyQuery(String type, String entity) {
        PropertyQuery query = new PropertyQuery(type, entity);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }

    private void assertStoredPropertyDoesNotMatchToInserted(Property property, Object query) throws Exception {
        Response response = queryProperty(query);
        assertEquals("Fail to execute property query", OK.getStatusCode(), response.getStatus());
        final String given = response.readEntity(String.class);
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue("Property does not match to expected", compareJsonString(expected, given));
    }

    private void assertInsertedPropertyReturned(Property property, PropertyQuery query) throws Exception {
        final String given = queryProperty(query).readEntity(String.class);
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        assertTrue("Inserted property should be received", compareJsonString(expected, given));
    }
}
