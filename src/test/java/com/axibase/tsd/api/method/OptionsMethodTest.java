package com.axibase.tsd.api.method;

import com.axibase.tsd.api.Config;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by Aleksandr Veselov.
 */
public class OptionsMethodTest extends BaseMethod {

    private static final String ALLOWED_METHODS = "GET, POST, PUT, PATCH, DELETE";
    private static final Set<String> ALLOWED_METHODS_SET = splitStringToSet(ALLOWED_METHODS, ", ");
    private static final String ALLOWED_ORIGIN = "*";
    public static final String ALLOWED_HEADERS = "Origin, X-Requested-With, Content-Type, Accept, Authorization";
    private static final Set<String> ALLOWED_HEADERS_SET = splitStringToSet(ALLOWED_HEADERS, ", ");

    @DataProvider(name = "availablePathProvider")
    Object[][] provideAvailablePaths() {
        return new Object[][] {
                // Data API
                {"/series/query"},
                {"/series/insert"},
                {"/series/csv/entity"},
                {"/series/format/entity/metric"},
                {"/properties/query"},
                {"/properties/insert"},
                {"/properties/delete"},
                {"/properties/entity/types/type"},
                {"/properties/entity/types"},
                {"/messages/query"},
                {"/messages/insert"},
                {"/messages/stats/query"},
                {"/alerts/query"},
                {"/alerts/update"},
                {"/alerts/delete"},
                {"/alerts/history/query"},
                {"/csv"},
                {"/nmon"},
                {"/command"},
                // Meta API
                {"/metrics"},
                {"/metrics/metric"},
                {"/metrics/metric/series"},
                {"/entities"},
                {"/entities/entity"},
                {"/entities/entity/groups"},
                {"/entities/entity/metrics"},
                {"/entities/entity/property-types"},
                {"/entity-groups/"},
                {"/entity-groups/group"},
                {"/entity-groups/group/entities"},
                {"/entity-groups/group/entities/add"},
                {"/entity-groups/group/entities/set"},
                {"/entity-groups/group/entities/delete"},
                {"/version"}
        };
    }

    @Test(dataProvider = "availablePathProvider")
    public static void testOptionsRequest(String path) throws Exception {
        Invocation.Builder builder = httpApiResource.path(path).request();

        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, Config.getInstance().getLogin());
        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, Config.getInstance().getPassword());

        Response response = builder.options();
        assertEquals("Bad response status", Response.Status.OK, response.getStatus());
        assertValidHeaderSet(ALLOWED_METHODS_SET, response, "Access-Control-Allow-Methods", true);
        assertValidHeaderSet(ALLOWED_HEADERS_SET, response, "Access-Control-Allow-Headers", true);
        assertValidHeader(ALLOWED_ORIGIN, response, "Access-Control-Allow-Origin");
    }

    @Test
    public static void testOptionsRequestForSQL() throws Exception {
        Invocation.Builder builder = httpRootResource.path("/api/sql").request();

        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, Config.getInstance().getLogin());
        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, Config.getInstance().getPassword());

        Response response = builder.options();
        assertEquals("Bad response status", Response.Status.OK, response.getStatus());
        assertValidHeaderSet(ALLOWED_METHODS_SET, response, "Access-Control-Allow-Methods", true);
        assertValidHeaderSet(ALLOWED_HEADERS_SET, response, "Access-Control-Allow-Headers", true);
        assertValidHeader(ALLOWED_ORIGIN, response, "Access-Control-Allow-Origin");
    }

    private static Set<String> splitStringToSet(String str, String splitter) {
        return new HashSet<>(Arrays.asList(str.split(splitter)));
    }

    private static void assertValidHeader(String expected, Response response, String header) throws Exception {
        String got = response.getHeaderString(header);
        assertEquals(String.format("Invalid %s header set", header), expected, got);
    }

    private static void assertValidHeaderSet(String expected, Response response, String header) throws Exception {
        assertValidHeaderSet(expected, response, header, false);
    }

    private static void assertValidHeaderSet(Set<String> expectedSet, Response response, String header)  throws Exception {
        assertValidHeaderSet(expectedSet, response, header, false);
    }

    private static void assertValidHeaderSet(String expected, Response response, String header, boolean strict) throws Exception {
        assertValidHeaderSet(splitStringToSet(expected, ", "), response, header, strict);
    }

    private static void assertValidHeaderSet(Set<String> expectedSet, Response response, String header, boolean strict)  throws Exception {
        String got = response.getHeaderString(header);
        Set<String> gotSet = splitStringToSet(got, ", ");
        boolean acceptable = strict ? expectedSet.equals(gotSet)
                                    : gotSet.containsAll(gotSet);
        String errMsg = String.format("Invalid %s header set, expected %s, got %s",
                header, StringUtils.join(expectedSet), StringUtils.join(gotSet));
        assertTrue(errMsg, acceptable);
    }
}
