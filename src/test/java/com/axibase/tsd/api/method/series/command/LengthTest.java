package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.testng.AssertJUnit.assertTrue;

public class LengthTest extends SeriesMethod {

    /**
     * #2412
     */
    @Test
    public void testMaxLength() throws Exception {
        final int MAX_LENGTH = 128 * 1024;
        final int METRICS_COUNT = 10;
        final int METRIC_PREFIX_LENGTH = 13097 - 2;

        String entityName = "e-series-max-len";
        String metricPrefix = Util.appendChar(new StringBuilder(), 'm', METRIC_PREFIX_LENGTH).append("-").toString();
        String date = "2016-05-21T00:00:00.000Z";
        String endDate = "2016-05-21T00:00:00.001Z";

        StringBuilder sb = new StringBuilder("series");
        sb.append(" e:").append(entityName);
        sb.append(" d:").append(date);
        for (int i = 0; i < METRICS_COUNT; i++) {
            sb.append(" m:").append(metricPrefix).append(i).append("=").append(i);
        }
        if (MAX_LENGTH != sb.length()) {
            Assert.fail("Command length is not maximal");
        }
        tcpSender.send(sb.toString(), DEFAULT_EXPECTED_PROCESSING_TIME);

        ArrayList<SeriesQuery> seriesQueries = new ArrayList<>();
        final ArrayList<Series> seriesList = new ArrayList<>();
        for (int i = 0; i < METRICS_COUNT; i++) {
            Series series = new Series(null, metricPrefix + i);
            series.setEntity(entityName);
            series.setData(Collections.singletonList(new Sample(date, i)));
            seriesList.add(series);
            seriesQueries.add(new SeriesQuery(entityName, metricPrefix + i, date, endDate));
        }

        JSONArray storedSeriesList = executeQuery(seriesQueries);

        String storedSeries = storedSeriesList.toString();
        String sentSeries = jacksonMapper.writeValueAsString(seriesList);

        assertTrue("Returned series do not match to inserted", compareJsonString(sentSeries, storedSeries));
    }

    /**
     * #2412
     */
    @Test
    public void testMaxLengthOverflow() throws Exception {
        final int MAX_LENGTH = 128 * 1024;
        final int METRICS_COUNT = 10;
        final int METRIC_PREFIX_LENGTH = 13097 - 2;

        String entityName = "e-series-max-over";
        String metricPrefix = Util.appendChar(new StringBuilder(), 'm', METRIC_PREFIX_LENGTH).append("-").toString();
        String date = "2016-05-21T00:00:00.000Z";
        String endDate = "2016-05-21T00:00:00.001Z";

        StringBuilder sb = new StringBuilder("series");
        sb.append(" e:").append(entityName);
        sb.append(" d:").append(date);
        for (int i = 0; i < METRICS_COUNT; i++) {
            sb.append(" m:").append(metricPrefix).append(i).append("=").append(i);
        }
        if (MAX_LENGTH + 1 != sb.length()) {
            Assert.fail("Command length is not equals to max + 1");
        }
        tcpSender.send(sb.toString(), DEFAULT_EXPECTED_PROCESSING_TIME);

        ArrayList<SeriesQuery> seriesQueries = new ArrayList<>();
        for (int i = 0; i < METRICS_COUNT; i++) {
            seriesQueries.add(new SeriesQuery(entityName, metricPrefix + i, date, endDate));
        }
        JSONArray storedSeriesList = executeQuery(seriesQueries);

        for (int i = 0; i < METRICS_COUNT; i++) {
            Assert.assertEquals("[]", Util.extractJSONObjectFieldFromJSONArrayByIndex(i, "data", storedSeriesList), "Managed to insert command that length is max + 1");
        }
    }


}
