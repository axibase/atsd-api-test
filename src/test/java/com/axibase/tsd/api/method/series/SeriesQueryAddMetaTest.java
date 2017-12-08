package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryAddMetaTest extends SeriesMethod {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String METRIC_NAME = Mocks.metric();
    private static final String EMPTY_ENTITY_NAME = Mocks.entity();
    private static final String EMPTY_METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series historySeries = new Series(ENTITY_NAME, METRIC_NAME)
                .setType(SeriesType.HISTORY);
        historySeries.addSamples(Sample.ofDateInteger("2017-11-23T10:00:00.000Z", 1));

        Series forecastSeries = new Series(ENTITY_NAME, METRIC_NAME)
                .setType(SeriesType.FORECAST);
        forecastSeries.addSamples(Sample.ofDateInteger("2017-11-23T10:00:00.000Z", 2));

        MetricMethod.createOrReplaceMetricCheck(new Metric(EMPTY_METRIC_NAME));
        EntityMethod.createOrReplaceEntityCheck(new Entity(EMPTY_ENTITY_NAME));

        insertSeriesCheck(historySeries, forecastSeries);
    }

    @DataProvider
    Object[][] seriesTypeProvider() {
        Object[][] result = new Object[SeriesType.values().length][];
        for (int i = 0; i < SeriesType.values().length; i++) {
            result[i] = new Object[]{SeriesType.values()[i]};
        }
        return result;
    }

    @Issue("4713")
    @Test(
            description = "Check that meta is included for all types of data",
            dataProvider = "seriesTypeProvider"
    )
    public void testSeriesResponseMetaIncluded(SeriesType type) {
        checkMeta(ENTITY_NAME, METRIC_NAME, type);
    }

    @Issue("4713")
    @Test(
            description = "Check that meta is included for all types of data, when no data samples were found",
            dataProvider = "seriesTypeProvider"
    )
    public void testSeriesResponseMetaIncludedForEmptyData(SeriesType type) {
        checkMeta(EMPTY_ENTITY_NAME, EMPTY_METRIC_NAME, type);
    }

    private void checkMeta(String entity, String metric, SeriesType type) {
        SeriesQuery seriesQuery =
                new SeriesQuery(entity, metric, Util.ISOFormat(1), MAX_QUERYABLE_DATE)
                        .setAddMeta(true)
                        .setType(type);

        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals(String.format("Response for series query of type %s has inappropriate length", type),
                1, seriesList.size());

        SeriesMeta meta = seriesList.get(0).getMeta();
        assertTrue(String.format("Response for series query of type %s doesn't contain meta", type),
                meta != null);

        assertTrue(String.format("Response for series query of type %s doesn't contain entity meta", type),
                meta.getEntity() != null);
        assertTrue(String.format("Response for series query of type %s doesn't contain metric meta", type),
                meta.getMetric() != null);
    }
}
