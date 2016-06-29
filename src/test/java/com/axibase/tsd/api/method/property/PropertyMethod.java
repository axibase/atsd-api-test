package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.property.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

import static javax.ws.rs.core.Response.Status.OK;

/**
 * @author Dmitry Korchagin.
 */
class PropertyMethod extends BaseMethod {
    private static final String METHOD_PROPERTY_INSERT = "/properties/insert";
    private static final String METHOD_PROPERTY_QUERY = "/properties/query";
    private static final String METHOD_PROPERTY_DELETE = "/properties/delete";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static <T> Response insertProperty(List<T> queryList) {
        return httpApiResource
                .path(METHOD_PROPERTY_INSERT)
                .request()
                .post(Entity.entity(queryList, MediaType.APPLICATION_JSON_TYPE));
    }

    public static <T> Response insertProperty(T... queries) {
        return insertProperty(Arrays.asList(queries));
    }

    public static <T> Response getProperty(List<T> queryList) {
        return httpApiResource
                .path(METHOD_PROPERTY_QUERY)
                .request()
                .post(Entity.entity(queryList, MediaType.APPLICATION_JSON_TYPE));
    }

    public static <T> Response getProperty(T... queries) {
        return getProperty(Arrays.asList(queries));
    }

    public static <T> Response deleteProperty(List<T> queryList) {
        return httpApiResource
                .path(METHOD_PROPERTY_DELETE)
                .request()
                .post(Entity.entity(queryList, MediaType.APPLICATION_JSON_TYPE));
    }

    public static <T> Response deleteProperty(T... queries) {
        return deleteProperty(Arrays.asList(queries));
    }


    public static void insertPropertyCheck(final Property property) throws IOException {
        if (insertProperty(Collections.singletonList(property)).getStatus() != OK.getStatusCode()) {
            throw new IOException("Can not execute insert property query");
        }

        if (!propertyExist(property)) {
            throw new IOException("Fail to check inserted property");
        }
    }

    public static boolean propertyExist(final Property property) throws IOException {
        return propertyExist(property, false);
    }

    public static boolean propertyExist(final Property property, boolean strict) throws IOException {
        Response response = getProperty(prepareStrictPropertyQuery(property));
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IOException("Fail to execute getProperty");
        }
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        String given = response.readEntity(String.class);
        logger.debug("check: {}\nresponse: {}", expected, given);
        return compareJsonString(expected, given, strict);
    }

    private static List prepareStrictPropertyQuery(final Property property) {
        Map<String, Object> query = new HashMap<>();
        query.put("entity", property.getEntity());
        query.put("type", property.getType());
        query.put("key", property.getKey());
        if (null == property.getDate()) {
            query.put("startDate", Util.getMinDate());
            query.put("endDate", Util.getMaxDate());
        } else {
            query.put("startDate", property.getDate());
            query.put("interval", new HashMap<String, Object>() {{
                put("unit", "MILLISECOND");
                put("count", "1");
            }});
        }
        query.put("exactMatch", true);

        return Collections.singletonList(query);
    }

}
