package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.DateFilter;
import com.axibase.tsd.api.model.EntityFilter;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.Util.MAX_STORABLE_DATE;
import static com.axibase.tsd.api.Util.addOneMS;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.*;

/**
 * @author Dmitry Korchagin.
 */
public class PropertyInsertTest extends PropertyMethod {

    /* #NoTicket - base tests*/
    @Test
    public void test_MultipleInsertDifferentKey_GetAll() throws IOException {
        final Property firstProperty = new Property("insert-type4", "insert-entity4");
        firstProperty.addTag("t1", "v1");
        firstProperty.addKey("k1", "v1");
        final Property secondProperty = new Property("insert-type5", "insert-entity5");
        secondProperty.addTag("t1", "v1");
        secondProperty.addKey("k1", "v1");
        final Property thirdProperty = new Property("insert-type6", "insert-entity6");
        thirdProperty.addTag("t1", "v1");
        thirdProperty.addKey("k1", "v1");

        Map<String, Object> insertFirstObj = new HashMap<>();
        insertFirstObj.put("type", firstProperty.getType());
        insertFirstObj.put("entity", firstProperty.getEntity());
        insertFirstObj.put("key", firstProperty.getKey());
        insertFirstObj.put("tags", firstProperty.getTags());

        Map<String, Object> insertSecondObj = new HashMap<>();
        insertSecondObj.put("type", secondProperty.getType());
        insertSecondObj.put("entity", secondProperty.getEntity());
        insertSecondObj.put("key", secondProperty.getKey());
        insertSecondObj.put("tags", secondProperty.getTags());

        Map<String, Object> insertThirdObj = new HashMap<>();
        insertThirdObj.put("type", thirdProperty.getType());
        insertThirdObj.put("entity", thirdProperty.getEntity());
        insertThirdObj.put("key", thirdProperty.getKey());
        insertThirdObj.put("tags", thirdProperty.getTags());

        assertEquals(OK.getStatusCode(), insertProperty(insertFirstObj, insertSecondObj, insertThirdObj).getStatus());
        assertTrue(propertyExist(firstProperty));
        assertTrue(propertyExist(secondProperty));
        assertTrue(propertyExist(thirdProperty));
    }


    /* #NoTicket - base tests*/
    @Test
    public void testMultipleInsertSameTypeEntityKey() throws IOException {
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

        Map<String, Object> insertFirstObj = new HashMap<>();
        insertFirstObj.put("type", property.getType());
        insertFirstObj.put("entity", property.getEntity());
        insertFirstObj.put("key", property.getKey());
        insertFirstObj.put("tags", property.getTags());
        insertFirstObj.put("date", property.getDate());

        Map<String, Object> insertUpdatedObj = new HashMap<>();
        insertUpdatedObj.put("type", updatedProperty.getType());
        insertUpdatedObj.put("entity", updatedProperty.getEntity());
        insertUpdatedObj.put("key", updatedProperty.getKey());
        insertUpdatedObj.put("tags", updatedProperty.getTags());
        insertUpdatedObj.put("date", updatedProperty.getDate());
        assertEquals(OK.getStatusCode(), insertProperty(insertFirstObj, insertUpdatedObj).getStatus());

        assertTrue(propertyExist(updatedProperty, true));
        assertFalse(propertyExist(property, true));
    }

    /* #NoTicket - base tests*/
    @Test
    public void testSameTypeEntityKey() throws IOException {
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

        Map<String, Object> insertFirstObj = new HashMap<>();
        insertFirstObj.put("type", property.getType());
        insertFirstObj.put("entity", property.getEntity());
        insertFirstObj.put("key", property.getKey());
        insertFirstObj.put("tags", property.getTags());
        insertFirstObj.put("date", property.getDate());
        insertProperty(insertFirstObj);
        assertTrue(propertyExist(property, true));

        Map<String, Object> insertUpdatedObj = new HashMap<>();
        insertUpdatedObj.put("type", updatedProperty.getType());
        insertUpdatedObj.put("entity", updatedProperty.getEntity());
        insertUpdatedObj.put("key", updatedProperty.getKey());
        insertUpdatedObj.put("tags", updatedProperty.getTags());
        insertUpdatedObj.put("date", updatedProperty.getDate());
        insertProperty(insertUpdatedObj);
        assertTrue(propertyExist(updatedProperty, true));
        assertFalse(propertyExist(property, true));
    }

    /* #NoTicket - base tests*/
    @Test
    public void testExtraKeyInRoot() throws IOException {
        final Property property = new Property("insert-type3", "insert-entity3");

        final Map<String, Object> insertObj = new HashMap<>();
        insertObj.put("type", property.getType());
        insertObj.put("entity", property.getEntity());
        insertObj.put("key", property.getKey());
        insertObj.put("tags", property.getTags());
        insertObj.put("date", property.getDate());
        insertObj.put("extraField", "extraValue");

        Response response = insertProperty(insertObj);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).contains("UnrecognizedPropertyException"));

        assertFalse(propertyExist(property));

    }

    /* #NoTicket - base tests*/
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

    /* #2957 */
    @Test
    public void testTimeRangeMinSaved() throws Exception {
        Property property = new Property("t-time-range-p-1", "e-time-range--1");
        property.addTag("ttr-t", "ttr-v");
        property.setDate(Util.MIN_STORABLE_DATE);

        Response response = insertProperty(property);
        Thread.sleep(1000L);
        assertEquals("Failed to insert property", response.getStatus(), OK.getStatusCode());

        assertTrue(propertyExist(property));
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxTimeSaved() throws Exception {
        Property property = new Property("t-time-range-p-3", "e-time-range-p-3");
        property.addTag("ttr-t", "ttr-v");
        property.setDate(MAX_STORABLE_DATE);

        Response response = insertProperty(property);
        assertEquals("Failed to insert property", response.getStatus(), OK.getStatusCode());

        assertTrue(propertyExist(property));
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxTimeOverflow() throws Exception {
        Property property = new Property("t-time-range-p-4", "e-time-range-p-4");
        property.addTag("ttr-t", "ttr-v");
        property.setDate(addOneMS(MAX_STORABLE_DATE));

        Response response = insertProperty(property);
        assertNotSame("Managed to insert property with date out of range", response.getStatus(), OK.getStatusCode());

        assertFalse(propertyExist(property));
    }

    @Test(enabled = false)//#2957
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

    /* #2850 */
    @Test
    public void testISOTimezoneZ() throws Exception {
        Property property = new Property("test1", "property-insert-test-isoz");
        property.addTag("test", "test");
        property.setDate("2016-07-21T00:00:00Z");

        insertProperty(property);

        PropertyQuery propertyQuery = new PropertyQuery();
        EntityFilter entityFilter = new EntityFilter();
        entityFilter.setEntity("property-insert-test-isoz");

        DateFilter dateFilter = new DateFilter();
        String date = "2016-07-21T00:00:00.000Z";
        dateFilter.setStartDate(date);
        dateFilter.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        propertyQuery.setEntityFilter(entityFilter);
        propertyQuery.setDateFilter(dateFilter);
        propertyQuery.setType(property.getType());

        List<Property> storedPropertyList = getProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        Assert.assertEquals(property.getEntity(), storedProperty.getEntity(), "Incorrect property entity");
        Assert.assertEquals(property.getTags(), storedProperty.getTags(), "Incorrect property tags");
        Assert.assertEquals(date, storedProperty.getDate(), "Incorrect property date");
    }

    /* #2850 */
    @Test
    public void testISOTimezonePlusHourMinute() throws Exception {
        String entityName = "property-insert-test-iso+hm";
        Property property = new Property("test2", entityName);
        property.addTag("test", "test");
        property.setDate("2016-07-21T01:23:00+01:23");

        insertProperty(property);

        PropertyQuery propertyQuery = new PropertyQuery();
        EntityFilter entityFilter = new EntityFilter();
        entityFilter.setEntity(entityName);

        DateFilter dateFilter = new DateFilter();
        String date = "2016-07-21T00:00:00.000Z";
        dateFilter.setStartDate(date);
        dateFilter.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        propertyQuery.setEntityFilter(entityFilter);
        propertyQuery.setDateFilter(dateFilter);
        propertyQuery.setType(property.getType());

        List<Property> storedPropertyList = getProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        Assert.assertEquals(property.getEntity(), storedProperty.getEntity(), "Incorrect property entity");
        Assert.assertEquals(property.getTags(), storedProperty.getTags(), "Incorrect property tags");
        Assert.assertEquals(date, storedProperty.getDate(), "Incorrect property date");
    }

    /* #2850 */
    @Test
    public void testISOTimezoneMinusHourMinute() throws Exception {
        String entityName = "property-insert-test-iso-hm";
        Property property = new Property("test3", entityName);
        property.addTag("test", "test");
        property.setDate("2016-07-20T22:37:00-01:23");

        insertProperty(property);

        PropertyQuery propertyQuery = new PropertyQuery();
        EntityFilter entityFilter = new EntityFilter();
        entityFilter.setEntity(entityName);

        DateFilter dateFilter = new DateFilter();
        String date = "2016-07-21T00:00:00.000Z";
        dateFilter.setStartDate(date);
        dateFilter.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        propertyQuery.setEntityFilter(entityFilter);
        propertyQuery.setDateFilter(dateFilter);
        propertyQuery.setType(property.getType());

        List<Property> storedPropertyList = getProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        Assert.assertEquals(property.getEntity(), storedProperty.getEntity(), "Incorrect property entity");
        Assert.assertEquals(property.getTags(), storedProperty.getTags(), "Incorrect property tags");
        Assert.assertEquals(date, storedProperty.getDate(), "Incorrect property date");
    }

    /* #2850 */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        String entityName = "property-insert-test-localtime";
        String type = "test4";

        Property property = new Property(type, entityName);
        property.addTag("test", "test");
        property.setDate("2016-06-09 20:00:00");

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Invalid date format\"}", response.readEntity(String.class), true);

    }

    /* #2850 */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        String entityName = "property-insert-test-xx-timezone";
        String type = "test5";

        Property property = new Property(type, entityName);
        property.addTag("test", "test");
        property.setDate("2016-06-09T09:50:00-1010");

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Invalid date format\"}", response.readEntity(String.class), true);
    }

    /* #2850 */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String entityName = "property-insert-test-milliseconds";
        String type = "test6";

        Property property = new Property(type, entityName);
        property.addTag("test", "test");
        property.setDate("1465502400000");

        Response response = insertProperty(property);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Invalid date format\"}", response.readEntity(String.class), true);
    }

}
