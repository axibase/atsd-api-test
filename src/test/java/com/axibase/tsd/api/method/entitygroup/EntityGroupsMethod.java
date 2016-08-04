package com.axibase.tsd.api.method.entitygroup;

import com.axibase.tsd.api.method.BaseMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Shmagrinskiy
 */
public class EntityGroupsMethod extends BaseMethod {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static WebTarget httpEntitiesApiResource = httpApiResource.path("/entity-groups/");

    public static Response list(String expression, Map<String, String> tags) {
        WebTarget resource = httpEntitiesApiResource;
        if (tags != null) {
            resource = resource.queryParam("tags", tags);
        }
        if (expression != null) {
            resource = resource.queryParam("expression", expression);
        }

        Response response = resource
                .request()
                .get();
        response.bufferEntity();
        return response;
    }

    public static Response list(String expression) {
        return list(expression, null);
    }

    public static Response list(Map<String, String> tags) {
        return list(null, tags);
    }

    public static Response list() {
        return list(null, null);
    }


    public static <T> Response updateEntityGroup(String entityGroup, T query, String expression, Map<String, String> tags) {
        WebTarget resource = httpEntitiesApiResource.path(entityGroup);
        if (tags != null) {
            resource = resource.queryParam("tags", tags);
        }
        if (expression != null) {
            resource = resource.queryParam("expression", expression);
        }
        Response response = resource
                .request()
                .method("PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response updateEntityGroup(String entityGroup, T query, String expression) {
        return updateEntityGroup(entityGroup, query, expression, null);
    }

    public static <T> Response updateEntityGroup(String entityGroup, T query, Map<String, String> tags) {
        return updateEntityGroup(entityGroup, query, null, tags);
    }

    public static <T> Response updateEntityGroup(String entityGroup, T query) {
        return updateEntityGroup(entityGroup, query, null, null);
    }


    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query, String expression, Map<String, String> tags) {
        WebTarget resource = httpEntitiesApiResource.path(entityGroup);
        if (tags != null) {
            resource = resource.queryParam("tags", tags);
        }
        if (expression != null) {
            resource = resource.queryParam("expression", expression);
        }
        Response response = resource
                .request()
                .put(Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup) {
        return createOrReplaceEntityGroup(entityGroup, "{}", null, null);
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query, String expression) {
        return createOrReplaceEntityGroup(entityGroup, query, expression, null);
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query, Map<String, String> tags) {
        return createOrReplaceEntityGroup(entityGroup, query, null, tags);
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query) {
        return createOrReplaceEntityGroup(entityGroup, query, null, null);
    }

    public static Response getEntityGroup(String entityGroup) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .request()
                .get();
        response.bufferEntity();
        return response;
    }


    public static Response delete(String entityGroup) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .request()
                .delete();
        response.bufferEntity();
        return response;
    }


    public static Response getEntitiesOfEntityGroup(String entityGroup) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .request()
                .get();
        response.bufferEntity();
        return response;
    }

    public static Response addEntitiesToEntityGroup(String entityGroup, List<String> entitiesNames) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .path("entities")
                .path("add")
                .request()
                .post(Entity.json(entitiesNames));
        response.bufferEntity();
        return response;
    }

    public static Response setEntitiesToEntityGroup(String entityGroup, List<String> entitiesNames) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .path("entities")
                .path("set")
                .request()
                .post(Entity.json(entitiesNames));
        response.bufferEntity();
        return response;
    }

    public static Response deleteEntitesFromEntityGroup(String entityGroup, List<String> entitiesNames) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .path("entities")
                .path("delete")
                .request()
                .post(Entity.json(entitiesNames));
        response.bufferEntity();
        return response;
    }


}
