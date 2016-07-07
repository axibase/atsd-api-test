package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Korchagin.
 */
public class EntityGetMetricsTest extends EntityMethod {


    /* #1278 */
    @Test
    public void testEntityNameContainsWhitespace() throws Exception {
        Entity entity = new Entity("getmetricsentity 1");
        assertEquals(BAD_REQUEST.getStatusCode(), queryEntityMetrics(entity.getName()).getStatus());
    }


    /* #1278 */
    @Test
    public void testEntityNameContainsSlash() throws Exception {
        final Series series = new Series("getmetrics/entity2", "getmetrics-metric2");
        series.addData(new Sample("1970-01-01T00:00:00.000Z", "1"));
        SeriesMethod.insertSeriesCheck(series, Util.EXPECTED_PROCESSING_TIME);

        assertUrlencodedPathHandledSuccessfullyOnGetMetrics(series);
    }

    /* #1278 */
    @Test
    public void testEntityNameContainsCyrillic() throws Exception {
        final Series series = new Series("getmetricsйё/entity3", "getmetrics-metric3");
        series.addData(new Sample("1970-01-01T00:00:00.000Z", "1"));
        SeriesMethod.insertSeriesCheck(series, Util.EXPECTED_PROCESSING_TIME);

        assertUrlencodedPathHandledSuccessfullyOnGetMetrics(series);
    }

    private void assertUrlencodedPathHandledSuccessfullyOnGetMetrics(final Series series) throws Exception {
        Response response = queryEntityMetrics(series.getEntity());
        assertEquals(OK.getStatusCode(), response.getStatus());
        List<Metric> metricList = response.readEntity(new GenericType<List<Metric>>() {
        });
        assertEquals(1, metricList.size());
        assertEquals(series.getMetric(), metricList.get(0).getName());
    }
}
