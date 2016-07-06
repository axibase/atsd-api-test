package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.property.Property;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.Util.MAX_STORABLE_DATE;
import static com.axibase.tsd.api.Util.addOneMS;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.*;

/**
 * @author Dmitry Korchagin.
 */
@SuppressWarnings("unchecked")
public class PropertyInsertTest extends PropertyMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        assertNotEquals("Managed to insert property with date out of range", response.getStatus(), OK.getStatusCode());

        assertFalse(propertyExist(property));
    }

    @Ignore //behaviour is not defined
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
}
