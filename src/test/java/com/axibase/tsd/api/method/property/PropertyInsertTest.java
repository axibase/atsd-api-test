package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.ErrorTemplate.DATE_FILTER_INVALID_FORMAT;
import static com.axibase.tsd.api.util.Mocks.MAX_STORABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_STORABLE_DATE;
import static com.axibase.tsd.api.util.Util.addOneMS;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.*;

public class PropertyInsertTest extends PropertyMethod {

    /**
     * #NoTicket - base tests
     */
    @Test
    public void testMultipleInsertDifferentKeyGetAll() throws Exception {
        final Property firstProperty = new Property("insert-type4", "insert-entity4");
        firstProperty.addTag("t1", "v1");
        firstProperty.addKey("k1", "v1");
        final Property secondProperty = new Property("insert-type5", "insert-entity5");
        secondProperty.addTag("t1", "v1");
        secondProperty.addKey("k1", "v1");
        final Property thirdProperty = new Property("insert-type6", "insert-entity6");
        thirdProperty.addTag("t1", "v1");
        thirdProperty.addKey("k1", "v1");

        assertEquals(OK.getStatusCode(), insertProperty(firstProperty, secondProperty, thirdProperty).getStatus());
        assertTrue(propertyExist(firstProperty));
        assertTrue(propertyExist(secondProperty));
        assertTrue(propertyExist(thirdProperty));
    }


    /**
     * #NoTicket - base tests
     */
    @Test
    public void testMultipleInsertSameTypeEntityKey() throws Exception {
        final long firstTime = System.currentTimeMillis() - 5;
        final long secondTime = System.currentTimeMillis();

        final Property property = new Property("insert-type2", "insert-entity2");
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(firstTime);

        final Property updatedProperty = new Property();
        updatedProperty.setType(property.getType());
        updatedProperty.setEntity(property.getEntity());
        updatedProperty.setKey(property.getKey());
        updatedProperty.setTags(new HashMap<String, String>() {{
            put("nt1", "ntv1");
        }});
        updatedProperty.setDate(secondTime);

        assertEquals(OK.getStatusCode(), insertProperty(property, updatedProperty).getStatus());

        assertTrue("Updated property should exist", propertyExist(updatedProperty, true));
        assertFalse("Old property should not exist", propertyExist(property, true));
    }

    /**
     * #NoTicket - base tests
     */
    @Test
    public void testSameTypeEntityKey() throws Exception {
        final Property property = new Property("insert-type1", "insert-entity1");
        final long firstTime = System.currentTimeMillis() - 5;
        final long secondTime = System.currentTimeMillis();
        property.addTag("t1", "v1");
        property.addKey("k1", "v1");
        property.setDate(firstTime);

        final Property updatedProperty = new Property();
        updatedProperty.setType(property.getType());
        updatedProperty.setEntity(property.getEntity());
        updatedProperty.setKey(property.getKey());
        updatedProperty.setTags(new HashMap<String, String>() {{
            put("nt1", "ntv1");
        }});
        updatedProperty.setDate(secondTime);

        insertPropertyCheck(property);
        insertPropertyCheck(updatedProperty);

        assertTrue("Updated property should exist", propertyExist(updatedProperty, true));
        assertFalse("Old property should not exist", propertyExist(property, true));
    }

    /**
     * #NoTicket - base tests
     */
    @Test
    public void testExtraKeyInRoot() throws Exception {
        final Property property = new Property("insert-type3", "insert-entity3");
        property.setDate("2014-02-02T00:00:00.000Z");
        property.addTag("t1", "tv1");

        final Map<String, Object> insertObj = new HashMap<>();
        insertObj.put("type", property.getType());
        insertObj.put("entity", property.getEntity());
        insertObj.put("tags", property.getTags());
        insertObj.put("date", property.getDate());
        insertObj.put("extraField", "extraValue");

        Response response = insertProperty(insertObj);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

        assertFalse("Inserted property should not exist", propertyExist(property));

    }

    /**
     * #NoTicket - base tests
     */
    @Test
    public void testNoKeySamePropertyOverwrite() throws Exception {
        final Property property = new Property("insert-type7", "insert-entity7");
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.addTag("t2", "tv2");
        insertPropertyCheck(property2);

        assertFalse(propertyExist(property));
        assertTrue(propertyExist(property2));
    }


    /**
     * #2957
     */
    @Test
    public void testTimeRangeMinSaved() throws Exception {
        Property property = new Property("t-time-range-p-1", "e-time-range--1");
        property.addTag("ttr-t", "ttr-v");
        property.setDate(MIN_STORABLE_DATE);

        Response response = insertProperty(property);
        assertEquals("Failed to insert property", response.getStatus(), OK.getStatusCode());

        assertTrue(propertyExist(property));
    }

    /**
     * #2957
     */
    @Test
    public void testTimeRangeMaxTimeSaved() throws Exception {
        Property property = new Property("t-time-range-p-3", "e-time-range-p-3");
        property.addTag("ttr-t", "ttr-v");
        property.setDate(MAX_STORABLE_DATE);

        Response response = insertProperty(property);
        assertEquals("Failed to insert property", response.getStatus(), OK.getStatusCode());

        assertTrue(propertyExist(property));
    }

    /**
     * #2957
     */
    @Test
    public void testTimeRangeMaxTimeOverflow() throws Exception {
        Property property = new Property("t-time-range-p-4", "e-time-range-p-4");
        property.addTag("ttr-t", "ttr-v");
        property.setDate(addOneMS(MAX_STORABLE_DATE));

        Response response = insertProperty(property);
        assertNotSame("Managed to insert property with date out of range", response.getStatus(), OK.getStatusCode());

        assertFalse(propertyExist(property));
    }

    /**
     * #2957
     */
    @Test
    public void testSameTimeSamePropertyConjunction() throws Exception {
        final long timeMillis = System.currentTimeMillis();
        final Property property = new Property("insert-type8", "insert-entity8");
        property.addTag("t1", "tv1");
        property.setDate(timeMillis);
        insertPropertyCheck(property);

        final Property property2 = new Property();
        property2.setType(property.getType());
        property2.setEntity(property.getEntity());
        property2.setDate(timeMillis);
        property2.addTag("t2", "tv2");
        insertPropertyCheck(property2);

        final Property resultProperty = new Property();
        resultProperty.setType(property.getType());
        resultProperty.setEntity(property.getEntity());
        resultProperty.setDate(timeMillis);
        resultProperty.addTag("t1", "tv1");
        resultProperty.addTag("t2", "tv2");

        assertTrue(propertyExist(resultProperty));
    }

    /**
     * #2850
     */
    @Test
    public void testISOTimezoneZ() throws Exception {
        Property property = new Property("test1", "property-insert-test-isoz");
        property.addTag("test", "test");
        property.setDate("2016-07-21T00:00:00Z");

        insertProperty(property);

        PropertyQuery propertyQuery = new PropertyQuery();

        propertyQuery.setEntity("property-insert-test-isoz");
        propertyQuery.setStartDate("2016-07-21T00:00:00.000Z");
        propertyQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));
        propertyQuery.setType(property.getType());

        List<Property> storedPropertyList = queryProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        Assert.assertEquals(property.getEntity(), storedProperty.getEntity(), "Incorrect property entity");
        Assert.assertEquals(property.getTags(), storedProperty.getTags(), "Incorrect property tags");
        Assert.assertEquals(propertyQuery.getStartDate(), storedProperty.getDate(), "Incorrect property date");
    }

    /**
     * #2850
     */
    @Test
    public void testISOTimezonePlusHourMinute() throws Exception {
        String entityName = "property-insert-test-iso+hm";
        Property property = new Property("test2", entityName);
        property.addTag("test", "test");
        property.setDate("2016-07-21T01:23:00+01:23");

        insertProperty(property);

        PropertyQuery propertyQuery = new PropertyQuery();
        propertyQuery.setType(property.getType());
        propertyQuery.setEntity(entityName);
        propertyQuery.setStartDate("2016-07-21T00:00:00.000Z");
        propertyQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        List<Property> storedPropertyList = queryProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        Assert.assertEquals(property.getEntity(), storedProperty.getEntity(), "Incorrect property entity");
        Assert.assertEquals(property.getTags(), storedProperty.getTags(), "Incorrect property tags");
        Assert.assertEquals(propertyQuery.getStartDate(), storedProperty.getDate(), "Incorrect property date");
    }

    /**
     * #2850
     */
    @Test
    public void testISOTimezoneMinusHourMinute() throws Exception {
        String entityName = "property-insert-test-iso-hm";
        Property property = new Property("test3", entityName);
        property.addTag("test", "test");
        property.setDate("2016-07-20T22:37:00-01:23");

        insertProperty(property);

        PropertyQuery propertyQuery = new PropertyQuery();
        propertyQuery.setType(property.getType());
        propertyQuery.setEntity(entityName);
        propertyQuery.setStartDate("2016-07-21T00:00:00.000Z");
        propertyQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        List<Property> storedPropertyList = queryProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        Assert.assertEquals(property.getEntity(), storedProperty.getEntity(), "Incorrect property entity");
        Assert.assertEquals(property.getTags(), storedProperty.getTags(), "Incorrect property tags");
        Assert.assertEquals(propertyQuery.getStartDate(), storedProperty.getDate(), "Incorrect property date");
    }

    /**
     * #2850
     */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        String entityName = "property-insert-test-localtime";
        String type = "test4";

        Property property = new Property(type, entityName);
        property.addTag("test", "test");
        property.setDate("2016-06-09 20:00:00");

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_INVALID_FORMAT, extractErrorMessage(response));

    }

    /**
     * #2850
     */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        String entityName = "property-insert-test-xx-timezone";
        String type = "test5";

        Property property = new Property(type, entityName);
        property.addTag("test", "test");
        property.setDate("2016-06-09T09:50:00-1010");

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_INVALID_FORMAT, extractErrorMessage(response));
    }

    /**
     * #2850
     */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String entityName = "property-insert-test-milliseconds";
        String type = "test6";

        Property property = new Property(type, entityName);
        property.addTag("test", "test");
        property.setDate("1465502400000");

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_INVALID_FORMAT, extractErrorMessage(response));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueNull() throws Exception {
        Property property = new Property("insert-property-t-1", "insert-property-e-1");
        property.addKey("k1", null);
        property.addTag("t1", "tv1");
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setKey(new HashMap<String, String>());

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testTagValueContainNull() throws Exception {
        Property property = new Property("insert-property-t-2", "insert-property-e-2");
        property.addTag("t1", "tv1");
        property.addTag("t2", null);
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setTags(null);
        storedProperty.addTag("t1", "tv1");

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testTagValueNull() throws Exception {
        Property property = new Property("insert-property-t-3", "insert-property-e-3");
        property.addTag("t1", null);
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        assertEquals("Query should fail if tag contain only null values", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertFalse("Inserted property should not be stored", propertyExist(property));

    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueEmpty() throws Exception {
        Property property = new Property("insert-property-t-4", "insert-property-e-4");
        property.addKey("k1", "");
        property.addTag("t1", "tv1");
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setKey(null);

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testTagValueEmpty() throws Exception {
        Property property = new Property("insert-property-t-41", "insert-property-e-41");
        property.addTag("t1", "tv1");
        property.addTag("t2", "");
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setTags(null);
        storedProperty.addTag("t1", "tv1");

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueContainSpace() throws Exception {
        Property property = new Property("insert-property-t-5", "insert-property-e-5");
        property.addKey("k1", " spaced ");
        property.addTag("t1", "tv1");
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setKey(null);
        storedProperty.addKey("k1", "spaced");

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueSpaces() throws Exception {
        Property property = new Property("insert-property-t-6", "insert-property-e-6");
        property.addKey("k1", "   ");
        property.addTag("t1", "tv1");
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setKey(null);

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testTagValueContainSpace() throws Exception {
        Property property = new Property("insert-property-t-7", "insert-property-e-7");
        property.addTag("t1", " tv1 ");
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setTags(null);
        storedProperty.addTag("t1", "tv1");

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testTagValueSpaces() throws Exception {
        Property property = new Property("insert-property-t-8", "insert-property-e-8");
        property.addTag("t1", "   ");
        property.addTag("t2", "tv2");
        property.setDate("2016-06-09T09:50:00.000Z");

        Response response = insertProperty(property);

        Property storedProperty = new Property(property);
        storedProperty.setTags(null);
        storedProperty.addTag("t2", "tv2");

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueInteger() throws Exception {
        final Property storedProperty = new Property("insert-property-t-9", "insert-property-e-9");
        storedProperty.addTag("t1", "tv1");
        storedProperty.addKey("k1", "111");

        Map<String, Object> property = new HashMap<>();
        property.put("type", storedProperty.getType());
        property.put("entity", storedProperty.getEntity());
        property.put("tags", storedProperty.getTags());

        Map<String, Object> key = new HashMap<>();
        key.put("k1", 111);
        property.put("key", key);

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testKeyValueBoolean() throws Exception {
        final Property storedProperty = new Property("insert-property-t-10", "insert-property-e-10");
        storedProperty.addTag("t1", "tv1");
        storedProperty.addKey("k1", "true");

        Map<String, Object> property = new HashMap<>();
        property.put("type", storedProperty.getType());
        property.put("entity", storedProperty.getEntity());
        property.put("tags", storedProperty.getTags());

        Map<String, Object> key = new HashMap<>();
        key.put("k1", true);
        property.put("key", key);

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testTagValueInteger() throws Exception {
        final Property storedProperty = new Property("insert-property-t-11", "insert-property-e-11");
        storedProperty.addTag("t1", "111");

        Map<String, Object> property = new HashMap<>();
        property.put("type", storedProperty.getType());
        property.put("entity", storedProperty.getEntity());

        Map<String, Object> tags = new HashMap<>();
        tags.put("t1", 111);
        property.put("tags", tags);

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }

    /**
     * #2416
     */
    @Test
    public void testTagValueBoolean() throws Exception {
        final Property storedProperty = new Property("insert-property-t-12", "insert-property-e-12");
        storedProperty.addTag("t1", "true");

        Map<String, Object> property = new HashMap<>();
        property.put("type", storedProperty.getType());
        property.put("entity", storedProperty.getEntity());

        Map<String, Object> tags = new HashMap<>();
        tags.put("t1", true);
        property.put("tags", tags);

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to get inserted properties", propertyExist(storedProperty));
    }


}
