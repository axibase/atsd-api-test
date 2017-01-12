package com.axibase.tsd.api.method;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * #3616
 */
public class OptionsMethodTest extends BaseMethod {

    private static final String ALLOWED_METHODS = "GET, POST, PUT, PATCH, DELETE";
    private static final Set<String> ALLOWED_METHODS_SET = splitStringToHeaderValueSet(ALLOWED_METHODS, ", ");
    private static final String ALLOWED_ORIGIN = "*";
    public static final String ALLOWED_HEADERS = "Origin, X-Requested-With, Content-Type, Accept, Authorization";
    private static final Set<String> ALLOWED_HEADERS_SET = splitStringToHeaderValueSet(ALLOWED_HEADERS, ", ");
    private static final boolean STRICT = true;

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

    /**
     * #3616
     */
    @Test(dataProvider = "availablePathProvider")
    public static void testResponseHeadersForAPI(String path) throws Exception {
        Response response = httpApiResource.path(path).request().options();
        assertResponseHasValidStatusAndHeaders(response);
    }

    /**
     * #3616
     */
    @Test
    public static void testResponseHeadersForSQL() throws Exception {
        Response response = httpRootResource.path("/api/sql").request().options();
        assertResponseHasValidStatusAndHeaders(response);
    }

    private static void assertResponseHasValidStatusAndHeaders(Response response) throws Exception {
        assertEquals("Bad response status", Response.Status.OK.getStatusCode(), response.getStatus());
        assertHeaderProvisionedWithValueSet(ALLOWED_METHODS_SET, response, "Access-Control-Allow-Methods", STRICT);
        assertHeaderProvisionedWithValueSet(ALLOWED_HEADERS_SET, response, "Access-Control-Allow-Headers", STRICT);
        assertHeaderProvisionedWithValue(ALLOWED_ORIGIN, response, "Access-Control-Allow-Origin");
    }

    private static Set<String> splitStringToHeaderValueSet(String str, String splitter) {
        return new HashSet<>(Arrays.asList(str.split(splitter)));
    }

    private static void assertHeaderProvisionedWithValue(String expected, Response response, String header) throws Exception {
        String got = response.getHeaderString(header);
        assertEquals(String.format("Invalid %s header value", header), expected, got);
    }

    private static void assertHeaderProvisionedWithValueSet(String expected, Response response, String header) throws Exception {
        assertHeaderProvisionedWithValueSet(expected, response, header, !STRICT);
    }

    private static void assertHeaderProvisionedWithValueSet(Set<String> expectedSet, Response response, String header) throws Exception {
        assertHeaderProvisionedWithValueSet(expectedSet, response, header, !STRICT);
    }

    private static void assertHeaderProvisionedWithValueSet(String expected, Response response, String header, boolean strict) throws Exception {
        assertHeaderProvisionedWithValueSet(splitStringToHeaderValueSet(expected, ", "), response, header, strict);
    }

    private static void assertHeaderProvisionedWithValueSet(Set<String> expectedSet, Response response, String header, boolean strict)  throws Exception {
        String got = response.getHeaderString(header);
        assertNotNull("No such header: " + header, got);
        Set<String> gotSet = splitStringToHeaderValueSet(got, ", ");
        if (!strict && gotSet != null) {
            // Check gotSet contains all of expected set
            gotSet.retainAll(expectedSet);
        }
        assertEquals(String.format("Invalid %s header value set", header), expectedSet, gotSet);
    }
}
