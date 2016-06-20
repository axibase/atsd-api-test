package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.transport.http.AtsdHttpResponse;
import com.axibase.tsd.api.transport.http.HTTPMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

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

        insertProperties(insertFirstObj, insertSecondObj, insertThirdObj);
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

        deleteProperties(property, updatedProperty);

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
        insertProperties(insertFirstObj, insertUpdatedObj);

        assertTrue(propertyExist(updatedProperty, false));
        assertFalse(propertyExist(property, false));
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

        deleteProperties(property, updatedProperty);

        Map<String, Object> insertFirstObj = new HashMap<>();
        insertFirstObj.put("type", property.getType());
        insertFirstObj.put("entity", property.getEntity());
        insertFirstObj.put("key", property.getKey());
        insertFirstObj.put("tags", property.getTags());
        insertFirstObj.put("date", property.getDate());
        insertProperties(insertFirstObj);
        assertTrue(propertyExist(property, false));

        Map<String, Object> insertUpdatedObj = new HashMap<>();
        insertUpdatedObj.put("type", updatedProperty.getType());
        insertUpdatedObj.put("entity", updatedProperty.getEntity());
        insertUpdatedObj.put("key", updatedProperty.getKey());
        insertUpdatedObj.put("tags", updatedProperty.getTags());
        insertUpdatedObj.put("date", updatedProperty.getDate());
        insertProperties(insertUpdatedObj);
        assertTrue(propertyExist(updatedProperty, false));
        assertFalse(propertyExist(property, false));
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

        JSONArray request = new JSONArray() {{
            add(new JSONObject(insertObj));
        }};
        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_PROPERTY_INSERT, request.toJSONString());
        assertEquals(500, response.getCode());
        assertTrue(response.getBody().contains("UnrecognizedPropertyException"));

        assertFalse(propertyExist(property));

    }

    private void insertProperties(final Map... insertObjects) throws IOException {
        JSONArray insertArray = new JSONArray();
        for (Map obj : insertObjects) {
            insertArray.add(new JSONObject(obj));
        }
        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_PROPERTY_INSERT, insertArray.toJSONString());
        assertEquals("Fail to execute insert query", 200, response.getCode());
    }


}
