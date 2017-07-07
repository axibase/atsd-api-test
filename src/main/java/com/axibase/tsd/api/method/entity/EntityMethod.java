package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.NotCheckedException;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public class EntityMethod extends BaseMethod {
    private static final String METHOD_ENTITY = "/entities/{entity}";
    private static final String METHOD_ENTITY_METRICS = "/entities/{entity}/metrics";
    private static final String METHOD_ENTITY_GROUPS = "/entities/{entity}/groups";
    private static final String METHOD_ENTITY_PROPERTY_TYPES = "/entities/{entity}/property-types";

    public static <T> Response createOrReplaceEntity(String entityName, T query) throws Exception {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().put(json(query));
        response.bufferEntity();
        return response;
    }

    public static Response createOrReplaceEntity(Entity entity) throws Exception {
        return createOrReplaceEntity(entity.getName(), entity);
    }

    public static void createOrReplaceEntityCheck(Entity entity) throws Exception {
        createOrReplaceEntityCheck(entity, new EntityCheck(entity));
    }

    public static void createOrReplaceEntityCheck(Entity entity, AbstractCheck check) throws Exception {
        if (createOrReplaceEntity(entity.getName(), jacksonMapper.writeValueAsString(entity)).getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Can not execute createOrReplaceEntity query");
        }
        Checker.check(check);
    }

    public static boolean entityExist(final Entity entity) throws Exception {
        Response response = getEntityResponse(entity.getName());
        if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (response.getStatus() != OK.getStatusCode()) {
            throw new Exception("Fail to execute queryMetric query");
        }
        return compareJsonString(jacksonMapper.writeValueAsString(entity), response.readEntity(String.class));
    }

    public static boolean entityExist(String entity) throws NotCheckedException {
        final Response response = EntityMethod.getEntityResponse(entity);
        if (response.getStatus() == OK.getStatusCode()) {
            return true;
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (entity.contains(" ")){
            return entityExist(entity.replace(" ", "_"));
        }

        throw new NotCheckedException("Fail to execute entity query");
    }

    public static Response getEntityResponse(String entityName) {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().get();
        response.bufferEntity();
        return response;
    }

    public static Entity getEntity(String entityName) {
        Response response = getEntityResponse(entityName);
        if (response.getStatus() != OK.getStatusCode()) {
            String error;
            try {
                error = extractErrorMessage(response);
            } catch (Exception e) {
                error = response.readEntity(String.class);
            }
            throw new IllegalStateException(String.format("Failed to get entity! Reason: %s", error));
        }
        return response.readEntity(Entity.class);
    }


    public static <T> Response updateEntity(String entityName, T query) throws Exception {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().method("PATCH", json(query));
        response.bufferEntity();
        return response;
    }

    public static Response updateEntity(Entity entity) throws Exception {
        return updateEntity(entity.getName(), entity);
    }

    public static Response deleteEntity(String entityName) throws Exception {
        Response response = httpApiResource.path(METHOD_ENTITY).resolveTemplate("entity", entityName).request().delete();
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityMetrics(String entityName) {
        return queryEntityMetrics(entityName, new HashMap<>());
    }

    private static Response queryEntityMetrics(String entityName, Map<String, String> parameters) {
        WebTarget target = httpApiResource.path(METHOD_ENTITY_METRICS).resolveTemplate("entity", entityName);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        Response response = target.request().get();
        response.bufferEntity();
        return response;
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
}
