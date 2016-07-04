package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.OutputFormat;
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

import static javax.ws.rs.core.Response.Status.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlApiResponseCodesTests extends SqlExecuteMethod {
    private static final String TEST_PREFIX = "sql-response-codes";
    private static Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");


    @BeforeClass
    public static void createTestData() throws InterruptedException, JSONException, IOException {
        testSeries.setData(Arrays.asList(
                new Sample("2016-06-03T09:23:00.000Z", "16.0"),
                new Sample("2016-06-03T09:26:00.000Z", "8.1"),
                new Sample("2016-06-03T09:36:00.000Z", "6.0"),
                new Sample("2016-06-03T09:41:00.000Z", "19.0")
                )
        );

        boolean isSuccessInsert = SeriesMethod.insertSeries(testSeries, 1000);
        if (!isSuccessInsert) {
            throw new IllegalStateException("Failed to insert series: " + testSeries);
        }
    }

    @Test
    public void testNoQueryParamsGet() {
        final Response response = httpSqlApiResource
                .request()
                .get();
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        response.close();

    }


    @Test
    public void testNoQueryParamsPost() {
        final Response response = httpSqlApiResource
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_FORM_URLENCODED));
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        response.close();
    }


    @Test
    public void testDefaultOutputFormatGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-codes-metric'")
                .request()
                .get();

        Assert.assertEquals(OK.getStatusCode(), response.getStatus());
        response.close();

    }

    @Test
    public void testDefaultOutputFormatPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM 'sql-response-codes-metric'");
        final Response response = httpSqlApiResource
                .request()
                .post(Entity.entity(
                        form,
                        MediaType.APPLICATION_FORM_URLENCODED));
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());
        response.close();

    }


    @Test
    public void testDefaultOutputFormatJsonPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM 'sql-response-codes-metric'");
        form.param("outputFormat", "json");
        final Response response = httpSqlApiResource
                .request()
                .post(Entity.entity(
                        form,
                        MediaType.APPLICATION_FORM_URLENCODED));
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());
        response.close();

    }

    @Test
    public void testDefaultOutputFormatCsvPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM 'sql-response-codes-metric'");
        form.param("outputFormat", "json");
        final Response response = httpSqlApiResource
                .request()
                .post(Entity.entity(
                        form,
                        MediaType.APPLICATION_FORM_URLENCODED));

        Assert.assertEquals(OK.getStatusCode(), response.getStatus());
        response.close();

    }

    @Test
    public void testDefaultOutputFormatJsonGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-codes-metric'")
                .queryParam("outputFormat", OutputFormat.JSON)
                .request()
                .get();
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());
        response.close();

    }

    @Test
    public void testDefaultOutputFormatCsvGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-codes-metric'")
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get();

        Assert.assertEquals(OK.getStatusCode(), response.getStatus());
        response.close();

    }

    @Test
    public void testIncorrectSqlQueryGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT 1")
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get();
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        response.close();

    }


    @Test
    public void testNotAllowedPutRequest() {
        final Response response = httpSqlApiResource
                .request()
                .put(Entity.entity("", MediaType.APPLICATION_JSON));
        Assert.assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

    @Test
    public void testNotAllowedDeleteRequest() {
        final Response response = httpSqlApiResource
                .request()
                .delete();
        Assert.assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

}
