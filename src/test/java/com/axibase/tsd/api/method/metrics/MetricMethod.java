package com.axibase.tsd.api.method.metrics;

import com.axibase.tsd.api.method.Method;
import com.axibase.tsd.api.model.metric.Metric;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class MetricMethod extends Method {
    protected static final String METHOD_METRICS = "/metrics/";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private JSONObject returnedMetric;
    private JSONParser jsonParser = new JSONParser();

    public Boolean createOrReplaceMetric(Metric metric) throws Exception {
        JSONObject request = (JSONObject) jsonParser.parse(jacksonMapper.writeValueAsString(metric));
        Response response = httpResource.path(METHOD_METRICS).path("{metric}").resolveTemplate("metric", metric.getName()).request().put(Entity.entity(request.toJSONString(), MediaType.APPLICATION_JSON_TYPE));
        if (200 == response.getStatus()) {
            logger.debug("Metric looks created or replaced");
        } else {
            logger.error("Fail to create or replace metric");
        }
        return 200 == response.getStatus();

    }

    protected Boolean getMetric(String metric) throws Exception {
        Response response = httpResource.path(METHOD_METRICS).path("{metric}").resolveTemplate("metric", metric).request().get();
        if (200 == response.getStatus()) {
            logger.debug("Metric looks deleted");
        } else {
            logger.error("Fail to delete metric");
        }
        returnedMetric = (JSONObject) jsonParser.parse(response.readEntity(String.class));

        return 200 == response.getStatus();
    }

    protected Boolean getMetric(Metric metric) throws Exception {
        return getMetric(metric.getName());
    }

    protected Boolean deleteMetric(String metric) throws IOException {
        Response response = httpResource.path(METHOD_METRICS).path("{metric}").resolveTemplate("metric", metric).request().delete();
        if (200 == response.getStatus()) {
            logger.debug("Metric looks deleted");
        } else {
            logger.error("Fail to delete metric");
        }
        return 200 == response.getStatus();
    }

    protected Boolean deleteMetric(Metric metric) throws IOException {
        return deleteMetric(metric.getName());
    }

    protected String getMetricField(String field) {
        if (returnedMetric == null) {
            return "";
        }
        return returnedMetric.get(field).toString();
    }
}
