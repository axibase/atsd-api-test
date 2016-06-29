package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlExecuteMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Igor Shmagrinslkiy
 */
public class SqlApiResponseHeadersTests extends SqlExecuteMethod {
    private static final String TEST_PREFIX = "sql-response-headers";
    private static Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");
    private static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String CONTENT_TYPE = "Content-type";

    @BeforeClass
    public static void createTestData() throws InterruptedException, JSONException, IOException {
        final Sample[] testSamples = {
                new Sample("2016-06-03T09:23:00.000Z", "16.0"),
                new Sample("2016-06-03T09:26:00.000Z", "8.1"),
                new Sample("2016-06-03T09:36:00.000Z", "6.0"),
                new Sample("2016-06-03T09:41:00.000Z", "19.0")
        };
        testSeries.setData(Arrays.asList(testSamples));
        boolean isSuccessInsert = SeriesMethod.insertSeries(testSeries, 1000);
        if (!isSuccessInsert) {
            throw new IllegalStateException("Failed to insert series: " + testSeries);
        }
    }


    @Test
    public void testAllowMethods() {
        final String expectedAllowMethods = "HEAD, GET, POST, PUT, PATCH, DELETE";
        Response response = httpSqlApiResource
                .request()
                .head();
        final String responseAllowMethods = response.getHeaderString(ALLOW_METHODS);
        Assert.assertEquals(expectedAllowMethods, responseAllowMethods);
    }

    @Test
    public void testContentTypeJsonGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-headers-metric'")
                .request()
                .get();
        Assert.assertEquals("application/json; charset=UTF-8", response.getHeaderString(CONTENT_TYPE));
    }

    @Test
    public void testContentTypeCsvGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-headers-metric'")
                .queryParam("outputFormat", "csv")
                .request()
                .get();
        Assert.assertEquals("text/csv;charset=UTF-8", response.getHeaderString(CONTENT_TYPE));
    }

    @Test
    public void testContentTypeJsonPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM 'sql-response-headers-metric'");
        form.param("outputFormat", "json");
        final Response response = httpSqlApiResource
                .request()
                .post(Entity.entity(form,
                        MediaType.APPLICATION_FORM_URLENCODED));
        Assert.assertEquals("application/json; charset=UTF-8", response.getHeaderString(CONTENT_TYPE));
    }
}
