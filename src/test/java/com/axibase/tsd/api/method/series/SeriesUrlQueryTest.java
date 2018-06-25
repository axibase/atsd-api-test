package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_STORABLE_DATE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;

public class SeriesUrlQueryTest extends SeriesMethod {

    @Issue("1278")
    @Test
    public void testEntityContainsWhitespace() throws Exception {
        final String entityName = "seriesurlquery entityname-1";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("startDate", MIN_QUERYABLE_DATE);
        parameters.put("endDate", MAX_QUERYABLE_DATE);
        Response response = urlQuerySeries(entityName, "metricname", parameters);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("1278")
    @Test
    public void testMetricContainsWhitespace() {
        final String metricName = "seriesurlquery metricname-2";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("startDate", MIN_QUERYABLE_DATE);
        parameters.put("endDate", MAX_QUERYABLE_DATE);
        Response response = urlQuerySeries("entityname", metricName, parameters);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("1278")
    @Test
    public void testEntityContainsSlash() throws Exception {
        Series series = new Series("seriesurlquery/entityname-3", "seriesurlquery-metric-3");
        assertUrlEncodePathHandledCorrectly(series);

    }

    @Issue("1278")
    @Test
    public void testMetricContainsSlash() throws Exception {
        Series series = new Series("seriesurlquery-entityname-4", "seriesurlquery/metric-4");
        assertUrlEncodePathHandledCorrectly(series);
    }

    @Issue("1278")
    @Test
    public void testEntityContainsCyrillic() throws Exception {
        Series series = new Series("seriesurlqueryйёentityname-5", "seriesurlquery-metric-5");
        assertUrlEncodePathHandledCorrectly(series);
    }

    @Issue("1278")
    @Test
    public void testMetricContainsCyrillic() throws Exception {
        Series series = new Series("seriesurlquery-entityname-6", "seriesurlqueryйёmetric-6");
        assertUrlEncodePathHandledCorrectly(series);
    }

    private void assertUrlEncodePathHandledCorrectly(Series series) throws Exception {
        series.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 0));
        insertSeriesCheck(Collections.singletonList(series));
        Map<String, String> parameters = new HashMap<>();
        parameters.put("startDate", MIN_QUERYABLE_DATE);
        parameters.put("endDate", MAX_QUERYABLE_DATE);

        Response response = urlQuerySeries(series.getEntity(), series.getMetric(), parameters);
        assertEquals(OK.getStatusCode(), response.getStatus());
        List<Series> responseSeries = response.readEntity(new GenericType<List<Series>>() {
        });
        assertEquals(1, responseSeries.size());
        final Series actualSeries = responseSeries.get(0);
        assertEquals("Incorrect series entity", series.getEntity(), actualSeries.getEntity());
        assertEquals("Incorrect series metric", series.getMetric(), actualSeries.getMetric());
        final List<Sample> samples = actualSeries.getData();
        assertEquals("Series should contains only one sample", 1, samples.size());
        final Sample sample = samples.get(0);
        assertEquals("Incorrect series sample date", 0L, sample.getUnixTime().longValue());
        assertEquals("Incorrect series sample value", 0, new BigDecimal(0).compareTo(sample.getValue()));
    }
}
