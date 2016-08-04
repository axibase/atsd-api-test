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
        Response response = httpEntitiesApiResource
                .queryParam("tags", tags)
                .queryParam("expression", expression)
                .request()
                .get();
        response.bufferEntity();
        return response;
    }

    public static Response list(String expression) {
        Response response = httpEntitiesApiResource
                .queryParam("expression", expression)
                .request()
                .get();
        response.bufferEntity();
        return response;
    }

    public static Response list(Map<String, String> tags) {
        Response response = httpEntitiesApiResource
                .queryParam("tags", tags)
                .request()
                .get();
        response.bufferEntity();
        return response;
    }

    public static Response list() {
        Response response = httpEntitiesApiResource
                .request()
                .get();
        response.bufferEntity();
        return response;
    }


    public static <T> Response updateEntityGroup(String entityGroup, T query) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .request()
                .method("PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }


    public static <T> Response updateEntityGroup(String entityGroup, T query, String expression, Map<String, String> tags) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .queryParam("expression", expression)
                .queryParam("tags", tags)
                .request()
                .method("PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response updateEntityGroup(String entityGroup, T query, String expression) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .queryParam("expression", expression)
                .request()
                .method("PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response updateEntityGroup(String entityGroup, T query, Map<String, String> tags) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .queryParam("tags", tags)
                .request()
                .method("PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }


    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query, String expression, Map<String, String> tags) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .queryParam("expression", expression)
                .queryParam("tags", tags)
                .request()
                .put(Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .request()
                .put(Entity.json("{}"));
        response.bufferEntity();
        return response;
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query, String expression) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .queryParam("expression", expression)
                .request()
                .put(Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query, Map<String, String> tags) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .queryParam("tags", tags)
                .request()
                .put(Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response createOrReplaceEntityGroup(String entityGroup, T query) {
        Response response = httpEntitiesApiResource.path(entityGroup)
                .request()
                .put(Entity.json(query));
        response.bufferEntity();
        return response;
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
