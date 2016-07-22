package com.axibase.tsd.api.method.property;


import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.property.Property;

import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.*;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */

public class PropertyQueryTest extends PropertyMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /* #NoTicket - base tests*/
    @Test
    public void testStartDateInFuture() throws Exception {
        final Property property = new Property("query-type19", "query-entity19");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getNextDay());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", property.getKey());
        queryObj.put("startDate", property.getDate());
        queryObj.put("interval", new HashMap<String, Object>() {{
            put("unit", "DAY");
            put("count", 2);
        }});

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testExactFalseWildcardNoKey() throws Exception {
        final Property property = new Property("query-type18", "query-entity18");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity18-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");
        secondProperty.addKey("k2", "kv2");
        insertPropertyCheck(secondProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", "*");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("exactMatch", false);


        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, secondProperty));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testExactTrueWildcartNoKey() throws Exception {
        final Property property = new Property("query-type17", "query-entity17");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity17-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");
        secondProperty.addKey("k2", "kv2");
        insertPropertyCheck(secondProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", "*");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("exactMatch", true);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testExactFalseWildcart() throws Exception {
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", "*");
        queryObj.put("key", property.getKey());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("exactMatch", false);


        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, secondProperty));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testExactTrueWildcartKeyMatch() throws Exception {
        final Property property = new Property("query-type15", "query-entity15");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property secondProperty = new Property(null, "query-entity15-2");
        secondProperty.setType(property.getType());
        secondProperty.addTag("t2", "tv2");
        secondProperty.addKey("k2", "kv2");
        insertPropertyCheck(secondProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", "*");
        queryObj.put("key", property.getKey());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("exactMatch", true);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testExactFalseDiffKey() throws Exception {
        final Property property = new Property("query-type14", "query-entity14");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.addKey("k2", "kv2");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("key", new HashMap<String, String>() {{
            put("misskey", "misskey_value");
        }});
        queryObj.put("exactMatch", true);

        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testExactTrueDiffKey() throws Exception {
        final Property property = new Property("query-type13", "query-entity13");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.addKey("k2", "kv2");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("key", new HashMap<String, String>() {{
            put("miss_key", "miss_key_value");
        }});
        queryObj.put("exactMatch", true);

        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testExactTruePartialMatch() throws Exception {
        final Property property = new Property("query-type12", "query-entity12");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.addKey("k2", "kv2");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("key", new HashMap<String, String>() {{
            put("k1", "kv1");
        }});
        queryObj.put("exactMatch", true);

        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testExactFalseFullMatch() throws Exception {
        final Property property = new Property("query-type11", "query-entity11");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("key", property.getKey());
        queryObj.put("exactMatch", false);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testExactTrueFullMatch() throws Exception {
        final Property property = new Property("query-type10", "query-entity10");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", "now");
        queryObj.put("key", property.getKey());
        queryObj.put("exactMatch", true);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntities() throws Exception {
        final Property property = new Property("query-type9", "query-entity9");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        insertPropertyCheck(property);

        final Property lastProperty = new Property(null, "query-entity9-2");
        lastProperty.setType(property.getType());
        lastProperty.addTag("t2", "tv2");


        insertPropertyCheck(lastProperty);

        final Property wrongProperty = new Property(null, "query-wrongentity-9");
        wrongProperty.setType(property.getType());
        wrongProperty.addTag("tw1", "twv1");
        insertPropertyCheck(wrongProperty);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entities", new ArrayList<String>() {{
            add(property.getEntity());
            add(lastProperty.getEntity());
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, lastProperty));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, lastProperty));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("last", false);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, lastProperty));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
        logger.debug("expected: {}", expected);
        logger.debug("given: {}", getProperty(queryObj));
    }

    /* #NoTicket - base tests*/
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("last", true);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(lastProperty));
        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("last", true);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(lastProperty, lastPropertySecond));
        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testStartPastIntervalGiveFuture() throws Exception {
        final Property property = new Property("query-type5", "query-entity5");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", property.getKey());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("interval", new HashMap<String, Object>() {{
            put("count", 1999);
            put("unit", "YEAR");
        }});

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        logger.debug("Expected json: {}", expected);
        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testStartEQDateInterval1MS() throws Exception {
        final Property property = new Property("query-type4", "query-entity4");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testStartPastEndFuture() throws Exception {
        final Property property = new Property("query-type3", "query-entity3");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", property.getKey());
        queryObj.put("startDate", Util.ISOFormat(Util.getPreviousDay()));
        queryObj.put("endDate", Util.ISOFormat(Util.getNextDay()));

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testTypeEntityStartEndExactDefault() throws Exception {
        final Property property = new Property("query-type2", "query-entity2");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", Util.ISOFormat(Util.getNextDay()));

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testTypeEntityStartEndExactFalse() throws Exception {
        final Property property = new Property("query-type1", "query-entity1");
        property.addTag("t1", "tv1");
        property.addKey("k1", "kv1");
        property.setDate(Util.getCurrentDate());
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("exactMatch", false);
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", Util.ISOFormat(Util.getNextDay()));

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testPartKey() throws Exception {
        final Property property = new Property("query-type41.5", "query-entity41.5");
        property.addTag("fs_type", "ext4");
        property.addKey("file_system", "/");
        property.addKey("mount_point", "/sda1");
        property.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("2016-05-25T04:00:00.000Z"));
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("key", new HashMap<String, String>() {{
            put("file_system", "/");
        }});
        queryObj.put("startDate", "2016-05-25T04:00:00Z");
        queryObj.put("endDate", "2016-05-25T05:00:00Z");

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEndDateAbsent() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "test_type");
        request.put("entity", "test_entity");
        request.put("startDate", "2016-05-25T05:00:00Z");

        Response response = getProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Insufficient parameters. One of the following combinations is required: interval, interval + startDate, interval + endDate, startDate + endDate\"}", formatToJsonString(response));
    }


    /* #NoTicket - base tests*/
    @Test
    public void testStartDateAbsent() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "test_type");
        request.put("entity", "test_entity");

        Response response = getProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Insufficient parameters. One of the following combinations is required: interval, interval + startDate, interval + endDate, startDate + endDate\"}", formatToJsonString(response));
    }

    /* #NoTicket - base tests*/
    @Test
    public void testOnlyTypeSpecified() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "test_type");

        Response response = getProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Insufficient parameters. One of the following combinations is required: interval, interval + startDate, interval + endDate, startDate + endDate\"}", formatToJsonString(response));
    }


    /* #NoTicket - base tests*/
    @Test
    public void testEntityFilterEndDateAbsent() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "test_type");
        request.put("type", "test_type");
        request.put("startDate", "2016-06-01T12:04:59.191Z");

        Response response = getProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: Insufficient parameters. One of the following combinations is required: interval, interval + startDate, interval + endDate, startDate + endDate\"}", formatToJsonString(response));
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityFilterAbsent() throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "test_type");
        request.put("startDate", "2016-06-01T12:04:59.191Z");
        request.put("endDate", "2016-06-01T13:04:59.191Z");

        Response response = getProperty(request);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\":\"IllegalArgumentException: entity or entities or entityGroup or entityExpression must not be empty\"}", formatToJsonString(response));
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTags() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("query-entity20");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map<String, String> tags = new HashMap<String, String>() {{
            put("t1", "v1");
            put("t2", "v2");
        }};
        entity.setTags(tags);
        property.setTags(tags);
        EntityMethod.createOrReplaceEntityCheck(entity);

        assertTrue(propertyExist(property));
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsTagsAsKeyExactTrue() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("query-entity21");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map<String, String> tags = new HashMap<String, String>() {{
            put("t1", "v1");
            put("t2", "v2");
        }};
        entity.setTags(tags);
        property.setTags(tags);
        EntityMethod.createOrReplaceEntityCheck(entity);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", "$entity_tags");
        queryObj.put("entity", entity.getName());
        queryObj.put("key", tags);
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", true);


        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsTagsAsPartKeyExactTrue() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("query-entity22");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map<String, String> tags = new HashMap<String, String>() {{
            put("t1", "v1");
            put("t2", "v2");
        }};
        entity.setTags(tags);
        property.setTags(tags);
        EntityMethod.createOrReplaceEntityCheck(entity);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", "$entity_tags");
        queryObj.put("entity", entity.getName());
        queryObj.put("key", new HashMap<String, String>() {{
            put("t1", "v1");
        }});
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", false);


        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsEmptyKeyExactFalse() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("query-entity23");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map<String, String> tags = new HashMap<String, String>() {{
            put("t1", "v1");
            put("t2", "v2");
        }};
        entity.setTags(tags);
        property.setTags(tags);
        EntityMethod.createOrReplaceEntityCheck(entity);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", "$entity_tags");
        queryObj.put("entity", entity.getName());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", false);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsEmptyKeyExactTrue() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("query-entity24");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map<String, String> tags = new HashMap<String, String>() {{
            put("t1", "v1");
            put("t2", "v2");
        }};
        entity.setTags(tags);
        property.setTags(tags);
        EntityMethod.createOrReplaceEntityCheck(entity);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", "$entity_tags");
        queryObj.put("entity", entity.getName());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("exactMatch", false);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsKeyExpression() throws Exception {
        final String entityTagsType = "$entity_tags";
        final Entity entity = new Entity("query-entity25");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map<String, String> tags = new HashMap<String, String>() {{
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
        queryObj.put("keyTagExpression", "tags.t1 == 'v1'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsKeyExpressionNoMatch() throws Exception {
        final String entityTagsType = "$entity_tags";
        EntityMethod entityMethod = new EntityMethod();
        final Entity entity = new Entity("query-entity26");
        final Property property = new Property();
        property.setType(entityTagsType);
        property.setEntity(entity.getName());
        Map<String, String> tags = new HashMap<String, String>() {{
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
        queryObj.put("keyTagExpression", "tags.t1 == 'v2'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);

        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testEntityWildcardTagsAsKey() throws Exception {
        final String entityTagType = "$entity_tags";
        Entity entity1 = new Entity("wck-query-entity33");
        entity1.addTag("wct1", "wcv1");
        Entity entity2 = new Entity("wck-query-entity34");
        entity2.addTag("wct1", "wcV1");
        Entity entity3 = new Entity("wck-query-entity35");
        entity3.addTag("wct1", "wcv1");
        entity3.addTag("wct2", "wcv2");
        Entity entity4 = new Entity("wck-query-entity36");
        entity4.addTag("wct2", "wcV2");
        EntityMethod.createOrReplaceEntityCheck(entity1);
        EntityMethod.createOrReplaceEntityCheck(entity2);
        EntityMethod.createOrReplaceEntityCheck(entity3);
        EntityMethod.createOrReplaceEntityCheck(entity4);


        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", entityTagType);
        queryObj.put("entity", "wck-*");
        queryObj.put("key", entity1.getTags());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);

        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }


    /* #NoTicket - base tests*/
    @Test
    public void testEntityWildcardExpression() throws Exception {
        final String entityTagType = "$entity_tags";
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
        property3.setType(entityTagType);
        property3.setEntity(entity3.getName());
        property3.setTags(entity3.getTags());


        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", entityTagType);
        queryObj.put("entity", "wcke-*");
        queryObj.put("keyTagExpression", "keys.wc2t1 = 'wc2V1' OR tags.wc2t2 = 'wc2v2'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);

        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property3));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);

    }


    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsExpressionCaseSensitiveValue() throws Exception {
        final String entityTagType = "$entity_tags";
        Entity entity = new Entity("query-entity41");
        entity.addTag("t1", "tv1");
        EntityMethod.createOrReplaceEntityCheck(entity);


        final Property property = new Property();
        property.setType(entityTagType);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", entityTagType);
        queryObj.put("entity", entity.getName());
        queryObj.put("keyTagExpression", "tags.t1 == 'tV1'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        JSONAssert.assertEquals("[]", formatToJsonString(getProperty(queryObj)), false);
    }

    /* #NoTicket - base tests*/
    @Test
    public void testEntityTagsKeyTagExpressionCaseInsensitiveName() throws Exception {
        final String entityTagType = "$entity_tags";
        Entity entity = new Entity("query-entity42");
        entity.addTag("t1", "tv1");
        EntityMethod.createOrReplaceEntityCheck(entity);


        final Property property = new Property();
        property.setType(entityTagType);
        property.setEntity(entity.getName());
        property.setTags(entity.getTags());

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", entityTagType);
        queryObj.put("entity", entity.getName());
        queryObj.put("keyTagExpression", "tags.T1 == 'tv1'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "tags.t1 == 'tv1' OR keys.k3 == 'kv3'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Arrays.asList(property, property2, property3));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }


    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "tags.t1 == 'tv1' AND keys.k2 == 'kv2'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));
        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "tags.t1 LIKE 'tv*'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "keys.k1 LIKE 'kv*'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "tags.t1 == keys.k1");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "tags.t1 != keys.k1");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "keys.k2 == ''");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "keys.k2 != ''");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "tags.t2 == ''");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
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

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "tags.t2 != ''");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property2));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
    @Test
    public void testKeyTagExpressionLowerTag() throws Exception {
        final Property property = new Property("query-type51", "query-entity51");
        property.addTag("t1", "TV1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "lower(tags.t1) == 'tv1'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
    @Test
    public void testKeyTagExpressionLowerKey() throws Exception {
        final Property property = new Property("query-type52", "query-entity52");
        property.addTag("t1", "tv1");
        property.addKey("k1", "KV1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "lower(keys.k1) == 'kv1'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
    @Test
    public void testKeyTagExpressionUpperTag() throws Exception {
        final Property property = new Property("query-type53", "query-entity53");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "upper(tags.t1) == 'TV1'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));

        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2908
    @Test
    public void testKeyTagExpressionUpperKey() throws Exception {
        final Property property = new Property("query-type54", "query-entity54");
        property.addTag("t1", "tv1");
        property.addKey("k1", "KV1");
        insertPropertyCheck(property);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("keyTagExpression", "upper(keys.k1) == 'KV1'");
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);


        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        JSONAssert.assertEquals(expected, formatToJsonString(getProperty(queryObj)), false);
    }

    //#2946
    @Test
    public void testLimit1() throws Exception {
        final Property property = new Property("query-type55", "query-entity55");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        property2.addKey("k2", "kv2");
        insertPropertyCheck(property2);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("limit", 1);

        assertEquals(1, calculateJsonArraySize(formatToJsonString(getProperty(queryObj))));
    }


    //#2946
    @Test
    public void testLimit2() throws Exception {
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


        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("limit", 2);

        assertEquals(2, calculateJsonArraySize(formatToJsonString(getProperty(queryObj))));
    }

    //#2946
    @Test
    public void testLimit0() throws Exception {
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


        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("limit", 0);

        assertEquals(3, calculateJsonArraySize(formatToJsonString(getProperty(queryObj))));
    }

    //#2946
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


        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", Util.MIN_QUERYABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);

        queryObj.put("limit", -1);
        assertEquals(3, calculateJsonArraySize(formatToJsonString(getProperty(queryObj))));

        queryObj.put("limit", -5);
        assertEquals(3, calculateJsonArraySize(formatToJsonString(getProperty(queryObj))));
    }
}
