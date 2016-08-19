package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Dmitry Korchagin.
 */
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


    public static <T> Response deletAlerts(T... queries) {
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
        series.setMetric(Util.RULE_METRIC_NAME);
        series.addData(new Sample(Util.ISOFormat(new Date()), Util.ALERT_OPEN_VALUE));
        SeriesMethod.insertSeriesCheck(series);
    }


    public static void generateAlertHistoryForEntity(final String entityName) throws Exception {
        Series series = new Series();
        series.setEntity(entityName);
        series.setMetric(Util.RULE_METRIC_NAME);
        series.addData(new Sample(Util.ISOFormat(new Date()), Util.ALERT_OPEN_VALUE));
        SeriesMethod.insertSeriesCheck(series);

        series.setData(null);
        series.addData(new Sample(Util.ISOFormat(new Date()), Util.ALERT_CLOSE_VALUE));
        SeriesMethod.insertSeriesCheck(series);
    }


}
