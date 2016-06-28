package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeriesMethod extends BaseMethod {
    private static final String METHOD_SERIES_INSERT = "/series/insert";
    private static final String METHOD_SERIES_QUERY = "/series/query";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Boolean insertSeries(final Series series, long sleepDuration) throws IOException, InterruptedException, JSONException {
        JSONArray request = new JSONArray() {{
            put(new JSONObject() {{
                put("entity", series.getEntity());
                put("metric", series.getMetric());
                put("data", new JSONArray() {{
                    ArrayList<Sample> data = series.getData();
                    for (final Sample sample : data) {
                        put(new JSONObject() {{
                            put("d", sample.getD());
                            put("v", sample.getV());
                        }});
                    }
                }});
                put("tags", new JSONObject(series.getTags()));
            }});
        }};

        Response response = httpApiResource.path(METHOD_SERIES_INSERT).request().post(Entity.entity(request.toString(), MediaType.APPLICATION_JSON_TYPE));
        response.close();
        Thread.sleep(sleepDuration);
        if (200 == response.getStatus()) {
            logger.debug("Series looks inserted");
        } else {
            logger.error("Fail to insert series");
        }
        return 200 == response.getStatus();
    }

    public static List<Series> executeQueryReturnSeries(final SeriesQuery seriesQuery) throws Exception {
        Response response = httpApiResource.path(METHOD_SERIES_QUERY).request().post(Entity.entity(Collections.singletonList(seriesQuery), MediaType.APPLICATION_JSON_TYPE));
        if (200 == response.getStatus()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute series query");
        }
        return response.readEntity(new GenericType<List<Series>>() {
        });
    }


    public static Boolean insertSeries(final Series series) throws IOException, InterruptedException, JSONException {
        return insertSeries(series, 0);
    }

    public static JSONArray executeQuery(final SeriesQuery seriesQuery) throws Exception {
        return executeQuery(Collections.singletonList(seriesQuery));
    }

    public static JSONArray executeQuery(final List<SeriesQuery> seriesQueries) throws IOException, ParseException, JSONException {
        Response response = httpApiResource.path(METHOD_SERIES_QUERY).request().post(Entity.entity(seriesQueries, MediaType.APPLICATION_JSON_TYPE));
        if (200 == response.getStatus()) {
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
