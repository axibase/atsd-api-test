package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.NotCheckedException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public class MetricMethod extends BaseMethod {
    private static final String METHOD_METRIC = "/metrics/{metric}";
    private static final String METHOD_METRIC_SERIES = "/metrics/{metric}/series";

    public static <T> Response createOrReplaceMetric(String metricName, T query) throws Exception {
        Response response = httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().put(Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static Response createOrReplaceMetric(Metric metric) throws Exception {
        return createOrReplaceMetric(metric.getName(), metric);
    }

    public static <T> Response updateMetric(String metricName, T query) throws Exception {
        Response response = httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().method("PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static Response updateMetric(Metric metric) throws Exception {
        return updateMetric(metric.getName(), metric);
    }

    public static Response queryMetric(String metricName) {
        Response response = httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().get();
        response.bufferEntity();
        return response;
    }

    public static Response queryMetricSeries(String metricName) throws Exception {
        WebTarget target = httpApiResource.path(METHOD_METRIC_SERIES).resolveTemplate("metric", metricName);
        Response response = target.request().get();
        response.bufferEntity();
        return response;
    }

    public static Response deleteMetric(String metricName) throws Exception {
        Response response = httpApiResource.path(METHOD_METRIC).resolveTemplate("metric", metricName).request().delete();
        response.bufferEntity();
        return response;
    }

    public static void createOrReplaceMetricCheck(Metric metric, AbstractCheck check) throws Exception {
        if (createOrReplaceMetric(metric.getName(), jacksonMapper.writeValueAsString(metric)).getStatus() != OK.getStatusCode()) {
            throw new Exception("Can not execute createOrReplaceEntityGroup query");
        }
        Checker.check(check);
    }

    public static void createOrReplaceMetricCheck(Metric metric) throws Exception {
        createOrReplaceMetricCheck(metric, new MetricCheck(metric));
    }

    public static boolean metricExist(final Metric metric) throws Exception {
        final Response response = queryMetric(metric.getName());
        if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (response.getStatus() != OK.getStatusCode()) {
            throw new Exception("Fail to execute queryMetric query");
        }
        return compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class));
    }

    public static boolean metricExist(String metric) throws NotCheckedException {
        final Response response = MetricMethod.queryMetric(metric);
        if (response.getStatus() == OK.getStatusCode()) {
            return true;
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (metric.contains(" ")){
            return metricExist(metric.replace(" ", "_"));
        }

        throw new NotCheckedException("Fail to execute metric query");
    }
}
