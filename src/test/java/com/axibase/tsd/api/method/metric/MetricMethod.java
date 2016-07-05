package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.sql.StringTable;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public class MetricMethod extends BaseMethod {
    private static final String METHOD_METRIC_LIST = "/metrics/";
    private static final String METHOD_METRIC = "/metrics/{metric}";
    private static final String METHOD_METRIC_SERIES = "/metrics/{metric}/series";

    public static <T> Response createOrReplaceMetric(String metricName, T query) throws Exception {
        return httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().put(Entity.json(query));
    }

    public static Response createOrReplaceMetric(Metric metric) throws Exception {
        return createOrReplaceMetric(metric.getName(), metric);
    }

    public static <T> Response updateMetric(String metricName, T query) throws Exception {
        return httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().method("PATCH", Entity.json(query));
    }

    public static Response updateMetric(Metric metric) throws Exception {
        return updateMetric(metric.getName(), metric);
    }

    public static Response getMetric(String metricName) throws Exception {
        Response response = httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().get();
        return response;
    }

    public static Response getMetricSeries(String metricName, Map<String, String> parameters) throws Exception {
        MultivaluedMap queryParams = new MultivaluedHashMap(parameters);
        WebTarget target =  httpApiResource.path(METHOD_METRIC_SERIES).resolveTemplate("metric", metricName);
        for(Map.Entry<String, String> entry: parameters.entrySet()) {
            target.queryParam(entry.getKey(), entry.getValue());
        }
        return target.request().get();
    }

//    public static <T> Response getMetricList(T query) throws Exception {
//        return httpApiResource.path(METHOD_METRIC_LIST).request().get(Entity.entity(query));
//    }

    public static Response deleteMetric(String metricName) throws Exception {
        Response response = httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().delete();
        response.close();
        return response;
    }

    public static void createOrReplaceMetricCheck(Metric metric) throws Exception {
        if (createOrReplaceMetric(metric.getName(), jacksonMapper.writeValueAsString(metric)).getStatus() != OK.getStatusCode()) {
            throw new IOException("Can not execute createOrReplace query");
        }
        if (!metricExist(metric)) {
            throw new IOException("Fail to check metric createOrReplace");
        }
    }


    public static boolean metricExist(final Metric metric) throws Exception {
        final Response response = getMetric(metric.getName());
        if(response.getStatus() == NOT_FOUND.getStatusCode()) {
            response.close();
            return false;
        }
        if (response.getStatus() != OK.getStatusCode()) {
            response.close();
            throw new IOException("Fail to execute getMetric query");
        }
        return compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class));
    }

}
