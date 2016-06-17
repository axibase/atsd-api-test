package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.model.property.Property;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PropertyCommandTest extends PropertyMethod {

    /* #2412 */
    @Test
    public void testMaxLength() throws ParseException, IOException, InterruptedException, JSONException {
        int maxLength = 128 * 1024;

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

        for (int i = 0; sb.length() < maxLength; i++) {
            String tagName = "name" + i;
            String textValue = "sda" + i;
            sb.append(" v:").append(tagName).append("=").append(textValue);
            property.addTag(tagName, textValue);
        }

        Assert.assertEquals("Command length is not maximal", maxLength, sb.length());
        tcpSender.send(sb.toString());

        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("type", property.getType());
        queryObj.put("entity", property.getEntity());
        queryObj.put("startDate", property.getDate());
        queryObj.put("endDate", endDate);

        PropertyMethod propertyMethod = new PropertyMethod();
        Thread.sleep(1000);

        String sentProperty = jacksonMapper.writeValueAsString(new ArrayList<Property>() {{
            add(property);
        }});
        String storedProperty = propertyMethod.queryProperty(queryObj);

        JSONAssert.assertEquals(sentProperty, storedProperty, JSONCompareMode.NON_EXTENSIBLE);
    }
}
