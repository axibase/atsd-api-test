package com.axibase.tsd.api.method.property;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.method.BaseMethod;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Korchagin.
 */
@SuppressWarnings("unchecked")
class PropertyMethod extends BaseMethod {
    static final String METHOD_PROPERTY_INSERT = "/properties/insert";
    static final String METHOD_PROPERTY_QUERY = "/properties/query";
    static final String METHOD_PROPERTY_DELETE = "/properties/delete";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    void insertPropertyCheck(final Property property) throws IOException {
        JSONArray request = new JSONArray() {{
            add(new JSONObject() {{
                put("entity", property.getEntity());
                put("type", property.getType());
                if (property.getKey() != null) {
                    put("key", property.getKey());
                }
                put("tags", property.getTags());
                if (property.getDate() != null) {
                    put("date", property.getDate());
                }
            }});
        }};
        Response response = httpApiResource.path(METHOD_PROPERTY_INSERT)
                .request().post(Entity.entity(request.toJSONString(), MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() != 200) {
            throw new IOException("Fail to insert property");
        }
        if (!propertyExist(property)) {
            throw new IOException("Fail to check inserted property");
        }
    }


    Boolean propertyExist(final Property property) throws IOException {
        return propertyExist(property, true);
    }

    Boolean propertyExist(final Property property, Boolean allowExtraFields) throws IOException {

        JSONArray request = new JSONArray() {{
            add(new JSONObject() {{
                put("entity", property.getEntity());
                put("type", property.getType());
                put("key", property.getKey());
                if (null == property.getDate()) {
                    put("startDate", Util.getMinDate());
                    put("endDate", Util.getMaxDate());
                } else {
                    put("startDate", property.getDate());
                    put("interval", new JSONObject() {{
                        put("unit", "MILLISECOND");
                        put("count", "1");
                    }});
                }
                put("exactMatch", true);
            }});
        }};

        String propertyJson = jacksonMapper.writeValueAsString(Arrays.asList(property));

        Response response = httpApiResource.path(METHOD_PROPERTY_QUERY).request().post(Entity.json(request));
        if (response.getStatus() != 200) {
            throw new IOException("Fail to execute property query");
        }
        String responseJson = response.readEntity(String.class);
        logger.debug("check: {}\nresponse: {}", propertyJson, responseJson);

        try {
            JSONAssert.assertEquals(propertyJson, responseJson, allowExtraFields ? JSONCompareMode.LENIENT : JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException e) {
            throw new IOException("Can not deserialize response");
        } catch (AssertionError e) {
            return false;
        }
        return true;
    }

    String queryProperty(final Map request) throws IOException {

        JSONArray query = new JSONArray() {{
            add(new JSONObject(request));
        }};
        Response response = httpApiResource.path(METHOD_PROPERTY_QUERY).request().post(Entity.entity(query.toJSONString(), MediaType.APPLICATION_JSON_TYPE));
        assertEquals(200, response.getStatus());

        return response.readEntity(String.class);
    }

    private JSONArray buildJsonArray(final Property... properties) {
        JSONArray jsonArray = new JSONArray();
        for (final Property property : properties) {
            jsonArray.add(buildJsonObject(property));
        }
        return jsonArray;
    }

    private JSONObject buildJsonObject(final Property property) {
        return new JSONObject() {{
            put("entity", property.getEntity());
            put("type", property.getType());
            if (property.getKey() != null) {
                put("key", property.getKey());
            }
            put("tags", property.getTags());
            if (property.getDate() != null) {
                put("date", property.getDate());
            }
        }};
    }

    protected void deleteProperties(final Property... properties) throws IOException {
        JSONArray jsonArray = new JSONArray();

        for (final Property property : properties) {
            jsonArray.add(new JSONObject() {{
                put("type", property.getType());
                put("entity", property.getEntity());
                put("key", property.getKey());
                put("exactMatch", true);

            }});
        }
        Response response = httpApiResource.path(METHOD_PROPERTY_DELETE).request().post(Entity.entity(jsonArray.toJSONString(), MediaType.APPLICATION_JSON_TYPE));
        assertEquals("Fail to delete properties", 200, response.getStatus());
    }

    protected Response deleteProperty(final Map deleteObj) throws IOException {
        JSONArray jsonArray = new JSONArray() {{
            add(new JSONObject(deleteObj));
        }};
        return httpApiResource.path(METHOD_PROPERTY_DELETE).request().post(Entity.entity(jsonArray.toJSONString(), MediaType.APPLICATION_JSON_TYPE));
    }

    protected int calculateJsonArraySize(String jsonArrayString) throws JSONException {
        return new org.json.JSONArray(jsonArrayString).length();
    }
}
