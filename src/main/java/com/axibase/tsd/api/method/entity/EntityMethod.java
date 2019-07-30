package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class EntityMethod extends BaseMethod {
    private static final String METHOD_ENTITY = "/entities/{entity}";
    private static final String METHOD_ENTITY_METRICS = "/entities/{entity}/metrics";
    private static final String METHOD_ENTITY_GROUPS = "/entities/{entity}/groups";
    private static final String METHOD_ENTITY_PROPERTY_TYPES = "/entities/{entity}/property-types";

    public static <T> Response createOrReplaceEntity(String entityName, T query, String token) {
        Response response;
        if (token != null) {
            response = executeTokenRootRequest(webTarget -> webTarget.path(Util.API_PATH + METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
                    .put(json(query)));
        } else {
            response = executeApiRequest(webTarget -> webTarget
                    .path(METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .put(json(query)));
        }
        response.bufferEntity();
        return response;
    }

    public static Response createOrReplaceEntity(Entity entity, String token) {
        return createOrReplaceEntity(entity.getName(), entity, token);
    }

    public static <T> Response createOrReplaceEntity(String entityName, T query) {
        return createOrReplaceEntity(entityName, query, null);
    }

    public static Response createOrReplaceEntity(Entity entity) {
        return createOrReplaceEntity(entity.getName(), entity);
    }

    public static void createOrReplaceEntityCheck(Entity entity) throws Exception {
        createOrReplaceEntityCheck(entity, new EntityCheck(entity));
    }

    public static void createOrReplaceEntityCheck(Entity entity, AbstractCheck check) throws Exception {
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(createOrReplaceEntity(entity.getName(), jacksonMapper.writeValueAsString(entity)))) {
            throw new IllegalStateException("Can not execute createOrReplaceEntity query");
        }
        Checker.check(check);
    }

    public static boolean entityExist(final Entity entity) throws Exception {
        Response response = getEntityResponse(entity.getName());
        if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new Exception("Fail to execute entity query: " + responseAsString(response));
        }
        return compareJsonString(jacksonMapper.writeValueAsString(entity), response.readEntity(String.class));
    }

    public static boolean entityExist(String entity) throws NotCheckedException {
        final Response response = EntityMethod.getEntityResponse(entity);
        if (Response.Status.Family.SUCCESSFUL == Util.responseFamily(response)) {
            return true;
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (entity.contains(" ")) {
            return entityExist(entity.replace(" ", "_"));
        }

        throw new NotCheckedException("Fail to execute entity query: " + responseAsString(response));
    }

    public static Response getEntityResponse(String entityName, String token) {
        Response response;
        if (token != null) {
            response = executeTokenRootRequest(webTarget -> webTarget.path(Util.API_PATH + METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
                    .get());
        } else {
            response = executeApiRequest(webTarget -> webTarget
                    .path(METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .get());
        }
        response.bufferEntity();
        return response;
    }

    public static Response getEntityResponse(String entityName) {
        return getEntityResponse(entityName, null);
    }

    public static Entity getEntity(String entityName) {
        Response response = getEntityResponse(entityName);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
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

    public static <T> Response updateEntity(String entityName, T query, String token) {
        Response response;
        if (token != null) {
            response = executeTokenRootRequest(webTarget -> webTarget
                    .path(Util.API_PATH + METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
                    .method("PATCH", json(query)));
        } else {
            response = executeApiRequest(webTarget -> webTarget
                    .path(METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .method("PATCH", json(query)));
        }
        response.bufferEntity();
        return response;
    }

    public static Response updateEntity(Entity entity, String token) {
        return updateEntity(entity.getName(), entity, token);
    }

    public static <T> Response updateEntity(String entityName, T query) {
        return updateEntity(entityName, query, null);
    }

    public static Response updateEntity(Entity entity) {
        return updateEntity(entity.getName(), entity);
    }

    public static Response deleteEntity(String entityName, String token) {
        Response response;
        if (token != null) {
            response = executeTokenRootRequest(webTarget -> webTarget
                    .path(Util.API_PATH + METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
                    .delete());
        } else {
            response = executeApiRequest(webTarget -> webTarget
                    .path(METHOD_ENTITY)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .delete());
        }
        response.bufferEntity();
        return response;
    }

    public static Response deleteEntity(String entityName) {
        return deleteEntity(entityName, null);
    }

    public static Response queryEntityMetrics(String entityName, Map<String, String> parameters, String token) {
        Response response;
        if (token != null) {
            response = executeTokenRootRequest(webTarget -> {
                WebTarget target = webTarget.path(Util.API_PATH + METHOD_ENTITY_METRICS).resolveTemplate("entity", entityName);
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    target = target.queryParam(entry.getKey(), entry.getValue());
                }
                return target.request().header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token)).get();
            });
        } else {
            response = executeApiRequest(webTarget -> {
                WebTarget target = webTarget.path(METHOD_ENTITY_METRICS).resolveTemplate("entity", entityName);
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    target = target.queryParam(entry.getKey(), entry.getValue());
                }
                return target.request().get();
            });
        }
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityMetrics(String entityName, String token) {
        return queryEntityMetrics(entityName, new HashMap<>(), token);
    }

    private static Response queryEntityMetrics(String entityName, Map<String, String> parameters) {
        return queryEntityMetrics(entityName, parameters, null);
    }

    public static Response queryEntityMetrics(String entityName) {
        return queryEntityMetrics(entityName, new HashMap<>());
    }

    public static Response queryEntityGroups(String entityName, String token) {
        Response response;
        if (token != null) {
            response = executeTokenRootRequest(webTarget -> webTarget
                    .path(Util.API_PATH + METHOD_ENTITY_GROUPS)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
                    .get());
        } else {
            response = executeApiRequest(webTarget -> webTarget
                    .path(METHOD_ENTITY_GROUPS)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .get());
        }
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityGroups(String entityName) {
        return queryEntityGroups(entityName, null);
    }

    public static Response queryEntityPropertyTypes(String entityName, String token) {
        Response response;
        System.err.println(Util.API_PATH + METHOD_ENTITY_PROPERTY_TYPES);
        if (token != null) {
            response = executeTokenRootRequest(webTarget -> webTarget
                    .path(Util.API_PATH + METHOD_ENTITY_PROPERTY_TYPES)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
                    .get());
        } else {
            response = executeApiRequest(webTarget -> webTarget
                    .path(METHOD_ENTITY_PROPERTY_TYPES)
                    .resolveTemplate("entity", entityName)
                    .request()
                    .get());
        }
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityPropertyTypes(String entityName) {
        return queryEntityPropertyTypes(entityName, null);
    }
}
