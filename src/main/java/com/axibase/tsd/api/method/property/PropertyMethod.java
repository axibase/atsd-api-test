package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.PropertyCheck;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.*;

import static com.axibase.tsd.api.util.Util.*;

public class PropertyMethod extends BaseMethod {
    private static final String METHOD_PROPERTY_INSERT = "/properties/insert";
    private static final String METHOD_PROPERTY_QUERY = "/properties/query";
    private static final String METHOD_PROPERTY_URL_QUERY = "/properties/{entity}/types/{type}";
    private static final String METHOD_PROPERTY_DELETE = "/properties/delete";
    private static final String METHOD_PROPERTY_TYPE_QUERY = "/properties/{entity}/types";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Map<String, Object> entityNameTemplate(String entityName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("entity", entityName);
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, Object> propertyAndEntityTypeTemplate(String entityName, String propertyType) {
        Map<String, Object> map = new LinkedHashMap<>(entityNameTemplate(entityName));
        map.put("type", propertyType);
        return Collections.unmodifiableMap(map);
    }

    public static <T> Response insertProperty(List<T> queries, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_PROPERTY_INSERT, HttpMethod.POST, Entity.json(queries));
        response.bufferEntity();
        return response;
    }

    public static <T> Response insertProperty(T... queries) {
        return insertProperty(Arrays.asList(queries), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response insertProperty(List<T> queries, String token) {
        return insertProperty(queries, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response queryProperty(List<T> queries, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_PROPERTY_QUERY, HttpMethod.POST, Entity.json(queries));
        response.bufferEntity();
        return response;
    }

    public static <T> Response queryProperty(T... queries) {
        return queryProperty(Arrays.asList(queries), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response queryProperty(List<T> queries, String token) {
        return queryProperty(queries, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response deleteProperty(List<T> queries, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_PROPERTY_DELETE, HttpMethod.POST, Entity.json(queries));
        response.bufferEntity();
        return response;
    }

    public static <T> Response deleteProperty(T... queries) {
        return deleteProperty(Arrays.asList(queries), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response deleteProperty(List<T> queries, String token) {
        return deleteProperty(queries, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response urlQueryProperty(String propertyType, String entityName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_PROPERTY_URL_QUERY, propertyAndEntityTypeTemplate(entityName, propertyType)
                , HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response urlQueryProperty(String propertyType, String entityName) {
        return urlQueryProperty(propertyType, entityName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response urlQueryProperty(String propertyType, String entityName, String token) {
        return urlQueryProperty(propertyType, entityName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response typeQueryProperty(String entityName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_PROPERTY_TYPE_QUERY, entityNameTemplate(entityName)
                , HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response typeQueryProperty(String entityName) {
        return typeQueryProperty(entityName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response typeQueryProperty(String entityName, String token) {
        return typeQueryProperty(entityName, new RequestSenderWithBearerAuthorization(token));
    }


    public static void insertPropertyCheck(final Property property, AbstractCheck check) throws Exception {
        Response response = insertProperty(property);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new Exception("Can not execute insert property query");
        }
        Checker.check(check);
    }

    public static void insertPropertyCheck(final Property property) throws Exception {
        insertPropertyCheck(property, new PropertyCheck(property));
    }

    public static boolean propertyExist(final Property property) throws Exception {
        return propertyExist(property, false);
    }

    public static boolean propertyExist(final Property property, boolean strict) throws Exception {
        Response response = queryProperty(prepareStrictPropertyQuery(property));
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            response.close();
            throw new Exception("Fail to execute queryProperty");
        }
        String expected = jacksonMapper.writeValueAsString(Collections.singletonList(property));
        String given = response.readEntity(String.class);
        logger.debug("check: {}\nresponse: {}", expected, given);
        return compareJsonString(expected, given, strict);
    }

    public static boolean propertyTypeExist(String propertyType) {
        final PropertyQuery q = new PropertyQuery();
        q.setEntity("*");
        q.setType(propertyType);
        q.setStartDate(MIN_STORABLE_DATE);
        q.setEndDate(MAX_STORABLE_DATE);

        q.setLimit(1);

        final Response response = queryProperty(q);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new NotCheckedException("Fail to execute property query");
        }

        String given = response.readEntity(String.class);
        return !"[]".equals(given);
    }

    private static Map prepareStrictPropertyQuery(final Property property) {
        Map<String, Object> query = new HashMap<>();
        query.put("entity", property.getEntity());
        query.put("type", property.getType());
        query.put("key", property.getKey());
        if (null == property.getDate()) {
            query.put("startDate", MIN_QUERYABLE_DATE);
            query.put("endDate", MAX_QUERYABLE_DATE);
        } else {
            query.put("startDate", property.getDate());
            query.put("interval", new HashMap<String, Object>() {{
                put("unit", "MILLISECOND");
                put("count", "1");
            }});
        }
        query.put("exactMatch", true);

        return query;
    }
}
