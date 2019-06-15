package com.axibase.tsd.api.method;

import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.testng.AssertJUnit.*;

@Issue("3616")
public class OptionsMethodTest extends BaseMethod {

    private static final Set<String> ALLOWED_ORIGINS_SET = Collections.singleton("*");
    private static final Set<String> ALLOWED_METHODS_SET = new HashSet<>(asList("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE"));
    private static final Set<String> ALLOWED_HEADERS_SET = new HashSet<>(asList("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization"));

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

    @Issue("3616")
    @Test(dataProvider = "availablePathProvider")
    public static void testResponseOptionsHeadersForURLs(String path, String method) {
        Response response = executeApiRequest(webTarget -> webTarget.path(path)
                .request()
                .header("Access-Control-Request-Method", method)
                .header("Access-Control-Request-Headers", StringUtils.join(ALLOWED_HEADERS_SET, ","))
                .header("Origin", "itdoesntmatter")
                .options());

        assertSame("Bad response status", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));

        assertResponseContainsHeaderWithValues(ALLOWED_METHODS_SET, response, "Access-Control-Allow-Methods");
        assertResponseContainsHeaderWithValues(ALLOWED_HEADERS_SET, response, "Access-Control-Allow-Headers");
        assertResponseContainsHeaderWithValues(ALLOWED_ORIGINS_SET, response, "Access-Control-Allow-Origin");
    }

    @Issue("3616")
    @Test
    public static void testResponseOptionsHeadersForSQL()  {
        Response response = executeRootRequest(webTarget -> webTarget.path("/api/sql")
                .queryParam("q", "")
                .request()
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", StringUtils.join(ALLOWED_HEADERS_SET, ","))
                .header("Origin", "itdoesntmatter")
                .options());

        assertSame("Bad response status", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));

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
