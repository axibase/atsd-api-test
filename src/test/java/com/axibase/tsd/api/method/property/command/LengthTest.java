package com.axibase.tsd.api.method.property.command;

import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.PropertyCommand;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.method.property.PropertyTest.assertPropertyExisting;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class LengthTest extends PropertyMethod {
    private final int maxLength;
    private final Transport transport;

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public LengthTest(Transport transport) {
        this.transport = transport;
        this.maxLength = transport.equals(Transport.HTTP) ? 128 * 1024 : 128 * 1024 - 6;
    }


    @Issue("2412")
    @Issue("6319")
    @Test
    public void testMaxLength() throws Exception {
        final Property property = new Property(Mocks.propertyType(), Mocks.entity());
        property.setDate(Mocks.ISO_TIME);
        property.setKey(Collections.emptyMap());
        property.addTag("type", property.getType());
        PlainCommand command = new PropertyCommand(property);
        int currentLength = command.compose().length();
        for (int i = 0; currentLength < maxLength; i++) {
            String tagName = "name" + property.getEntity() + i;
            String textValue = "sda" + property.getEntity() + i;
            String addedTag = String.format(" v:%s=%s", tagName, textValue);
            currentLength += addedTag.length();
            if (currentLength <= maxLength) {
                property.addTag(tagName, textValue);
            } else {
                currentLength -= addedTag.length();
                break;
            }
        }
        if (currentLength < maxLength) {
            property.setType(property.getType() + StringUtils.repeat("+", maxLength - currentLength));
        }
        command = new PropertyCommand(property);
        assertEquals("Command length is not maximal", maxLength, command.compose().length());
        transport.send(command);
        assertPropertyExisting("Inserted property can not be received", property);
    }

    @Issue("2412")
    @Issue("6319")
    @Test
    public void testMaxLengthOverflow() throws Exception {
        final Property property = new Property(Mocks.propertyType(), Mocks.entity());
        property.setDate(Mocks.ISO_TIME);
        property.setKey(Collections.emptyMap());
        property.addTag("type", property.getType());
        PlainCommand command = new PropertyCommand(property);
        int currentLength = command.compose().length();
        for (int i = 0; currentLength < maxLength + 1; i++) {
            String tagName = "name" + i;
            String textValue = "sda" + i;
            currentLength += String.format(" v:%s=%s", tagName, textValue).length();
            property.addTag(tagName, textValue);
        }
        command = new PropertyCommand(property);
        assertFalse("Managed to insert command that length is overflow max", transport.send(command));
    }


}
