package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.metric.Metric;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Entity;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Korchagin.
 */
public class EntityMethod extends BaseMethod {
    static final String METHOD_ENTITY_LIST = "/entities/";
    static final String METHOD_ENTITY= "/entities/{entity}";
    static final String METHOD_ENTITY_METRICS= "/entities/{entity}/metrics";
    static final String METHOD_ENTITY_GROUPS = "/entities/{entity}/groups";
    static final String METHOD_ENTITY_PROPERTY_TYPES = "/entities/{entity}/property-types";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static <T> Response createOrReplaceEntity(String entityName, T query) throws Exception {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().put(Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static Response createOrReplaceEntity(com.axibase.tsd.api.model.entity.Entity entity) throws Exception {
        return createOrReplaceEntity(entity.getName(), entity);
    }

    public static Response getEntity(String entityName) {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().get();
        response.bufferEntity();
        return response;
    }


    public static void createOrReplaceEntityCheck(com.axibase.tsd.api.model.entity.Entity entity) throws Exception {
        if (createOrReplaceEntity(entity.getName(), jacksonMapper.writeValueAsString(entity)).getStatus() != OK.getStatusCode()) {
            throw new IOException("Can not execute createOrReplace query");
        }
        if (!entityExist(entity)) {
            throw new IOException("Fail to check metric createOrReplace");
        }
    }


    public static boolean entityExist(final com.axibase.tsd.api.model.entity.Entity entity) throws IOException {
        return entityExist(entity, false);
    }

    public static boolean entityExist(final com.axibase.tsd.api.model.entity.Entity entity, boolean strict) throws IOException {
        Response response = getEntity(entity.getName());
        if(response.getStatus() != OK.getStatusCode()) {
            throw new IOException("Fail to execute getEntity query");
        }
        String responseJson = response.readEntity(String.class);
        String expected = jacksonMapper.writeValueAsString(entity);
        logger.debug("check: {}\nresponse: {}", expected, responseJson);

        return compareJsonString(expected, responseJson, strict);
    }
}
