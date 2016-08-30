package com.axibase.tsd.api.method;

import com.axibase.tsd.api.method.series.CSVInsertMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AuthenticationTest extends BaseMethod {
    private static final String UNKNOWN_USER = "Unknown User";
    private static final String UNKNOWN_USER_PASSWORD = "Unknown User Password";
    public static final String BAD_CREDENTIALS_ERROR_MESSAGE = "org.springframework.security.authentication.BadCredentialsException: Bad credentials";

    /**
     * #2870
     */
    @Test
    public void seriesQueryTest() throws Exception {
        List<SeriesQuery> seriesQueryList = Collections.singletonList(new SeriesQuery());
        Response response = SeriesMethod.executeQueryRaw(seriesQueryList, UNKNOWN_USER, UNKNOWN_USER_PASSWORD);

        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        String errorMessage = extractErrorMessage(response);
        Assert.assertEquals(errorMessage, BAD_CREDENTIALS_ERROR_MESSAGE);
    }

    /**
     * #2870
     */
    @Test
    public void seriesInsertTest() throws Exception {
        List<Series> seriesQueryList = Collections.singletonList(new Series());
        Response response = SeriesMethod.insertSeries(seriesQueryList, UNKNOWN_USER, UNKNOWN_USER_PASSWORD);

        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        String errorMessage = extractErrorMessage(response);
        Assert.assertEquals(errorMessage, BAD_CREDENTIALS_ERROR_MESSAGE);
    }

    /**
     * #2870
     */
    @Test
    public void seriesCSVInsertTest() throws Exception {
        Response response = CSVInsertMethod.csvInsert("entity", "some csv", new HashMap<String, String>(), UNKNOWN_USER, UNKNOWN_USER_PASSWORD);

        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        String errorMessage = extractErrorMessage(response);
        Assert.assertEquals(errorMessage, BAD_CREDENTIALS_ERROR_MESSAGE);
    }

    /**
     * #2870
     */
    @Test
    public void seriesUrlQueryTest() throws Exception {
        Response response = SeriesMethod.urlQuerySeries("entity", "metric", OutputFormat.JSON, new HashMap < String, String > (), UNKNOWN_USER, UNKNOWN_USER_PASSWORD);

        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        String errorMessage = extractErrorMessage(response);
        Assert.assertEquals(errorMessage, BAD_CREDENTIALS_ERROR_MESSAGE);
    }
}
