package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.apache.http.entity.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Igor Shmagrinslkiy
 */
public class SqlApiResponseHeadersTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-response-headers";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String CONTENT_TYPE = "Content-type";

    @BeforeClass
    public static void prepareDataSet() {
        Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");
        testSeries.addSamples(
                Sample.ofDateInteger("2016-06-03T09:23:00.000Z", 16),
                Sample.ofDateDecimal("2016-06-03T09:26:00.000Z", new BigDecimal("8.1")),
                Sample.ofDateInteger("2016-06-03T09:36:00.000Z", 6),
                Sample.ofDateInteger("2016-06-03T09:41:00.000Z", 19)
        );
    }


    /**
     * Disabled until #3609 will not be fixed
     */
    @Test(enabled = false)
    public void testAllowMethods() {
        Set<String> expectedAllowedMethods = new HashSet<>(Arrays.asList("HEAD", "GET", "POST", "OPTIONS"));
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .head());
        Set<String> responseAllowedMethods = parseResponseAllowedMethods(response);
        response.bufferEntity();
        assertEquals(expectedAllowedMethods, responseAllowedMethods);
    }


    @Test
    public void testContentTypeJsonGet() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .queryParam("q", "SELECT * FROM \"sql-response-headers-metric\"")
                .request()
                .get());
        response.bufferEntity();
        assertContentTypeEquals("text/csv", response.getHeaderString(CONTENT_TYPE));
    }

    @Test
    public void testContentTypeCsvGet() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .queryParam("q", "SELECT * FROM \"sql-response-headers-metric\"")
                .queryParam("outputFormat", "csv")
                .request()
                .get());
        response.bufferEntity();
        assertContentTypeEquals("text/csv", response.getHeaderString(CONTENT_TYPE));
    }

    @Test
    public void testContentTypeJsonPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM \"sql-response-headers-metric\"");
        form.param("outputFormat", "json");
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .post(Entity.entity(form,
                        MediaType.APPLICATION_FORM_URLENCODED)));
        response.bufferEntity();
        assertContentTypeEquals(MediaType.APPLICATION_JSON, response.getHeaderString(CONTENT_TYPE));
    }

    private Set<String> parseResponseAllowedMethods(Response response) {
        return new HashSet<>(Arrays.asList(
                response.getHeaderString(ACCESS_CONTROL_ALLOW_METHODS)
                        .replace(" ", "")
                        .split(",")
        ));
    }

    private void assertContentTypeEquals(String mimeType, final String inputContentType) {
        ContentType contentType = ContentType.parse(inputContentType);
        assertEquals(mimeType, contentType.getMimeType());
        assertEquals(StandardCharsets.UTF_8, contentType.getCharset());
    }
}
