package com.axibase.tsd.api.method.property.command;

import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.PropertyCommand;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.method.property.PropertyTest.assertPropertyExisting;
import static com.axibase.tsd.api.util.TestUtil.getCurrentDate;

public class EqualCharEscapeTest extends PropertyMethod {
    private final static Map DEFAULT_PROPERTY_TAGS;
    private final Transport transport;

    static {
        DEFAULT_PROPERTY_TAGS = new HashMap();
        DEFAULT_PROPERTY_TAGS.put("t1", "tv1");
    }
    
    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public EqualCharEscapeTest(Transport transport) {
        this.transport = transport;
    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testEntity() throws Exception {
        Property property = new Property(Mocks.propertyType(), Mocks.entity().replaceAll("-", "=-"));
        property.setTags(DEFAULT_PROPERTY_TAGS);
        property.setDate(getCurrentDate());
        PlainCommand command = new PropertyCommand(property);
        transport.send(command);
        assertPropertyExisting("Inserted property can not be received", property);

    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testType() throws Exception {
        Property property = new Property(Mocks.propertyType().replaceAll("-", "=-"), Mocks.entity());
        property.setTags(DEFAULT_PROPERTY_TAGS);
        property.setDate(getCurrentDate());
        PlainCommand command = new PropertyCommand(property);
        transport.send(command);
        property.setType(property.getType().replace("\"", ""));
        assertPropertyExisting("Inserted property can not be received", property);

    }
}
