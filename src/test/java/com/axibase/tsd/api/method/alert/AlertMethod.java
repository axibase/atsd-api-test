package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;

public class AlertMethod extends BaseMethod {
    private static final String METHOD_ALERTS_QUERY = "/alerts/query";
    private static final String METHOD_ALERTS_UPDATE = "/alerts/update";
    private static final String METHOD_ALERTS_DELETE = "/alerts/delete";
    private static final String METHOD_ALERTS_HISTORY_QUERY = "/alerts/history/query";

    public static <T> Response queryAlerts(T... queries) {
        Response response = httpApiResource
                .path(METHOD_ALERTS_QUERY)
                .request()
                .post(Entity.json(Arrays.asList(queries)));
        response.bufferEntity();
        return response;
    }

    public static <T> Response updateAlerts(T... queries) {
        Response response = httpApiResource
                .path(METHOD_ALERTS_UPDATE)
                .request()
                .post(Entity.json(Arrays.asList(queries)));
        response.bufferEntity();
        return response;
    }

    public static boolean alertExist(String entityName, String metricName) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("entity", entityName);
        query.put("metrics", Collections.singletonList(metricName));
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);

        Alert alert = new Alert();
        alert.setEntity(entityName);
        alert.setMetric(metricName);
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(alert));
        final String given = queryAlerts(query).readEntity(String.class);
        return compareJsonString(expected, given);
    }


    public static <T> Response deleteAlerts(T... queries) {
        Response response = httpApiResource
                .path(METHOD_ALERTS_DELETE)
                .request()
                .post(Entity.json(Arrays.asList(queries)));
        response.bufferEntity();
        return response;
    }

    public static <T> Response queryAlertsHistory(T... queries) {
        Response response = httpApiResource
                .path(METHOD_ALERTS_HISTORY_QUERY)
                .request()
                .post(Entity.json(Arrays.asList(queries)));
        response.bufferEntity();
        return response;
    }

    public static void generateAlertForEntity(final String entityName) throws Exception {
        Series series = new Series();
        series.setEntity(entityName);
        series.setMetric(BaseMethod.RULE_METRIC_NAME);
        series.addData(new Sample(Util.ISOFormat(new Date()), BaseMethod.ALERT_OPEN_VALUE));
        SeriesMethod.insertSeriesCheck(series);
    }


    public static void generateAlertHistoryForEntity(final String entityName) throws Exception {
        Series series = new Series();
        series.setEntity(entityName);
        series.setMetric(BaseMethod.RULE_METRIC_NAME);
        series.addData(new Sample(Util.ISOFormat(new Date()), BaseMethod.ALERT_OPEN_VALUE));
        SeriesMethod.insertSeriesCheck(series);


        series.setData(null);
        series.addData(new Sample(Util.ISOFormat(new Date()), BaseMethod.ALERT_CLOSE_VALUE));
        SeriesMethod.insertSeriesCheck(series);

        Long startTime = System.currentTimeMillis();
        boolean alertAbsent = true;
        while(alertExist(entityName, BaseMethod.RULE_METRIC_NAME)) {
            Thread.sleep(BaseMethod.REQUEST_INTERVAL);
            if(System.currentTimeMillis() > (startTime + BaseMethod.EXPECTED_PROCESSING_TIME)) {
                alertAbsent = false;
                break;
            }
        }
        if(!alertAbsent && alertExist(entityName, BaseMethod.RULE_METRIC_NAME)) {
            throw new IllegalArgumentException("Fail to generate AlertHistory Element");
        }
    }


}
