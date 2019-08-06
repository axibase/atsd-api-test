package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupMethod extends BaseMethod {
    private final static String METHOD_ENTITYGROUP = "/entity-groups/{group}";
    private final static String METHOD_ENTITYGROUP_ENTITIES = "/entity-groups/{group}/entities";
    private final static String METHOD_ENTITYGROUP_ENTITIES_ADD = "/entity-groups/{group}/entities/add";
    private final static String METHOD_ENTITYGROUP_ENTITIES_SET = "/entity-groups/{group}/entities/set";
    private final static String METHOD_ENTITYGROUP_ENTITIES_DELETE = "/entity-groups/{group}/entities/delete";
    final static String SYNTAX_ALLOWED_ENTITYGROUP_EXPRESSION = "properties('some.prop').size() > 0";
    private static final String ENTITYGROUP_KEYWORD = "group";

    private static Map<String, Object> nameTemplate(String entityGroupName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ENTITYGROUP_KEYWORD, entityGroupName);
        return Collections.unmodifiableMap(map);
    }

    public static Response createOrReplaceEntityGroup(EntityGroup entityGroup, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP, nameTemplate(entityGroup.getName()),
                HttpMethod.PUT, Entity.json(entityGroup));
        response.bufferEntity();
        return response;
    }

    public static Response createOrReplaceEntityGroup(EntityGroup entityGroup) {
        return createOrReplaceEntityGroup(entityGroup, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response createOrReplaceEntityGroup(EntityGroup entityGroup, String token) {
        return createOrReplaceEntityGroup(entityGroup, new RequestSenderWithBearerAuthorization(token));
    }

    public static void createOrReplaceEntityGroupCheck(EntityGroup entityGroup) throws Exception {
        Response response = createOrReplaceEntityGroup(entityGroup);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute createOrReplaceEntityGroup query");
        }

        response = getEntityGroup(entityGroup.getName());
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute getEntityGroup query");
        }

        if (!compareJsonString(jacksonMapper.writeValueAsString(entityGroup), response.readEntity(String.class))) {
            throw new IllegalStateException("Fail to check entityGroup inserted");
        }
    }

    public static boolean entityGroupExist(EntityGroup entityGroup) throws Exception {
        Response response = getEntityGroup(entityGroup.getName());
        if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute getEntityGroup query");
        }

        final String expected = jacksonMapper.writeValueAsString(entityGroup);
        final String given = response.readEntity(String.class);
        return compareJsonString(expected, given, true);
    }

    public static boolean entityGroupExist(String entityGroup) throws NotCheckedException {
        final Response response = EntityGroupMethod.getEntityGroup(entityGroup);
        if (Response.Status.Family.SUCCESSFUL == Util.responseFamily(response)) {
            return true;
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        throw new NotCheckedException("Fail to execute entity group query");
    }

    public static Response getEntityGroup(String groupName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP, nameTemplate(groupName), HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response getEntityGroup(String groupName) {
        return getEntityGroup(groupName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response getEntityGroup(String groupName, String token) {
        return getEntityGroup(groupName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response updateEntityGroup(EntityGroup entityGroup, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP, nameTemplate(entityGroup.getName()),
                "PATCH", Entity.json(entityGroup));
        response.bufferEntity();
        return response;
    }

    public static Response updateEntityGroup(EntityGroup entityGroup) {
        return updateEntityGroup(entityGroup, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response updateEntityGroup(EntityGroup entityGroup, String token) {
        return updateEntityGroup(entityGroup, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response deleteEntityGroup(String groupName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP, nameTemplate(groupName), HttpMethod.DELETE);
        response.bufferEntity();
        return response;
    }

    public static Response deleteEntityGroup(String groupName) {
        return deleteEntityGroup(groupName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response deleteEntityGroup(String groupName, String token) {
        return deleteEntityGroup(groupName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response getEntities(String groupName, Map<String, String> parameters, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP_ENTITIES, nameTemplate(groupName),
                Util.toStringObjectMap(parameters), Collections.EMPTY_MAP, HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response getEntities(String groupName, Map<String, String> parameters) {
        return getEntities(groupName, parameters, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response getEntities(String groupName) {
        return getEntities(groupName, new HashMap<>());
    }

    public static Response getEntities(String groupName, String token) {
        return getEntities(groupName, Collections.EMPTY_MAP, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response addEntities(String groupName, Boolean createEntities, List<String> entityNames,
                                       RequestSenderWithAuthorization sender) {
        Map<String, Object> parametersMap = new LinkedHashMap<>();
        if (createEntities != null) {
            parametersMap.put("createEntities", createEntities);
        }

        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP_ENTITIES_ADD, nameTemplate(groupName),
                Collections.unmodifiableMap(parametersMap), Collections.EMPTY_MAP, HttpMethod.POST, Entity.json(entityNames));
        response.bufferEntity();
        return response;
    }

    public static Response addEntities(String groupName, Boolean createEntities, List<String> entityNames) {
        return addEntities(groupName, createEntities, entityNames, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response addEntities(String groupName, List<String> entityNames) {
        return addEntities(groupName, true, entityNames);
    }

    public static Response addEntities(String groupName, List<String> entityNames, String token) {
        return addEntities(groupName, true, entityNames, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response setEntities(String groupName, Boolean createEntities, List<String> entityNames,
                                       RequestSenderWithAuthorization sender) {
        Map<String, Object> parametersMap = new LinkedHashMap<>();
        if (createEntities != null) {
            parametersMap.put("createEntities", createEntities);
        }

        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP_ENTITIES_SET, nameTemplate(groupName),
                Collections.unmodifiableMap(parametersMap), Collections.EMPTY_MAP, HttpMethod.POST, Entity.json(entityNames));
        response.bufferEntity();
        return response;
    }

    public static Response setEntities(String groupName, Boolean createEntities, List<String> entityNames) {
        return setEntities(groupName, createEntities, entityNames, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response setEntities(String groupName, List<String> entityNames) {
        return setEntities(groupName, true, entityNames);
    }

    public static Response setEntities(String groupName, List<String> entityNames, String token) {
        return setEntities(groupName, true, entityNames, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response deleteEntities(String groupName, List entityNames, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_ENTITYGROUP_ENTITIES_DELETE, nameTemplate(groupName),
                HttpMethod.POST, Entity.json(entityNames));
        response.bufferEntity();
        return response;
    }

    public static Response deleteEntities(String groupName, List entityNames) {
        return deleteEntities(groupName, entityNames, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response deleteEntities(String groupName, List entityNames, String token) {
        return deleteEntities(groupName, entityNames, new RequestSenderWithBearerAuthorization(token));
    }
}



