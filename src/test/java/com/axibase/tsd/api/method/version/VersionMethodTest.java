package com.axibase.tsd.api.method.version;

import com.axibase.tsd.api.model.version.Version;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;


public class VersionMethodTest extends VersionMethod {

    @Test
    public static void testQuery() {
        Response response;
        try {
            response = queryVersion();
            assertSame(Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
            response.readEntity(Version.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}