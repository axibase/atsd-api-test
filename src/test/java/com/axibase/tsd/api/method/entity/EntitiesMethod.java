package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.BaseMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
public class EntitiesMethod extends BaseMethod {
    static final String METHOD_ENTITY_LIST = "/entities/";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static WebTarget httpEntitiesApiResource = httpApiResource.path(METHOD_ENTITY_LIST);


    public static Response list() {
        Response response = httpEntitiesApiResource
                .request()
                .get();
        response.bufferEntity();
        return response;
    }

    private static EntityMethod entity(String entityName) {
        return new EntityMethod(entityName);
    }

    public static void main(String[] args) {
        Response response = EntitiesMethod
                .entity("atsd")
                .get();
        System.out.println(response.readEntity(String.class));
    }

    private static class EntityMethod {
        private static WebTarget httpEntityApiResource;

        EntityMethod(String entityName) {
            httpEntityApiResource = httpEntitiesApiResource.path(entityName);
        }

        public static <T> Response createOrReplace(T query) throws Exception {
            Response response = httpEntityApiResource
                    .request()
                    .put(Entity.json(query));
            response.bufferEntity();
            return response;
        }

        public static Response get() {
            Response response = httpEntityApiResource
                    .request()
                    .get();
            response.bufferEntity();
            return response;
        }

        public static Response delete() {
            Response response = httpEntityApiResource
                    .request()
                    .delete();
            response.bufferEntity();
            return response;
        }

        public static Response metrics(Map<String, String> parameters) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                httpEntityApiResource.queryParam(entry.getKey(), entry.getValue());
            }
            Response response = httpEntityApiResource
                    .path("/metrics")
                    .request()
                    .get();
            response.bufferEntity();
            return response;
        }


        public static <T> Response update(T query) {
            Response response = httpEntityApiResource
                    .request()
                    .method("PATCH", Entity.json(query));
            response.bufferEntity();
            return response;
        }

        public static <T> Response propertyTypes(T query) {
            Response response = httpApiResource.path("/property-types").request().get();
            response.bufferEntity();
            return response;
        }

        public static <T> Response groups(T query) {
            Response response = httpApiResource
                    .path("/groups")
                    .request()
                    .get();
            response.bufferEntity();
            return response;
        }

    }
}
