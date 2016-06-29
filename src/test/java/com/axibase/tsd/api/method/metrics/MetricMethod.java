package com.axibase.tsd.api.method.metrics;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.method.BaseMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngineResult;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

public class MetricMethod extends BaseMethod {
    protected static final String METHOD_METRICS = "/metrics/";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Response createOrReplaceMetric(Metric metric) throws Exception {
        return httpApiResource.path(METHOD_METRICS).path(metric.getName()).request().put(Entity.entity(metric, MediaType.APPLICATION_JSON_TYPE));
    }

    public static Response getMetric(String metricName) throws Exception {
        return httpApiResource.path(METHOD_METRICS).path(metricName).request().get();
    }

    public static void createOrReplaceMetricCheck(Metric metric) throws Exception {
        if(httpApiResource.path(METHOD_METRICS).path(metric.getName()).request().put(Entity.entity(metric, MediaType.APPLICATION_JSON_TYPE)).getStatus() != OK.getStatusCode()) {
            throw new IOException("Can not execute createOrReplace query");
        }
        if(!metricExist(metric)) {
            throw new IOException("Fail to check metric createOrReplace");
        }
    }


    public static boolean metricExist(final Metric metric) throws Exception {
        Response response = getMetric(metric.getName());
        if(response.getStatus() != OK.getStatusCode()) {
            throw new IOException("Fail to execute getMetric query");
        }
        return compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class));
    }



    public static void deleteMetric(String metricName) throws IOException {
        Response response = httpApiResource.path(METHOD_METRICS).path(metricName).request().delete();
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Metric looks deleted");
        } else {
            throw new IOException("Fail to execute deleteMetric query");
        }
    }
}
