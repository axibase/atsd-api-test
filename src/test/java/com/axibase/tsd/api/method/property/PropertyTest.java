package com.axibase.tsd.api.method.property;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.PropertyCheck;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.util.NotCheckedException;

import static org.testng.AssertJUnit.fail;

public class PropertyTest extends PropertyMethod {
    public static void assertPropertyExisting(String assertProperty, Property property) {
        try {
            Checker.check(new PropertyCheck(property));
        } catch (NotCheckedException e) {
            fail(assertProperty);
        }
    }

    public static void assertPropertyExisting(Property property) {
        String assertMessage = String.format(
                com.axibase.tsd.api.method.property.PropertyTest.DefaultPropertysTemplates.PROPERTY_NOT_EXIST,
                property
        );
        assertPropertyExisting(assertMessage, property);
    }

    private static final class DefaultPropertysTemplates {
        private static final String PROPERTY_NOT_EXIST = "Property: %s%n doesn't exist!";
    }
}
