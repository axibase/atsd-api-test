package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Collections;

import static javax.ws.rs.core.Response.Status.*;
import static org.testng.AssertJUnit.assertEquals;


public class SqlApiResponseCodesTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-response-codes";

    @BeforeClass
    public static void prepareDataSet() throws Exception {
        Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");
        testSeries.addSamples(
                Sample.ofDateInteger("2016-06-03T09:23:00.000Z", 16),
                Sample.ofDateDecimal("2016-06-03T09:26:00.000Z", new BigDecimal("8.1")),
                Sample.ofDateInteger("2016-06-03T09:36:00.000Z", 6),
                Sample.ofDateInteger("2016-06-03T09:41:00.000Z", 19)
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(testSeries));
    }

    @Test
    public void testNoQueryParamsGet() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .get());
        response.bufferEntity();
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Issue("3609")
    @Test
    public void testNoQueryParamsPost() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_FORM_URLENCODED)));
        response.bufferEntity();
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    @Test
    public void testDefaultOutputFormatGet() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .queryParam("q", "SELECT * FROM \"sql-response-codes-metric\"")
                .request()
                .get());
        response.bufferEntity();
        assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDefaultOutputFormatPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM \"sql-response-codes-metric\"");
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .post(Entity.entity(
                        form,
                        MediaType.APPLICATION_FORM_URLENCODED)));
        response.bufferEntity();
        assertEquals(OK.getStatusCode(), response.getStatus());

    }


    @Test
    public void testDefaultOutputFormatJsonPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM \"sql-response-codes-metric\"");
        form.param("outputFormat", "json");
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .post(Entity.entity(
                        form,
                        MediaType.APPLICATION_FORM_URLENCODED)));
        response.bufferEntity();
        assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDefaultOutputFormatCsvPost() {
        final Form form = new Form();
        form.param("q", "SELECT * FROM \"sql-response-codes-metric\"");
        form.param("outputFormat", "csv");
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .post(Entity.entity(
                        form,
                        MediaType.APPLICATION_FORM_URLENCODED)));
        response.bufferEntity();
        assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDefaultOutputFormatJsonGet() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .queryParam("q", "SELECT * FROM \"sql-response-codes-metric\"")
                .queryParam("outputFormat", OutputFormat.JSON)
                .request()
                .get());
        response.bufferEntity();
        assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDefaultOutputFormatCsvGet() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .queryParam("q", "SELECT * FROM \"sql-response-codes-metric\"")
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get());
        response.bufferEntity();
        assertEquals(OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testIncorrectSqlQueryGet() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .queryParam("q", "SELECT FROM")
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get());
        response.bufferEntity();
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void testIncorrectSqlQueryPOST() {
        Form form = new Form();
        form.param("q", "SELECT FROM");
        form.param("outputFormat", OutputFormat.JSON.toString());
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .post(Entity.form(form)));
        response.bufferEntity();
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    @Test
    public void testNotAllowedPutRequest() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .put(Entity.entity("", MediaType.APPLICATION_JSON)));
        response.bufferEntity();
        assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());

    }

    @Test
    public void testNotAllowedDeleteRequest() {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .request()
                .delete());
        response.bufferEntity();
        assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
    }

}
