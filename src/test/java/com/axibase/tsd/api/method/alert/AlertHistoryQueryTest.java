package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
public class AlertHistoryQueryTest extends AlertMethod {

    /**
     * #2991
     */
    @Test
    public void testEntityExpression() throws Exception {
        final String metricName = Util.RULE_METRIC_NAME;
        final String entityName = "alert-historyquery-entity-1";
        Registry.Entity.register(entityName);
        Series series = new Series();
        series.setEntity(entityName);
        series.setMetric(metricName);
        series.addData(new Sample(Util.ISOFormat(new Date()), Util.ALERT_OPEN_VALUE));
        SeriesMethod.insertSeriesCheck(series);

        series.setData(null);
        series.addData(new Sample(Util.ISOFormat(new Date()), Util.ALERT_CLOSE_VALUE));
        SeriesMethod.insertSeriesCheck(series);


        Map<String, String> query = new HashMap<>();
        query.put("entity", "alert-historyquery-entity*");
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get alerts by entity expression");
    }
}
