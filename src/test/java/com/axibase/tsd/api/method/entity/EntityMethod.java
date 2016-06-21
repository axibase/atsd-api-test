package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.Method;
import com.axibase.tsd.api.model.entity.Entity;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    public void createOrUpdateCheck(final Entity... entities) throws IOException {
        for(Entity e: entities) {
            createOrUpdateCheck(e);
        }
    }

    public void createOrUpdateCheck(final Entity entity) throws IOException {
        final String path = METHOD_ENTITIES + entity.getName();

        Map<String, Object> payload = new HashMap<>();
        payload.put("tags", entity.getTags());
        Response response = httpResource.path(METHOD_ENTITIES).path("{entity}").resolveTemplate("entity", entity.getName()).request().put(javax.ws.rs.client.Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() != 200) {
            throw new IOException("Fail to insert entity");
        }
        if (!entityExist(entity)) {
            throw new IOException("Fail to check entity property");
        }
    }


    Boolean entityExist(final Entity entity) throws IOException {
        return entityExist(entity, true);
    }

    Boolean entityExist(final Entity entity, Boolean allowExtraFields) throws IOException {
        String entityJson = jacksonMapper.writeValueAsString(entity);
        Response response = httpResource.path(METHOD_ENTITIES).path("{entity}").resolveTemplate("entity", entity.getName()).request().get();
        assertEquals(200, response.getStatus());
        String responseJson = response.readEntity(String.class);
        logger.debug("check: {}\nresponse: {}", entityJson, responseJson);

        try {
            JSONAssert.assertEquals(entityJson, responseJson, allowExtraFields ? JSONCompareMode.LENIENT : JSONCompareMode.NON_EXTENSIBLE);
        } catch (JSONException e) {
            throw new IOException("Can not deserialize response");
        } catch (AssertionError e) {
            return false;
        }
        return true;
    }
}
