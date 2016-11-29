package com.axibase.tsd.api.method.version;

import com.axibase.tsd.api.model.version.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;


public class VersionMethodTest extends VersionMethod {

    @Test
    public static void testQuery() throws JsonProcessingException {
        Response response;
        try {
            response = queryVersion();
            assertEquals(OK.getStatusCode(), response.getStatus());
            response.readEntity(Version.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}