package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class EntityMethod extends BaseMethod {
    private static final String METHOD_ENTITY = "/entities/{entity}";
    private static final String METHOD_ENTITY_METRICS = "/entities/{entity}/metrics";
    private static final String METHOD_ENTITY_GROUPS = "/entities/{entity}/groups";
    private static final String METHOD_ENTITY_PROPERTY_TYPES = "/entities/{entity}/property-types";
    private static final String METHOD_ENTITY_VERSIONS = "/entities/{entity}/versions";

    private static Map<String, Object> nameTemplate(String entityName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("entity", entityName);
        return Collections.unmodifiableMap(map);
    }

    public static <T> Response createOrReplaceEntity(String entityName, T query, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITY, nameTemplate(entityName), HttpMethod.PUT, json(query));
        response.bufferEntity();
        return response;
    }

    public static Response createOrReplaceEntity(Entity entity, String token) {
        return createOrReplaceEntity(entity.getName(), entity, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response createOrReplaceEntity(String entityName, T query) {
        return createOrReplaceEntity(entityName, query, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response createOrReplaceEntity(Entity entity) {
        return createOrReplaceEntity(entity.getName(), entity);
    }

    public static void createOrReplaceEntityCheck(String entityName) throws Exception {
        createOrReplaceEntityCheck(new Entity(entityName));
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
            final String responseBody = responseAsString(response);
            final String errorMessage = String.format("Unexpected response for '%s' entity query. Status: %s %n. " +
                            "Response body: %s",
                    entity.getName(),
                    response.getStatus(),
                    responseBody);
            throw new Exception(errorMessage);
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

    public static Response getEntityResponse(String entityName, Long version, RequestSenderWithAuthorization sender) {
        Map<String, Object> params = version == null ? Collections.emptyMap() : Collections.singletonMap("version", Util.ISOFormat(version));
        Response response = sender.executeApiRequest(METHOD_ENTITY, nameTemplate(entityName), params, Collections.emptyMap(), HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response getEntityResponse(String entityName, String token) {
        return getEntityResponse(entityName, null, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response getEntityResponse(String entityName) {
        return getEntityResponse(entityName, (Long) null);
    }

    public static Response getEntityResponse(String entityName, Long version) {
        return getEntityResponse(entityName, version, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Entity getEntity(String entityName) {
        return getEntity(entityName, null);
    }

    public static Entity getEntity(String entityName, Long version) {
        Response response = getEntityResponse(entityName, version);
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

    public static Response getEntityVersionsResponse(String entityName, Long minVersionTime, Long maxVersionTime, RequestSenderWithAuthorization sender) {
        Map<String, Object> params = new HashMap<>();
        if (minVersionTime != null) {
            params.put("minVersionDate", Util.ISOFormat(minVersionTime));
        }
        if (maxVersionTime != null) {
            params.put("maxVersionDate", Util.ISOFormat(maxVersionTime));
        }
        Response response = sender.executeApiRequest(METHOD_ENTITY_VERSIONS, nameTemplate(entityName), params, Collections.emptyMap(), HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response getEntityVersionsResponse(String entityName, Long minVersionTime, Long maxVersionTime) {
        return getEntityVersionsResponse(entityName, minVersionTime, maxVersionTime, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static List<Long> getEntityVersions(String entityName, Long minVersionTime, Long maxVersionTime) {
        Response response = getEntityVersionsResponse(entityName, minVersionTime, maxVersionTime);
        List<String> result = response.readEntity(new GenericType<List<String>>() {
        });
        return result.stream().map(Util::getUnixTime).collect(Collectors.toList());
    }

    public static <T> Response updateEntity(String entityName, T query, RequestSenderWithAuthorization sender) {
        Map<String, Object> contentTypeMap = new HashMap<>();
        contentTypeMap.put(HttpHeaders.CONTENT_TYPE, "application/json");
        Response response = sender.executeApiRequest(METHOD_ENTITY, nameTemplate(entityName), Collections.EMPTY_MAP, contentTypeMap, "PATCH", json(query));
        response.bufferEntity();
        return response;
    }

    public static Response updateEntity(Entity entity, String token) {
        return updateEntity(entity.getName(), entity, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response updateEntity(String entityName, T query) {
        return updateEntity(entityName, query, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response updateEntity(Entity entity) {
        return updateEntity(entity.getName(), entity);
    }

    public static Response deleteEntity(String entityName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITY, nameTemplate(entityName), HttpMethod.DELETE);
        response.bufferEntity();
        return response;
    }

    public static Response deleteEntity(String entityName, String token) {
        return deleteEntity(entityName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response deleteEntity(String entityName) {
        return deleteEntity(entityName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response queryEntityMetrics(String entityName, Map<String, String> parameters, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITY_METRICS, nameTemplate(entityName), Util.toStringObjectMap(parameters),
                Collections.EMPTY_MAP, HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityMetrics(String entityName, String token) {
        return queryEntityMetrics(entityName, new HashMap<>(), new RequestSenderWithBearerAuthorization(token));
    }

    private static Response queryEntityMetrics(String entityName, Map<String, String> parameters) {
        return queryEntityMetrics(entityName, parameters, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response queryEntityMetrics(String entityName) {
        return queryEntityMetrics(entityName, Collections.EMPTY_MAP);
    }

    public static Response queryEntityGroups(String entityName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITY_GROUPS, nameTemplate(entityName), HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityGroups(String entityName, String token) {
        return queryEntityGroups(entityName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response queryEntityGroups(String entityName) {
        return queryEntityGroups(entityName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response queryEntityPropertyTypes(String entityName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITY_PROPERTY_TYPES, nameTemplate(entityName), HttpMethod.GET);
        System.err.println(Util.API_PATH + METHOD_ENTITY_PROPERTY_TYPES);
        response.bufferEntity();
        return response;
    }

    public static Response queryEntityPropertyTypes(String entityName, String token) {
        return queryEntityPropertyTypes(entityName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response queryEntityPropertyTypes(String entityName) {
        return queryEntityPropertyTypes(entityName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }
}
