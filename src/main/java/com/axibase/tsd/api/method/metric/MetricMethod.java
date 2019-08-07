package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.MethodParameters;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.metric.MetricSeriesTags;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class MetricMethod extends BaseMethod {
    private static final String METHOD_METRIC = "/metrics/{metric}";
    private static final String METHOD_METRIC_SERIES = "/metrics/{metric}/series";
    private static final String METHOD_METRIC_SERIES_TAGS = "/metrics/{metric}/series/tags";
    private static final String METHOD_METRIC_RENAME = "/metrics/{metric}/rename";

    private static Map<String, Object> nameReplacement(String metricName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("metric", metricName);
        return Collections.unmodifiableMap(map);
    }

    public static <T> Response createOrReplaceMetric(String metricName, T query, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_METRIC, nameReplacement(metricName), HttpMethod.PUT, Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response createOrReplaceMetric(String metricName, T query) {
        return createOrReplaceMetric(metricName, query, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response createOrReplaceMetric(Metric metric) {
        return createOrReplaceMetric(metric.getName(), metric);
    }

    public static Response createOrReplaceMetric(Metric metric, String token) {
        return createOrReplaceMetric(metric.getName(), metric, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response updateMetric(String metricName, T query, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_METRIC, nameReplacement(metricName), "PATCH", Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response updateMetric(String metricName, T query) {
        return updateMetric(metricName, query, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response updateMetric(Metric metric) {
        return updateMetric(metric.getName(), metric);
    }

    public static Response updateMetric(Metric metric, String token) {
        return updateMetric(metric.getName(), metric, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response queryMetric(String metricName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_METRIC, nameReplacement(metricName), HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response queryMetric(String metricName) {
        return queryMetric(metricName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response queryMetric(String metricName, String token) {
        return queryMetric(metricName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Metric getMetric(String entityName) {
        Response response = queryMetric(entityName);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            String error;
            try {
                error = extractErrorMessage(response);
            } catch (Exception e) {
                error = response.readEntity(String.class);
            }
            throw new IllegalStateException(String.format("Failed to get metric! Reason: %s", error));
        }
        return response.readEntity(Metric.class);
    }

    public static Response queryMetricSeries(String metricName, String token) {
        return queryMetricSeries(metricName, null, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response queryMetricSeries(String metricName) {
        return queryMetricSeries(metricName, (MetricSeriesParameters) null);
    }

    public static Response queryMetricSeries(String metricName,
                                             MetricSeriesParameters parameters) {
        return queryMetricSeries(metricName, parameters, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response queryMetricSeries(String metricName, MetricSeriesParameters parameters, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_METRIC_SERIES, nameReplacement(metricName),
                parameters == null ? Collections.EMPTY_MAP : parameters.toUnmodifiableMap(), Collections.EMPTY_MAP, HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response deleteMetric(String metricName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_METRIC, nameReplacement(metricName), HttpMethod.DELETE);
        response.bufferEntity();
        return response;
    }

    public static Response deleteMetric(String metricName) {
        return deleteMetric(metricName, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response deleteMetric(String metricName, String token) {
        return deleteMetric(metricName, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response queryMetricSeriesTagsResponse(String metricName, MethodParameters parameters, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_METRIC_SERIES_TAGS, nameReplacement(metricName),
                parameters == null ? Collections.EMPTY_MAP : parameters.toUnmodifiableMap(), Collections.EMPTY_MAP, HttpMethod.GET);
        response.bufferEntity();
        return response;
    }

    public static Response queryMetricSeriesTagsResponse(String metricName,
                                                         MethodParameters parameters) {
        return queryMetricSeriesTagsResponse(metricName, parameters, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response queryMetricSeriesTagsResponse(String metricName, MethodParameters parameters, String token) {
        return queryMetricSeriesTagsResponse(metricName, parameters, new RequestSenderWithBearerAuthorization(token));
    }

    public static MetricSeriesTags queryMetricSeriesTags(final String metricName,
                                                         final MethodParameters parameters) {
        return queryMetricSeriesTagsResponse(metricName, parameters)
                .readEntity(MetricSeriesTags.class);
    }

    public static Response renameMetric(String oldName, String newName, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_METRIC_RENAME, nameReplacement(oldName), HttpMethod.POST, Entity.json(new Metric(newName)));
        response.bufferEntity();
        return response;
    }

    public static Response renameMetric(String oldName, String newName, String token) {
        return renameMetric(oldName, newName, new RequestSenderWithBearerAuthorization(token));
    }

    public static void createOrReplaceMetricCheck(Metric metric, AbstractCheck check) throws Exception {
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(createOrReplaceMetric(metric.getName(), jacksonMapper.writeValueAsString(metric)))) {
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
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new Exception("Fail to execute metric query: " + responseAsString(response));
        }
        return compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class));
    }

    public static boolean metricExist(String metric) throws NotCheckedException {
        final Response response = MetricMethod.queryMetric(metric);
        if (Response.Status.Family.SUCCESSFUL == Util.responseFamily(response)) {
            return true;
        } else if (response.getStatus() == NOT_FOUND.getStatusCode()) {
            return false;
        }
        if (metric.contains(" ")) {
            return metricExist(metric.replace(" ", "_"));
        }

        throw new NotCheckedException("Fail to execute metric query: " + responseAsString(response));
    }
}
