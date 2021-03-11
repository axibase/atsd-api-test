package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.Check;
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
        String assertMessage = String.format("Property: %s doesn't exist!", property);
        assertPropertyExisting(assertMessage, property);
    }

    public static void assertPropertyTypeExist(String propertyType) {
        String assertMessage = String.format("Fail to find property type %s in ATSD", propertyType);
        try {
            Check propertyCheck = new Check(assertMessage, () -> PropertyTest.propertyTypeExist(propertyType));
            Checker.check(propertyCheck);
        } catch (NotCheckedException e) {
            fail(assertMessage);
        }
    }
}
