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
        return new Object[][] {
                // Data API
                {"/api/v1/series/query"},
                {"/api/v1/series/insert"},
                {"/api/v1/series/csv/entity"},
                {"/api/v1/series/format/entity/metric"},
                {"/api/v1/properties/query"},
                {"/api/v1/properties/insert"},
                {"/api/v1/properties/delete"},
                {"/api/v1/properties/entity/types/type"},
                {"/api/v1/properties/entity/types"},
                {"/api/v1/messages/query"},
                {"/api/v1/messages/insert"},
                {"/api/v1/messages/stats/query"},
                {"/api/v1/alerts/query"},
                {"/api/v1/alerts/update"},
                {"/api/v1/alerts/delete"},
                {"/api/v1/alerts/history/query"},
                {"/api/v1/csv"},
                {"/api/v1/nmon"},
                {"/api/v1/command"},
                // Meta API
                {"/api/v1/metrics"},
                {"/api/v1/metrics/metric"},
                {"/api/v1/metrics/metric/series"},
                {"/api/v1/entities"},
                {"/api/v1/entities/entity"},
                {"/api/v1/entities/entity/groups"},
                {"/api/v1/entities/entity/metrics"},
                {"/api/v1/entities/entity/property-types"},
                {"/api/v1/entity-groups/"},
                {"/api/v1/entity-groups/group"},
                {"/api/v1/entity-groups/group/entities"},
                {"/api/v1/entity-groups/group/entities/add"},
                {"/api/v1/entity-groups/group/entities/set"},
                {"/api/v1/entity-groups/group/entities/delete"},
                {"/api/v1/version"},
                // SQL
                // Parameter stub is required (LOL :D)
                {"/api/sql?q="}
        };
    }

    /**
     * #3616
     */
    @Test(dataProvider = "availablePathProvider")
    public static void testResponseOptionsHeadersForURLs(String path) throws Exception {
        Response response = httpRootResource.path(path).request()
                .header("Access-Control-Request-Method", "GET")
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
        for (String value: str.split(",")) {
            values.add(value.trim());
        }
        return values;
    }

}
