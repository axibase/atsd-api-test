package com.axibase.tsd.api.method.property;


import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.property.Property;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */

public class PropertyQueryOffsetTest extends PropertyMethod {
    private final static String propertyType = "query-offset-type1";
    private final static String entityName = "query-offset-entity1";
    private final static Property propertyPast;
    private final static Property propertyMiddle;
    private final static Property propertyCurrent1;
    private final static Property propertyCurrent2;

    static {
        long offsetPastMillis = 20L;
        long offsetMiddleMillis = 10L;
        long currentTimeMillis = System.currentTimeMillis();

        propertyPast = new Property(propertyType, entityName);
        propertyPast.addTag("t1", "tv1");
        propertyPast.addKey("k1", "KV1");
        propertyPast.setDate(currentTimeMillis - offsetPastMillis);

        propertyMiddle = new Property();
        propertyMiddle.setType(propertyType);
        propertyMiddle.setEntity(entityName);
        propertyMiddle.addTag("t2", "tv2");
        propertyMiddle.addKey("k2", "KV2");
        propertyMiddle.setDate(currentTimeMillis - offsetMiddleMillis);

        propertyCurrent1 = new Property();
        propertyCurrent1.setType(propertyType);
        propertyCurrent1.setEntity(entityName);
        propertyCurrent1.addTag("t3", "tv3");
        propertyCurrent1.addKey("k3", "KV3");
        propertyCurrent1.setDate(currentTimeMillis);

        propertyCurrent2 = new Property();
        propertyCurrent2.setType(propertyType);
        propertyCurrent2.setEntity(entityName);
        propertyCurrent2.addTag("t3", "tv3");
        propertyCurrent2.addKey("k4", "KV4");
        propertyCurrent2.setDate(currentTimeMillis);
    }


    @BeforeClass
    public static void prepareProperty() throws IOException {
        insertPropertyCheck(propertyPast);
        insertPropertyCheck(propertyMiddle);
        insertPropertyCheck(propertyCurrent1);
        insertPropertyCheck(propertyCurrent2);
    }

    //#2947
    @Test
    public void testOffset0() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", propertyType);
        queryObj.put("entity", entityName);
        queryObj.put("startDate", Util.getMinDate());
        queryObj.put("endDate", Util.getMaxDate());
        queryObj.put("offset", 0);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyCurrent1, propertyCurrent2));

        JSONAssert.assertEquals(expected, getProperty(queryObj).readEntity(String.class), false);
    }

    //#2947
    @Test
    public void testOffsetNegative() throws Exception {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", propertyType);
        queryObj.put("entity", entityName);
        queryObj.put("startDate", Util.getMinDate());
        queryObj.put("endDate", Util.getMaxDate());
        queryObj.put("offset", -1);

        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyPast, propertyMiddle, propertyCurrent1, propertyCurrent2));

        JSONAssert.assertEquals(expected, getProperty(queryObj).readEntity(String.class), false);

        queryObj.put("offset", -5);
        JSONAssert.assertEquals(expected, getProperty(queryObj).readEntity(String.class), false);

        queryObj.put("offset", -50);
        JSONAssert.assertEquals(expected, getProperty(queryObj).readEntity(String.class), false);
    }

    //#2947
    @Test
    public void testOffsetPositive() throws Exception {
        Long middleMillis = Util.getDate(propertyMiddle.getDate()).getTime();
        Long presentMillis = Util.getDate(propertyCurrent1.getDate()).getTime();

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", propertyType);
        queryObj.put("entity", entityName);
        queryObj.put("startDate", Util.getMinDate());
        queryObj.put("endDate", Util.getMaxDate());
        queryObj.put("offset", presentMillis - middleMillis);


        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyMiddle, propertyCurrent1, propertyCurrent2));

        JSONAssert.assertEquals(expected, getProperty(queryObj).readEntity(String.class), false);
    }

}
