package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.version.Version;

import java.util.Collections;

public class AlertTest extends AlertMethod {
    public static final String RULE_METRIC_NAME = "test_alert_metric_1";

    static {
        Registry.Metric.register(RULE_METRIC_NAME);
    }

    public static void generateAlertForEntity(final String entityName) throws Exception {
        Series series = new Series();
        series.setEntity(entityName);
        series.setMetric(RULE_METRIC_NAME);
        String date = VersionMethod.queryVersion().readEntity(Version.class).getDate().getCurrentDate();
        series.addData(new Sample(Util.parseDate(date), BaseMethod.ALERT_OPEN_VALUE));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }
}
