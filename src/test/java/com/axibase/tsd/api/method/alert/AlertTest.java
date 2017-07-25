package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.version.Version;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.ALERT_OPEN_VALUE;

public class AlertTest extends AlertMethod {
    public static final String RULE_METRIC_NAME = "test_alert_metric_1";

    static {
        Registry.Metric.checkExists(RULE_METRIC_NAME);
    }

    public static void generateAlertForEntity(final String entityName) throws Exception {
        Series series = new Series();
        series.setEntity(entityName);
        series.setMetric(RULE_METRIC_NAME);
        String date = VersionMethod.queryVersion().readEntity(Version.class).getDate().getCurrentDate();
        series.addSamples(new Sample(TestUtil.parseDate(date), ALERT_OPEN_VALUE));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }
}
