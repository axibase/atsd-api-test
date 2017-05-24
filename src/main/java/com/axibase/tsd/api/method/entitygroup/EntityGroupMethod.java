package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.util.NotCheckedException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGroupMethod extends BaseMethod {
    public final static String METHOD_ENTITYGROUP_LIST = "/entity-groups";
    public final static String METHOD_ENTITYGROUP = "/entity-groups/{group}";
    public final static String METHOD_ENTITYGROUP_ENTITIES = "/entity-groups/{group}/entities";
    public final static String METHOD_ENTITYGROUP_ENTITIES_ADD = "/entity-groups/{group}/entities/add";
    public final static String METHOD_ENTITYGROUP_ENTITIES_SET = "/entity-groups/{group}/entities/set";
    public final static String METHOD_ENTITYGROUP_ENTITIES_DELETE = "/entity-groups/{group}/entities/delete";
    public final static String SYNTAX_ALLOWED_ENTITYGROUP_EXPRESSION = "properties('some.prop').size() > 0";

    public static Response getEntityGroup(String groupName) {
        Response response = httpApiResource.path(METHOD_ENTITYGROUP).resolveTemplate("group", groupName).request().get();
        response.bufferEntity();
        return response;
    }

    public static Response listEntityGroup(Map<String, String> parameters) {
        WebTarget target = httpApiResource.path(METHOD_ENTITYGROUP_LIST);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        Response response = target.request().get();
        response.bufferEntity();
        return response;
    }

    public static Response updateEntityGroup(EntityGroup entityGroup) {
        Response response = httpApiResource.path(METHOD_ENTITYGROUP).resolveTemplate("group", entityGroup.getName()).request().method("PATCH", Entity.json(entityGroup));
        response.bufferEntity();
        return response;
    }

    public static Response deleteEntityGroup(String groupName) {
        Response response = httpApiResource.path(METHOD_ENTITYGROUP).resolveTemplate("group", groupName).request().delete();
        response.bufferEntity();
        return response;
    }

    public static Response createOrReplaceEntityGroup(EntityGroup entityGroup) {
        Response response = httpApiResource.path(METHOD_ENTITYGROUP).resolveTemplate("group", entityGroup.getName()).request().put(Entity.json(entityGroup));
        response.bufferEntity();
        return response;
    }

    public static Response getEntities(String groupName, Map<String, String> parameters) {
        WebTarget target = httpApiResource.path(METHOD_ENTITYGROUP_ENTITIES).resolveTemplate("group", groupName);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        Response response = target.request().get();
        response.bufferEntity();
        return response;
    }

    public static Response getEntities(String groupName) {
        return getEntities(groupName, new HashMap<String, String>());
    }

    public static Response addEntities(String groupName, Boolean createEntities, List<String> entityNames) {
        WebTarget target = httpApiResource
                .path(METHOD_ENTITYGROUP_ENTITIES_ADD)
                .resolveTemplate("group", groupName);
        if (createEntities != null) {
            target = target.queryParam("createEntities", createEntities);
        }
        Response response = target.request().post(Entity.json(entityNames));
        response.bufferEntity();
        return response;
    }

    public static Response addEntities(String groupName, List<String> entityNames) {
        return addEntities(groupName, true, entityNames);
    }

    public static Response setEntities(String groupName, Boolean createEntities, List<String> entityNames) {
        WebTarget target = httpApiResource
                .path(METHOD_ENTITYGROUP_ENTITIES_SET)
                .resolveTemplate("group", groupName);
        if (createEntities != null) {
            target = target.queryParam("createEntities", createEntities);
        }
        Response response = target.request().post(Entity.json(entityNames));
        response.bufferEntity();
        return response;
    }

    public static Response setEntities(String groupName, List<String> entityNames) {
        return setEntities(groupName, true, entityNames);
    }

    public static Response deleteEntities(String groupName, List entityNames) {
        Response response = httpApiResource
                .path(METHOD_ENTITYGROUP_ENTITIES_DELETE)
                .resolveTemplate("group", groupName)
                .request()
                .post(Entity.json(entityNames));
        response.bufferEntity();
        return response;
    }

    public static void createOrReplaceEntityGroupCheck(EntityGroup entityGroup) throws Exception {
        Response response = createOrReplaceEntityGroup(entityGroup);
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Fail to execute createOrReplaceEntityGroup query");
        }

        response = getEntityGroup(entityGroup.getName());
        if (response.getStatus() != OK.getStatusCode()) {
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
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Fail to execute getEntityGroup query");
        }

        final String expected = jacksonMapper.writeValueAsString(entityGroup);
        final String given = response.readEntity(String.class);
        return compareJsonString(expected, given, true);
    }

    public static boolean entityGroupExist(String entityGroup) throws NotCheckedException {
        final Response response = EntityGroupMethod.getEntityGroup(entityGroup);
        if (response.getStatus() == OK.getStatusCode()) {
            return true;
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        throw new NotCheckedException("Fail to execute entity group query");
    }


}



