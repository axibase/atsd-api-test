package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.BaseMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * @author Dmitry Korchagin.
 */
public class EntityMethod extends BaseMethod {
    public static final String METHOD_ENTITY_LIST = "/entities/";
    public static final String METHOD_ENTITY = "/entities/{entity}";
    public static final String METHOD_ENTITY_METRICS = "/entities/{entity}/metrics";
    public static final String METHOD_ENTITY_GROUPS = "/entities/{entity}/groups";
    public static final String METHOD_ENTITY_PROPERTY_TYPES = "/entities/{entity}/property-types";
    public static final String UNKNOWN_ENTITY_FIELD_ERROR_PREFIX = "org.codehaus.jackson.map.exc.UnrecognizedPropertyException:";
    public static final String TAG_VALUE_ARRAY_ERROR_PREFIX = "org.codehaus.jackson.map.JsonMappingException: Can not deserialize instance";


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

    public static <T> Response updateEntity(String entityName, T query) throws Exception {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().method("PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static Response updateEntity(com.axibase.tsd.api.model.entity.Entity entity) throws Exception {
        return updateEntity(entity.getName(), entity);
    }

    public static Response deleteEntity(String entityName) throws Exception {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().delete();
        response.bufferEntity();
        return response;
    }


    public static Response queryEntityMetrics(String entityName, Map<String, String> parameters) {
        WebTarget target = httpApiResource.path(METHOD_ENTITY_METRICS).resolveTemplate("entity", entityName);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        Response response = target.request().get();
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityMetrics(String entityName) {
        return queryEntityMetrics(entityName, new HashMap<String, String>());
    }

    public static Response queryEntityGroups(String entityName) {
        Response response = httpApiResource.path(METHOD_ENTITY_GROUPS).resolveTemplate("entity", entityName).request().get();
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityPropertyTypes(String entityName) {
        Response response = httpApiResource.path(METHOD_ENTITY_PROPERTY_TYPES).resolveTemplate("entity", entityName).request().get();
        response.bufferEntity();
        return response;
    }


    public static void createOrReplaceEntityCheck(com.axibase.tsd.api.model.entity.Entity entity) throws Exception {
        if (createOrReplaceEntity(entity.getName(), jacksonMapper.writeValueAsString(entity)).getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Can not execute createOrReplaceEntity query");
        }
        if (!entityExist(entity)) {
            throw new IllegalStateException("Fail to check entity " + entity.getName());
        }
    }


    public static boolean entityExist(final com.axibase.tsd.api.model.entity.Entity entity) throws Exception {
        return entityExist(entity, false);
    }

    public static boolean entityExist(final com.axibase.tsd.api.model.entity.Entity entity, boolean strict) throws Exception {
        Response response = getEntity(entity.getName());
        if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (response.getStatus() != OK.getStatusCode()) {
            throw new Exception("Fail to execute queryMetric query");
        }
        return compareJsonString(jacksonMapper.writeValueAsString(entity), response.readEntity(String.class), strict);
    }
}
