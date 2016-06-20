package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.Method;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.transport.http.AtsdHttpResponse;
import com.axibase.tsd.api.transport.http.HTTPMethod;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Korchagin.
 */
public class EntityMethod extends Method {
    static final String METHOD_ENTITIES = "/entities/";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public EntityMethod() {
        prepare();
    }

    public void createOrUpdate(final Entity... entities) throws IOException {
        for(Entity e: entities) {
            createOrUpdate(e);
        }
    }

    public void createOrUpdate(final Entity entity) throws IOException {
        final String path = METHOD_ENTITIES + entity.getName();

        Map<String, Object> payload = new HashMap<>();
        payload.put("tags", entity.getTags());
        AtsdHttpResponse response = httpSender.send(HTTPMethod.PUT, path, (new JSONObject(payload).toJSONString()));
        if (response.getCode() != 200) {
            throw new IOException("Fail to insert entity");
        }
        if (!entityExist(entity)) {
            throw new IOException("Fail to check entity property");
        }
    }

    public void deleteEntity(String entityName) throws IOException {
        final String path = METHOD_ENTITIES + entityName;
        Map<String, Object> payload = new HashMap<>();
        AtsdHttpResponse response = httpSender.send(HTTPMethod.DELETE, path, (new JSONObject(payload).toJSONString()));
        if (response.getCode() != 200) {
            throw new IOException("Failed to delete entity");
        }

    }

    Boolean entityExist(final Entity entity) throws IOException {
        return entityExist(entity, true);
    }

    Boolean entityExist(final Entity entity, Boolean allowExtraFields) throws IOException {
        String entityJson = jacksonMapper.writeValueAsString(entity);

        AtsdHttpResponse response = httpSender.sendGet(METHOD_ENTITIES + entity.getName());
        assertEquals(200, response.getCode());
        logger.debug("check: {}\nresponse: {}", entityJson, response.getBody());

        try {
            JSONAssert.assertEquals(entityJson, response.getBody(), allowExtraFields ? JSONCompareMode.LENIENT : JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException e) {
            throw new IOException("Can not deserialize response");
        } catch (AssertionError e) {
            return false;
        }
        return true;
    }
}
