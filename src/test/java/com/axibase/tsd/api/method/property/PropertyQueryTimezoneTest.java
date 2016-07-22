package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.model.DateFilter;
import com.axibase.tsd.api.model.EntityFilter;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;


import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.assertEquals;

public class PropertyQueryTimezoneTest extends PropertyMethod {
    private static final Property property;

    static {
        property = new Property("property-query-test-isoz", "test-query1");
        property.addTag("test", "test");
        property.setDate("2016-05-21T00:00:00.000Z");
    }

    @BeforeMethod
    public void prepare() throws Exception {
        insertPropertyCheck(property);
    }

    /* #2850 */
    @Test
    public void testISOTimezoneZ() throws Exception {

        PropertyQuery propertyQuery = buildPropertyQuery();

        propertyQuery.getDateFilter().setStartDate("2016-05-21T00:00:00Z");

        List<Property> storedPropertyList = getProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        assertEquals("Incorrect property entity", property.getEntity(), storedProperty.getEntity());
        assertEquals("Incorrect property tags", property.getTags(), storedProperty.getTags());
        assertEquals("Incorrect property date", property.getDate(), storedProperty.getDate());
    }

    /* #2850 */
    @Test
    public void testISOTimezonePlusHourMinute() throws Exception {

        PropertyQuery propertyQuery = buildPropertyQuery();

        propertyQuery.getDateFilter().setStartDate("2016-05-21T01:23:00+01:23");

        List<Property> storedPropertyList = getProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        assertEquals("Incorrect property entity", property.getEntity(), storedProperty.getEntity());
        assertEquals("Incorrect property tags", property.getTags(), storedProperty.getTags());
        assertEquals("Incorrect property date", property.getDate(), storedProperty.getDate());
    }

    /* #2850 */
    @Test
    public void testISOTimezoneMinusHourMinute() throws Exception {
        PropertyQuery propertyQuery = buildPropertyQuery();

        propertyQuery.getDateFilter().setStartDate("2016-05-20T22:37:00-01:23");

        List<Property> storedPropertyList = getProperty(propertyQuery).readEntity(new GenericType<List<Property>>() {
        });
        Property storedProperty = storedPropertyList.get(0);

        assertEquals("Incorrect property entity", property.getEntity(), storedProperty.getEntity());
        assertEquals("Incorrect property tags", property.getTags(), storedProperty.getTags());
        assertEquals("Incorrect property date", property.getDate(), storedProperty.getDate());
    }

    /* #2850 */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        PropertyQuery propertyQuery = buildPropertyQuery();

        propertyQuery.getDateFilter().setStartDate("2016-07-21 00:00:00");

        Response response = getProperty(propertyQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-21 00:00:00\"}", response.readEntity(String.class), true);

    }

    /* #2850 */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        PropertyQuery propertyQuery = buildPropertyQuery();

        propertyQuery.getDateFilter().setStartDate("2016-07-20T22:50:00-0110");

        Response response = getProperty(propertyQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-20T22:50:00-0110\"}", response.readEntity(String.class), true);
    }

    /* #2850 */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        PropertyQuery propertyQuery = buildPropertyQuery();

        propertyQuery.getDateFilter().setStartDate("1469059200000");

        Response response = getProperty(propertyQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 1469059200000\"}", response.readEntity(String.class), true);
    }


    private PropertyQuery buildPropertyQuery() {
        PropertyQuery propertyQuery = new PropertyQuery();
        propertyQuery.setType(property.getType());

        EntityFilter entityFilter = new EntityFilter();
        entityFilter.setEntity(property.getEntity());

        DateFilter dateFilter = new DateFilter();
        dateFilter.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        propertyQuery.setEntityFilter(entityFilter);
        propertyQuery.setDateFilter(dateFilter);

        return propertyQuery;
    }

}
