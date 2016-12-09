package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.OK;

public class SeriesMethod extends BaseMethod {
    private static final String METHOD_SERIES_INSERT = "/series/insert";
    private static final String METHOD_SERIES_QUERY = "/series/query";
    private static final String METHOD_SERIES_URL_QUERY = "/series/{format}/{entity}/{metric}";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static Response insertSeries(final List<Series> seriesList, String user, String password, boolean sleepEnabled) {
        Invocation.Builder builder = httpApiResource.path(METHOD_SERIES_INSERT).request();

        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);

        Response response = builder.post(Entity.json(seriesList));
        response.bufferEntity();
        try {
            if (sleepEnabled)
                Thread.sleep(DEFAULT_EXPECTED_PROCESSING_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static Response insertSeries(final List<Series> seriesList, String user, String password) {
        return insertSeries(seriesList, user, password, true);
    }

    public static Response insertSeries(final List<Series> seriesList, boolean sleepEnabled) throws FileNotFoundException {
        return insertSeries(seriesList, Config.getInstance().getLogin(), Config.getInstance().getPassword(), sleepEnabled);
    }

    public static Response insertSeries(final List<Series> seriesList) throws FileNotFoundException {
        return insertSeries(seriesList, false);
    }


    public static <T> Response querySeries(T... queries) {
        return querySeries(Arrays.asList(queries));
    }

    public static <T> Response querySeries(List<T> queries) {
        Response response = httpApiResource.path(METHOD_SERIES_QUERY).request().post(Entity.json(queries));
        response.bufferEntity();
        return response;
    }

    public static Response urlQuerySeries(String entity, String metric, OutputFormat format, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, format, parameters, null, null);
    }

    public static Response urlQuerySeries(String entity, String metric, OutputFormat format, Map<String, String> parameters, String user, String password) {
        WebTarget webTarget = httpApiResource
                .path(METHOD_SERIES_URL_QUERY)
                .resolveTemplate("format", format.toString())
                .resolveTemplate("entity", entity)
                .resolveTemplate("metric", metric);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
        }
        Invocation.Builder builder = webTarget.request();

        if (user != null && password != null) {
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
        }
        Response response = builder.get();
        response.bufferEntity();
        return response;
    }

    public static Response urlQuerySeries(String entity, String metric, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, OutputFormat.JSON, parameters);
    }

    public static void insertSeriesCheck(Series... series) throws Exception {
        insertSeriesCheck(Arrays.asList(series));
    }

    public static void insertSeriesCheck(final List<Series> seriesList) throws Exception {
        insertSeriesCheck(seriesList, new SeriesCheck(seriesList));
    }

    public static void insertSeriesCheck(final List<Series> seriesList, AbstractCheck check) throws Exception {
        Response response = insertSeries(seriesList);
        if (OK.getStatusCode() != response.getStatus()) {
            throw new Exception("Fail to execute insertSeries query");
        }
        Checker.check(check);
    }

    public static <T> List<Series> executeQueryReturnSeries(T... seriesQuery) throws Exception {
        Response response = querySeries(seriesQuery);
        return response.readEntity(new GenericType<List<Series>>() {
        });
    }


    public static Response executeQueryRaw(final List<SeriesQuery> seriesQueries) {
        return executeQueryRaw(seriesQueries, null, null);
    }

    public static Response executeQueryRaw(final List<SeriesQuery> seriesQueries, String user, String password) {
        Invocation.Builder builder = httpApiResource.path(METHOD_SERIES_QUERY).request();
        if (user != null && password != null) {
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user);
            builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
        }
        Response response = builder.post(Entity.json(seriesQueries));
        response.bufferEntity();
        return response;
    }

    public static JSONArray executeQuery(final SeriesQuery seriesQuery) throws Exception {
        return executeQuery(Collections.singletonList(seriesQuery));
    }

    public static JSONArray executeQuery(final List<SeriesQuery> seriesQueries) throws Exception {
        Response response = executeQueryRaw(seriesQueries);
        response.bufferEntity();
        return new JSONArray(response.readEntity(String.class));
    }

    public static String getDataField(int index, String field, JSONArray array) throws JSONException {
        if (array == null) {
            return "returnedSeries is null";
        }
        return ((JSONObject) ((JSONArray) ((JSONObject) array.get(0)).get("data")).get(index)).get(field).toString();
    }

    protected String buildSeriesCommandFromSeriesAndSample(Series series, Sample sample) {
        StringBuilder sb = new StringBuilder("series");
        sb.append(" e:\"").append(series.getEntity()).append("\"");
        sb.append(" m:\"").append(series.getMetric()).append("\"=").append(sample.getV());
        sb.append(" d:").append(sample.getD());
        return sb.toString();
    }
}
