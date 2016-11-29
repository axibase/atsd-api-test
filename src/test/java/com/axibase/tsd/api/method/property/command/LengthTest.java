package com.axibase.tsd.api.method.property.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.PropertyCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.method.property.PropertyTest.assertPropertyExisting;
import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.propertyType;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class LengthTest extends PropertyMethod {
    private static final int MAX_LENGTH = 128 * 1024;


    /**
     * #2412
     */
    @Test
    public void testMaxLength() throws Exception {
        final Property property = new Property(propertyType(), entity());
        property.setDate(Mocks.ISO_TIME);
        property.setKey(Collections.EMPTY_MAP);
        property.addTag("type", property.getType());
        PlainCommand command = new PropertyCommand(property);
        Integer currentLength = command.compose().length();
        for (int i = 0; currentLength < MAX_LENGTH; i++) {
            String tagName = "name" + i;
            String textValue = "sda" + i;
            String addedTag = String.format(" v:%s=%s", tagName, textValue);
            currentLength += addedTag.length();
            if (currentLength <= MAX_LENGTH) {
                property.addTag(tagName, textValue);
            } else {
                currentLength -= addedTag.length();
                break;
            }
        }
        while (currentLength != MAX_LENGTH) {
            property.setType(property.getType().concat("+"));
            currentLength++;
        }
        command = new PropertyCommand(property);
        assertEquals("Command length is not maximal", MAX_LENGTH, command.compose().length());
        CommandMethod.send(command);
        assertPropertyExisting("Inserted property can not be received", property);
    }

    /**
     * #2412
     */
    @Test
    public void testMaxLengthOverflow() throws Exception {
        final Property property = new Property(propertyType(), entity());
        property.setDate(Mocks.ISO_TIME);
        property.setKey(Collections.EMPTY_MAP);
        property.addTag("type", property.getType());
        PlainCommand command = new PropertyCommand(property);
        Integer currentLength = command.compose().length();
        for (int i = 0; currentLength < MAX_LENGTH + 1; i++) {
            String tagName = "name" + i;
            String textValue = "sda" + i;
            currentLength += String.format(" v:%s=%s", tagName, textValue).length();
            property.addTag(tagName, textValue);
        }
        command = new PropertyCommand(property);
        CommandSendingResult actualResult = CommandMethod.send(command);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        assertTrue("Command length is not greater than max", MAX_LENGTH < currentLength);
        assertEquals("Managed to insert command that length is overflow max", expectedResult, actualResult);
    }


}
