package com.axibase.tsd.api.method.property;


import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.property.Property;


import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */

public class PropertyQueryOffsetTest extends PropertyMethod {
    private final static String propertyType = "query-offset-type1";
    private final static Property propertyPast1 = buildProperty("query-offset-entity1", "2016-06-29T00:00:00.010Z", "k1", "kv1");
    private final static Property propertyMiddl = buildProperty("query-offset-entity1", "2016-06-29T00:00:00.015Z", "k2", "kv2");
    private final static Property propertyLast1 = buildProperty("query-offset-entity2", "2016-06-29T00:00:00.020Z", "k3", "kv3");
    private final static Property propertyLast2 = buildProperty("query-offset-entity3", "2016-06-29T00:00:00.020Z", "k4", "kv4");


    @BeforeClass
    public static void prepareProperty() throws IOException {
        insertPropertyCheck(propertyPast1);
        insertPropertyCheck(propertyMiddl);
        insertPropertyCheck(propertyLast1);
        insertPropertyCheck(propertyLast2);
    }

    private static Property buildProperty(String entityName, String date, String... key) {
        if (key.length % 2 != 0) {
            throw new IllegalArgumentException("Key should be specified as name=value pairs");
        }
        Property property = new Property();
        property.setType(propertyType);
        property.setEntity(entityName);
        for (int i = 0; i < key.length; i += 2) {
            property.addKey(key[i], key[i + 1]);
        }
        property.addTag("defaultname", "defaultval");
        property.setDate(date);

        return property;
    }

    private static Response executeOffsetQuery(int offset) {
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", propertyType);
        queryObj.put("entity", "*");
        queryObj.put("startDate", Util.MIN_STORABLE_DATE);
        queryObj.put("endDate", Util.MAX_QUERYABLE_DATE);
        queryObj.put("offset", offset);
        return getProperty(queryObj);
    }

    //#2947
    /*
    Last offset = 0, = 0 therefore Last include
    Middle offset = 5, > 0 therefore Past do not include
    Past offset = 10, > 0 therefore Past do not include
     */
    @Test
    public void testOffset0SelectsLast() throws Exception {
        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyLast1, propertyLast2));

        JSONAssert.assertEquals(expected, executeOffsetQuery(0).readEntity(String.class), false);
    }

    //#2947
    /*
    offset < 0 therefore include ALL.
    */
    @Test
    public void testOffsetNegativeSelectAll() throws Exception {
        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyPast1, propertyMiddl, propertyLast1, propertyLast2));

        JSONAssert.assertEquals(expected, executeOffsetQuery(-5).readEntity(String.class), false);
    }

    //#2947
     /*
    Middle offset = 5, Last offset = 0, <= 5 therefore Middle&Last include
    Past offset = 10, > 5 therefore Past do not include
     */
    @Test
    public void testOffsetEqualDateDiffSelectsDateInclusive() throws Exception {
        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyMiddl, propertyLast1, propertyLast2));

        JSONAssert.assertEquals(expected, executeOffsetQuery(5).readEntity(String.class), false);
    }

    //#2947
    /*
    Middle offset = 5, Last offset = 0, < 6 therefore Middle&Last include
    Past offset = 10, > 6 therefore Past do not include
     */
    @Test
    public void testOffsetMoreDateSelectsLessOrEqualDate() throws Exception {
        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyMiddl, propertyLast1, propertyLast2));

        JSONAssert.assertEquals(expected, executeOffsetQuery(6).readEntity(String.class), false);
    }

    //#2947
     /*
    Middle offset = 5, Last offset = 0, Past offset = 10 < 100 therefore Past&Middle&Last include
     */
    @Test
    public void testOffsetLargeSelectAll() throws Exception {
        String expected = jacksonMapper.writeValueAsString(Arrays.asList(propertyPast1, propertyMiddl, propertyLast1, propertyLast2));

        JSONAssert.assertEquals(expected, executeOffsetQuery(100).readEntity(String.class), false);
    }


}
