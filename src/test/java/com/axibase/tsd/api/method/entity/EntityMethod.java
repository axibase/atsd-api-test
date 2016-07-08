package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.BaseMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * @author Dmitry Korchagin.
 */
public class EntityMethod extends BaseMethod {
    static final String METHOD_ENTITY_LIST = "/entities/";
    static final String METHOD_ENTITY = "/entities/{entity}";
    static final String METHOD_ENTITY_METRICS = "/entities/{entity}/metrics";
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
            target.queryParam(entry.getKey(), entry.getValue());
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
            throw new IOException("Can not execute createOrReplace query");
        }
        if (!entityExist(entity)) {
            throw new IOException("Fail to check entity createOrReplace");
        }
    }


    public static boolean entityExist(final com.axibase.tsd.api.model.entity.Entity entity) throws IOException {
        return entityExist(entity, false);
    }

    public static boolean entityExist(final com.axibase.tsd.api.model.entity.Entity entity, boolean strict) throws IOException {
        Response response = getEntity(entity.getName());
        if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IOException("Fail to execute queryMetric query");
        }
        return compareJsonString(jacksonMapper.writeValueAsString(entity), response.readEntity(String.class), strict);
    }
}
