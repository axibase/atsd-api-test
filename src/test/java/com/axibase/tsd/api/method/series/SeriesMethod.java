package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.Method;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.transport.http.AtsdHttpResponse;
import com.axibase.tsd.api.transport.http.HTTPMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Map;

public class SeriesMethod extends Method {
    protected static final String METHOD_SERIES_INSERT = "/series/insert";
    protected static final String METHOD_SERIES_QUERY = "/series/query";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private JSONArray returnedSeries;
    private JSONParser jsonParser = new JSONParser();

    public Boolean insertSeries(final Series series, long sleepDuration) throws IOException, InterruptedException {
        JSONArray request = new JSONArray() {{
            add(new JSONObject() {{
                put("entity", series.getEntity());
                put("metric", series.getMetric());
                put("data", new JSONArray() {{
                    ArrayList<Sample> data = series.getData();
                    for (final Sample sample : data) {
                        add(new JSONObject() {{
                            put("d", sample.getD());
                            put("v", sample.getV());
                        }});
                    }
                }});
                put("tags", new JSONObject(series.getTags()));
            }});
        }};


        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_SERIES_INSERT, request.toJSONString());
        Thread.sleep(sleepDuration);
        if (200 == response.getCode()) {
            logger.debug("Series looks inserted");
        } else {
            logger.error("Fail to insert series");
        }
        return 200 == response.getCode();
    }

    protected Boolean insertSeries(final Series series) throws IOException, InterruptedException {
        return insertSeries(series, 0);
    }

    protected Boolean executeQuery(final SeriesQuery seriesQuery) throws Exception {
        JSONArray request = new JSONArray() {{
            add(queryToJSONObject(seriesQuery));
        }};

        String body = request.toJSONString();
        final AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_SERIES_QUERY, body);
        if (200 == response.getCode()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute series query");
        }
        returnedSeries = (JSONArray) jsonParser.parse(response.getBody());
        return 200 == response.getCode();
    }

    protected Boolean executeQuery(final ArrayList<SeriesQuery> seriesQueries) throws IOException, ParseException {
        JSONArray request = new JSONArray();
        for (SeriesQuery seriesQuery : seriesQueries) {
            request.add(queryToJSONObject(seriesQuery));
        }
        final AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_SERIES_QUERY, request.toJSONString());
        if (200 == response.getCode()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute series query");
        }
        returnedSeries = (JSONArray) jsonParser.parse(response.getBody());
        return 200 == response.getCode();

    }

    private JSONObject queryToJSONObject(final SeriesQuery seriesQuery) {
        return new JSONObject() {
            {
                put("entity", seriesQuery.getEntity());
                put("metric", seriesQuery.getMetric());
                put("startDate", seriesQuery.getStartDate());
                put("endDate", seriesQuery.getEndDate());
                put("tags", seriesQuery.getTags());
                final Map aggregatePeriod = ((Map) seriesQuery.getAggregatePeriod());
                final ArrayList<String> aggregateTypes = ((ArrayList<String>) seriesQuery.getAggregateTypes());
                if (aggregatePeriod != null && aggregateTypes != null) {
                    put("aggregate", new JSONObject() {{
                        {
                            put("types", new JSONArray() {{
                                for (String aggregateType : aggregateTypes) {
                                    add(aggregateType);
                                }
                            }});
                            put("period", new JSONObject(aggregatePeriod));
                        }
                    }});
                }
            }
        };
    }

    protected String getDataField(int index, String field) {
        if (returnedSeries == null) {
            return "returnedSeries is null";
        }
        return ((JSONObject) ((JSONArray) ((JSONObject) returnedSeries.get(0)).get("data")).get(index)).get(field).toString();
    }

    protected String getField(int index, String field) {
        if (returnedSeries == null) {
            return "returnedSeries is null";
        }
        return (((JSONObject) returnedSeries.get(index)).get(field)).toString();
    }

    public JSONArray getReturnedSeries() {
        return returnedSeries;
    }
}
