package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.model.property.Property;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PropertyCommandTest extends PropertyMethod {
    /* #2412 */
    @Test
    public void testMaxLength() throws ParseException, IOException, InterruptedException, JSONException {
        final int MAX_LENGTH = 128 * 1024;

        String startDate = "2016-05-21T00:00:00.000Z";
        String endDate = "2016-05-21T00:00:01Z";

        final Property property = new Property("t-property-max-cmd-length", "e-property-max-cmd-len");
        property.setDate(startDate);
        property.setKey(new HashMap<String, String>());
        property.addTag("type", property.getType());

        StringBuilder sb = new StringBuilder("property");
        sb.append(" e:").append(property.getEntity());
        sb.append(" d:").append(property.getDate());
        sb.append(" t:").append(property.getType());
        sb.append(" v:").append("type=").append(property.getTags().get("type"));

        for (int i = 0; sb.length() < MAX_LENGTH; i++) {
            String tagName = "name" + i;
            String textValue = "sda" + i;
            sb.append(" v:").append(tagName).append("=").append(textValue);
            property.addTag(tagName, textValue);
        }

        Assert.assertEquals("Command length is not maximal", MAX_LENGTH, sb.length());
        tcpSender.send(sb.toString(), 1000);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", endDate);

        PropertyMethod propertyMethod = new PropertyMethod();
        Thread.sleep(1000);

        String sentProperty = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        String storedProperty = propertyMethod.queryProperty(queryObj);

        JSONAssert.assertEquals(sentProperty, storedProperty, JSONCompareMode.LENIENT);
    }

    /* #2412 */
    @Test
    public void testMaxLengthOverflow() throws ParseException, IOException, InterruptedException, JSONException {
        final int MAX_LENGTH = 128 * 1024;

        String startDate = "2016-05-21T00:00:00.000Z";
        String endDate = "2016-05-21T00:00:01Z";

        final Property property = new Property("t-property-max-cmd-lnngth", "e-property-max-cmd-over");
        property.setDate(startDate);
        property.setKey(new HashMap<String, String>());
        property.addTag("type", property.getType());

        StringBuilder sb = new StringBuilder("property");
        sb.append(" e:").append(property.getEntity());
        sb.append(" d:").append(property.getDate());
        sb.append(" t:").append(property.getType());
        sb.append(" v:").append("type=").append(property.getTags().get("type"));

        for (int i = 0; sb.length() < MAX_LENGTH + 1; i++) {
            String tagName = "name" + i;
            String textValue = "sda" + i;
            sb.append(" v:").append(tagName).append("=").append(textValue);
            property.addTag(tagName, textValue);
        }

        if (MAX_LENGTH + 1 != sb.length()) {
            Assert.fail("Command length is not maximal");
        }
        tcpSender.send(sb.toString(), 1000);

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", endDate);

        PropertyMethod propertyMethod = new PropertyMethod();
        Thread.sleep(1000);

        String storedProperty = propertyMethod.queryProperty(queryObj);
        Assert.assertEquals("Managed to insert command that length is max + 1", "[]", storedProperty);
    }
}
