package com.axibase.tsd.api.method.property.command;

import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.property.Property;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertTrue;

public class DoubleBackslashCharEscapeTest extends PropertyMethod {
    private final static Map DEFAULT_PROPERTY_TAGS;

    static {
        DEFAULT_PROPERTY_TAGS = new HashMap();
        DEFAULT_PROPERTY_TAGS.put("t1", "tv1");
    }

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Property property = new Property("property-command-test-t8", "property-command-test\\\\-e8");
        property.setTags(DEFAULT_PROPERTY_TAGS);
        property.setDate(Util.getCurrentDate());

        String command = buildPropertyCommandFromProperty(property);
        tcpSender.send(command, DEFAULT_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted property can not be received", PropertyMethod.propertyExist(property));
    }

    /**
     * #2854
     */
    @Test
    public void testType() throws Exception {
        Property property = new Property("property-command-test\\\\-t7", "property-command-test-e7");
        property.setTags(DEFAULT_PROPERTY_TAGS);
        property.setDate(Util.getCurrentDate());

        String command = buildPropertyCommandFromProperty(property);
        tcpSender.send(command, DEFAULT_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted property can not be received", PropertyMethod.propertyExist(property));
    }
}
