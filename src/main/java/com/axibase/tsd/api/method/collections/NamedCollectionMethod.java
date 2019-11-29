package com.axibase.tsd.api.method.collections;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.collections.NamedCollection;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

public class NamedCollectionMethod extends BaseMethod {
    private static final String COLLECTIONS_PATH = "/collections/name";

    public static Response insertCollection(NamedCollection collection) {
        Response response = executeRootRequest(webTarget -> webTarget
                .path(COLLECTIONS_PATH)
                .queryParam("name", collection.getName())
                .queryParam("values", String.join(",", collection.getItems()))
                .queryParam("save", "Save")
                .request()
                .method(HttpMethod.POST));
        response.bufferEntity();
        return response;
    }
}
