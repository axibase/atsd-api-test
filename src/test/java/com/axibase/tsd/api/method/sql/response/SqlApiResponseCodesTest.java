package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;

import static javax.ws.rs.core.Response.Status.*;


public class SqlApiResponseCodesTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-response-codes";

    @BeforeClass
    public static void prepareDataSet() throws Exception {
        Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");
        testSeries.setData(Arrays.asList(
                new Sample("2016-06-03T09:23:00.000Z", "16.0"),
                new Sample("2016-06-03T09:26:00.000Z", "8.1"),
                new Sample("2016-06-03T09:36:00.000Z", "6.0"),
                new Sample("2016-06-03T09:41:00.000Z", "19.0")
        ));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(testSeries));
    }

    @Test
    public void testNoQueryParamsGet() {
        final Response response = httpSqlApiResource
                .request()
                .get();
        response.bufferEntity();
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    /**
     * Disabled until #3609 will not be fixed
     */
    @Test(enabled = false)
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
                .queryParam("q", "SELECT FROM")
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get();
        response.bufferEntity();
        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void testIncorrectSqlQueryPOST() {
        Form form = new Form();
        form.param("q", "SELECT FROM");
        form.param("outputFormat", OutputFormat.JSON.toString());
        final Response response = httpSqlApiResource
                .request()
                .post(Entity.form(form));
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
