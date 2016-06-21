package com.axibase.tsd.api.method.metrics;

import com.axibase.tsd.api.method.Method;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.transport.http.AtsdHttpResponse;
import com.axibase.tsd.api.transport.http.HTTPMethod;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;

public class MetricMethod extends Method {
    protected static final String METHOD_METRICS = "/metrics/";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private JSONObject returnedMetric;
    private JSONParser jsonParser = new JSONParser();

    public Boolean createOrReplaceMetric(Metric metric) throws Exception {
        JSONObject request = (JSONObject) jsonParser.parse(jacksonMapper.writeValueAsString(metric));

        AtsdHttpResponse response = httpSender.send(HTTPMethod.PUT, METHOD_METRICS + URLEncoder.encode(metric.getName(), "UTF-8"), request.toJSONString());
        if (200 == response.getCode()) {
            logger.debug("Metric looks created or replaced");
        } else {
            logger.error("Fail to create or replace metric");
        }
        return 200 == response.getCode();

    }

    protected Boolean getMetric(String metric) throws Exception {
        AtsdHttpResponse response = httpSender.send(HTTPMethod.GET, METHOD_METRICS + URLEncoder.encode(metric, "UTF-8"), null);
        if (200 == response.getCode()) {
            logger.debug("Metric looks deleted");
        } else {
            logger.error("Fail to delete metric");
        }
        returnedMetric = (JSONObject) jsonParser.parse(response.getBody());

        return 200 == response.getCode();
    }

    public Boolean metricExists(Metric metric) throws Exception {
        return getMetric(metric);
    }

    protected Boolean getMetric(Metric metric) throws Exception {
        return getMetric(metric.getName());
    }

    public Boolean deleteMetric(String metric) throws IOException {
        AtsdHttpResponse response = httpSender.send(HTTPMethod.DELETE, METHOD_METRICS + URLEncoder.encode(metric, "UTF-8"), null);
        if (200 == response.getCode()) {
            logger.debug("Metric looks deleted");
        } else {
            logger.error("Fail to delete metric");
        }
        return 200 == response.getCode();
    }

    public Boolean deleteMetric(Metric metric) throws IOException {
        return deleteMetric(metric.getName());
    }



    protected String getMetricField(String field) {
        if (returnedMetric == null) {
            return "";
        }
        return returnedMetric.get(field).toString();
    }
}
