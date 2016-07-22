package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlApiResponseCodesTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-response-codes";

    @BeforeClass
    public static void prepareDataSet() {
        Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");
        sendSamplesToSeries(testSeries,
                new Sample("2016-06-03T09:23:00.000Z", "16.0"),
                new Sample("2016-06-03T09:26:00.000Z", "8.1"),
                new Sample("2016-06-03T09:36:00.000Z", "6.0"),
                new Sample("2016-06-03T09:41:00.000Z", "19.0")
        );
    }

    @Test
    public void testNoQueryParamsGet() {
        final Response response = httpSqlApiResource
                .request()
                .get();
        response.bufferEntity();
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }


    @Test
    public void testNoQueryParamsPost() {
        final Response response = httpSqlApiResource
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_FORM_URLENCODED));
        response.bufferEntity();
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    @Test
    public void testDefaultOutputFormatGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-codes-metric'")
                .request()
                .get();
        response.bufferEntity();
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());

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
        response.bufferEntity();
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());

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
        response.bufferEntity();
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());

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
        response.bufferEntity();
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDefaultOutputFormatJsonGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-codes-metric'")
                .queryParam("outputFormat", OutputFormat.JSON)
                .request()
                .get();
        response.bufferEntity();
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDefaultOutputFormatCsvGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT * FROM 'sql-response-codes-metric'")
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get();
        response.bufferEntity();
        Assert.assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testIncorrectSqlQueryGet() {
        final Response response = httpSqlApiResource
                .queryParam("q", "SELECT 1")
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get();
        response.bufferEntity();
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }


    @Test
    public void testNotAllowedPutRequest() {
        final Response response = httpSqlApiResource
                .request()
                .put(Entity.entity("", MediaType.APPLICATION_JSON));
        response.bufferEntity();
        Assert.assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());

    }

    @Test
    public void testNotAllowedDeleteRequest() {
        final Response response = httpSqlApiResource
                .request()
                .delete();
        response.bufferEntity();
        Assert.assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
    }

}
