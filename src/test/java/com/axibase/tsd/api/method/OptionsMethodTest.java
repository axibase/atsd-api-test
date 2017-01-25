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
                {"/series/query",                           new String[]{"POST"}},
                {"/series/insert",                          new String[]{"POST"}},
                {"/series/csv/entity",                      new String[]{"POST"}},
                {"/series/format/entity/metric",            new String[]{"GET"}},
                {"/properties/query",                       new String[]{"POST"}},
                {"/properties/insert",                      new String[]{"POST"}},
                {"/properties/delete",                      new String[]{"POST"}},
                {"/properties/entity/types/type",           new String[]{"GET"}},
                {"/properties/entity/types",                new String[]{"GET"}},
                {"/messages/query",                         new String[]{"POST"}},
                {"/messages/insert",                        new String[]{"POST"}},
                {"/messages/stats/query",                   new String[]{"POST"}},
                {"/alerts/query",                           new String[]{"POST"}},
                {"/alerts/update",                          new String[]{"POST"}},
                {"/alerts/delete",                          new String[]{"POST"}},
                {"/alerts/history/query",                   new String[]{"POST"}},
                {"/csv",                                    new String[]{"POST"}},
                {"/nmon",                                   new String[]{"POST"}},
                {"/command",                                new String[]{"POST"}},
                // Meta API
                {"/metrics",                                new String[]{"GET"}},
                {"/metrics/metric",                         new String[]{"GET", "PUT", "PATCH", "DELETE"}},
                {"/metrics/metric/series",                  new String[]{"GET"}},
                {"/entities",                               new String[]{"GET", "POST"}},
                {"/entities/entity",                        new String[]{"GET", "PUT", "PATCH", "DELETE"}},
                {"/entities/entity/groups",                 new String[]{"GET"}},
                {"/entities/entity/metrics",                new String[]{"GET"}},
                {"/entities/entity/property-types",         new String[]{"GET"}},
                {"/entity-groups",                          new String[]{"GET"}},
                {"/entity-groups/group",                    new String[]{"GET", "PUT", "PATCH", "DELETE"}},
                {"/entity-groups/group/entities",           new String[]{"GET",}},
                {"/entity-groups/group/entities/add",       new String[]{"POST"}},
                {"/entity-groups/group/entities/set",       new String[]{"POST"}},
                {"/entity-groups/group/entities/delete",    new String[]{"POST"}},
                {"/version",                                new String[]{"GET"}}
        };
    }

    /**
     * #3616
     */
    @Test(dataProvider = "availablePathProvider")
    public static void testResponseOptionsHeadersForURLs(String path, String[] methods) throws Exception {
        for (String method: methods) {
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
    }

    /**
     * #3616
     */
    @Test(dataProvider = "availablePathProvider")
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
        for (String value: str.split(",")) {
            values.add(value.trim());
        }
        return values;
    }

}
