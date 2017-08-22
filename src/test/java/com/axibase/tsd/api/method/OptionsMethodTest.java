package com.axibase.tsd.api.method;

import jersey.repackaged.com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * #3616
 */
public class OptionsMethodTest extends BaseMethod {

    private static final Set<String> ALLOWED_ORIGINS_SET = Sets.newHashSet("*");
    private static final Set<String> ALLOWED_METHODS_SET = Sets.newHashSet("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> ALLOWED_HEADERS_SET = Sets.newHashSet("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization");

    @DataProvider(name = "availablePathProvider")
    Object[][] provideAvailablePaths() {
        return new Object[][]{
                // Data API
                {"/series/query", "POST"},
                {"/series/insert", "POST"},
                {"/series/csv/entity", "POST"},
                {"/series/format/entity/metric", "GET"},
                {"/properties/query", "POST"},
                {"/properties/insert", "POST"},
                {"/properties/delete", "POST"},
                {"/properties/entity/types/type", "GET"},
                {"/properties/entity/types", "GET"},
                {"/messages/query", "POST"},
                {"/messages/insert", "POST"},
                {"/messages/stats/query", "POST"},
                {"/alerts/query", "POST"},
                {"/alerts/update", "POST"},
                {"/alerts/delete", "POST"},
                {"/alerts/history/query", "POST"},
                {"/csv", "POST"},
                {"/nmon", "POST"},
                {"/command", "POST"},
                // Meta API
                {"/metrics", "GET"},
                {"/metrics/metric", "GET"},
                {"/metrics/metric", "PUT"},
                {"/metrics/metric", "PATCH"},
                {"/metrics/metric", "DELETE"},
                {"/metrics/metric/series", "GET"},
                {"/entities", "GET"},
                {"/entities", "POST"},
                {"/entities/entity", "GET"},
                {"/entities/entity", "PUT"},
                {"/entities/entity", "PATCH"},
                {"/entities/entity", "DELETE"},
                {"/entities/entity/groups", "GET"},
                {"/entities/entity/metrics", "GET"},
                {"/entities/entity/property-types", "GET"},
                {"/entity-groups", "GET"},
                {"/entity-groups/group", "GET"},
                {"/entity-groups/group", "PUT"},
                {"/entity-groups/group", "PATCH"},
                {"/entity-groups/group", "DELETE"},
                {"/entity-groups/group/entities", "GET"},
                {"/entity-groups/group/entities/add", "POST"},
                {"/entity-groups/group/entities/set", "POST"},
                {"/entity-groups/group/entities/delete", "POST"},
                {"/search", "GET"},
                {"/version", "GET"},
        };
    }

    /**
     * #3616
     */
    @Test(dataProvider = "availablePathProvider")
    public static void testResponseOptionsHeadersForURLs(String path, String method) throws Exception {
        Response response = httpApiResource.path(path)
                .request()
                .header("Access-Control-Request-Method", method)
                .header("Access-Control-Request-Headers", StringUtils.join(ALLOWED_HEADERS_SET, ","))
                .header("Origin", "itdoesntmatter")
                .options();

        assertEquals("Bad response status", Response.Status.OK.getStatusCode(), response.getStatus());

        assertResponseContainsHeaderWithValues(ALLOWED_METHODS_SET, response, "Access-Control-Allow-Methods");
        assertResponseContainsHeaderWithValues(ALLOWED_HEADERS_SET, response, "Access-Control-Allow-Headers");
        assertResponseContainsHeaderWithValues(ALLOWED_ORIGINS_SET, response, "Access-Control-Allow-Origin");
    }

    /**
     * #3616
     */
    @Test
    public static void testResponseOptionsHeadersForSQL() throws Exception {
        Response response = httpRootResource.path("/api/sql")
                .queryParam("q", "")
                .request()
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", StringUtils.join(ALLOWED_HEADERS_SET, ","))
                .header("Origin", "itdoesntmatter")
                .options();

        assertEquals("Bad response status", Response.Status.OK.getStatusCode(), response.getStatus());

        assertResponseContainsHeaderWithValues(ALLOWED_METHODS_SET, response, "Access-Control-Allow-Methods");
        assertResponseContainsHeaderWithValues(ALLOWED_HEADERS_SET, response, "Access-Control-Allow-Headers");
        assertResponseContainsHeaderWithValues(ALLOWED_ORIGINS_SET, response, "Access-Control-Allow-Origin");
    }

    private static void assertResponseContainsHeaderWithValues(Set<String> expected, Response response, String headerName) {
        String headerValue = response.getHeaderString(headerName);
        assertNotNull("No such header: " + headerName, headerValue);
        assertEquals(String.format("Invalid %s header value", headerName), expected, splitByComma(headerValue));
    }

    private static Set<String> splitByComma(String str) {
        Set<String> values = new HashSet<>();
        for (String value : str.split(",")) {
            values.add(value.trim());
        }
        return values;
    }

}
