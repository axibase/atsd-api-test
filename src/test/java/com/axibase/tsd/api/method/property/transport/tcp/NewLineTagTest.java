package com.axibase.tsd.api.method.property.transport.tcp;

import com.axibase.tsd.api.method.property.PropertyTest;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.PropertyCommand;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class NewLineTagTest extends PropertyTest {

    @DataProvider
    private Object[][] newLines() {
        return new String[][] {
                {"\n"},
                {" \n"},
                {"\n "},
                {"\r\n"},
                {"\n\r"}
        };
    }

    @Issue("6327")
    @Test(
            dataProvider = "newLines",
            description = "Check that tag that comes after tag value with line separator is not thrown away"
    )
    public void testTagsAfterNewLine(String newLine) throws Exception {
        String propertyType = Mocks.propertyType();
        String entityName = Mocks.entity();
        Property property = new Property(propertyType, entityName);
        property.setTags(ImmutableMap.of("t1", newLine, "t2", "v2"));
        PlainCommand command = new PropertyCommand(property);
        String response = TCPSender.send(command, true);
        assertEquals("Property command with newline tag did not returned ok", "ok", response);
        property.setTags(ImmutableMap.of("t2", "v2"));
        assertPropertyExisting(property);
    }

}
