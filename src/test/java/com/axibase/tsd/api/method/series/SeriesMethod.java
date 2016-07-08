package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

import static javax.ws.rs.core.Response.Status.OK;

public class SeriesMethod extends BaseMethod {
    private static final String METHOD_SERIES_INSERT = "/series/insert";
    private static final String METHOD_SERIES_QUERY = "/series/query";
    private static final String METHOD_SERIES_URL_QUERY = "/series/{format}/{entity}/{metric}";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static Response insertSeries(final List<Series> seriesList) {
        Response response = httpApiResource.path(METHOD_SERIES_INSERT).request().post(Entity.json(seriesList));
        response.bufferEntity();
        return response;
    }


    public static Response insertSeries(final Series series) {
        return insertSeries(Collections.singletonList(series));
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
        WebTarget webTarget = httpApiResource
                .path(METHOD_SERIES_URL_QUERY)
                .resolveTemplate("format", format.toString())
                .resolveTemplate("entity", entity)
                .resolveTemplate("metric", metric);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
        }

        Response response = webTarget.request().get();
        response.bufferEntity();
        return response;
    }

    public static Response urlQuerySeries(String entity, String metric, Map<String, String> parameters) {
        return urlQuerySeries(entity, metric, OutputFormat.JSON, parameters);
    }

    public static void insertSeriesCheck(final Series series, long checkTimeoutMillis) throws IOException {
        insertSeriesCheck(Collections.singletonList(series), checkTimeoutMillis);
    }

    public static void insertSeriesCheck(final List<Series> seriesList, long checkTimeoutMillis) throws IOException {
        Response response = insertSeries(seriesList);
        if (OK.getStatusCode() != response.getStatus()) {
            throw new IOException("Fail to execute insertSeries query");
        }
        final long startCheckTimeMillis = System.currentTimeMillis();
        do {
            if (seriesListIsInserted(seriesList)) {
                return;
            }
            try {
                Thread.sleep(Util.REQUEST_INTERVAL);
            } catch (InterruptedException e) {
                throw new IOException("Fail to check inserted queries: checking was interrupted.");
            }
        } while (System.currentTimeMillis() <= startCheckTimeMillis + checkTimeoutMillis);
        if (!seriesListIsInserted(seriesList)) {
            throw new IOException("Fail to check inserted queries");
        }
    }

    public static void insertSeriesCheck(final Series series) throws IOException {
        insertSeriesCheck(series, Util.EXPECTED_PROCESSING_TIME);
    }

    public static void insertSeriesCheck(final List<Series> series) throws IOException {
        insertSeriesCheck(series, Util.EXPECTED_PROCESSING_TIME);
    }

    private static boolean seriesListIsInserted(final List<Series> seriesList) throws IOException {
        List<SeriesQuery> seriesQueryList = new ArrayList<>();
        for (final Series series : seriesList) {
            seriesQueryList.add(new SeriesQuery(series) {{
                setTags(series.getTags());
            }});
        }
        Response response = querySeries(seriesQueryList);
        List<Series> expectedList = new ArrayList<>();
        for (final Series series : seriesList) {
            final Map<String, String> formattedTags = new HashMap<>();
            for (String key : series.getTags().keySet()) {
                formattedTags.put(key.toLowerCase(), series.getTags().get(key));
            }
            expectedList.add(new Series() {{
                setEntity(series.getEntity().toLowerCase());
                setData(series.getData());
                setMetric(series.getMetric());
                setTags(formattedTags);
            }});
        }
        String expected = jacksonMapper.writeValueAsString(expectedList);
        String  actual = response.readEntity(String.class);
        return compareJsonString(expected, actual);
    }

    public static boolean insertSeries(final Series series, long sleepDuration) throws IOException, InterruptedException, JSONException {
        Response response = httpApiResource.path(METHOD_SERIES_INSERT).request().post(Entity.json(Collections.singletonList(series)));
        response.close();
        Thread.sleep(sleepDuration);
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Series looks inserted");
        } else {
            logger.error("Fail to insert series");
        }
        return OK.getStatusCode() == response.getStatus();
    }

    public static List<Series> executeQueryReturnSeries(final SeriesQuery seriesQuery) throws Exception {
        Response response = querySeries(seriesQuery);
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute series query");
        }
        return response.readEntity(new GenericType<List<Series>>() {
        });
    }


    public static JSONArray executeQuery(final SeriesQuery seriesQuery) throws Exception {
        return executeQuery(Collections.singletonList(seriesQuery));
    }

    public static JSONArray executeQuery(final List<SeriesQuery> seriesQueries) throws IOException, JSONException {
        Response response = httpApiResource.path(METHOD_SERIES_QUERY).request().post(Entity.json(seriesQueries));
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Query looks succeeded");
        } else {
            response.close();
            throw new IOException("Failed to execute series query");
        }
        return new JSONArray(response.readEntity(String.class));
    }

    public static String getDataField(int index, String field, JSONArray array) throws JSONException {
        if (array == null) {
            return "returnedSeries is null";
        }
        return ((JSONObject) ((JSONArray) ((JSONObject) array.get(0)).get("data")).get(index)).get(field).toString();
    }

    public static String getField(int index, String field, JSONArray array) throws JSONException {
        if (array == null) {
            return "returnedSeries is null";
        }
        return (((JSONObject) array.get(index)).get(field)).toString();
    }
}
