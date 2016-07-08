package com.axibase.tsd.api.method.property;


import com.axibase.tsd.api.model.property.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmitry Korchagin.
 */

public class PropertyTypeQueryTest extends PropertyMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /* 1278 */
    @Test
    public void testEntityNameContainsWhilespace() {
        Property property = new Property("typequery-property-type-1", "typequery entityname-1");
        assertEquals(BAD_REQUEST.getStatusCode(), typeQueryProperty(property.getEntity()).getStatus());
    }

    /* 1278 */
    @Test
    public void testEntityNameContainsSlash() throws IOException {
        Property property = new Property("typequery-property-type-2", "typequery/entityname-2");
        assertUrlencodedPathHandledSuccessfullyOnTypeQuery(property);
    }

    /* 1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws IOException {
        Property property = new Property("typequery-property-type-3", "typequeryйёentityname-3");
        assertUrlencodedPathHandledSuccessfullyOnTypeQuery(property);

    }

    public void assertUrlencodedPathHandledSuccessfullyOnTypeQuery(final Property property) throws IOException {
        property.addTag("t1", "tv1");
        insertPropertyCheck(property);
        Response response = typeQueryProperty(property.getEntity());
        assertEquals(OK.getStatusCode(), response.getStatus());
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property.getType()));
        assertTrue(compareJsonString(expected, formatToJsonString(response)));
    }


}
