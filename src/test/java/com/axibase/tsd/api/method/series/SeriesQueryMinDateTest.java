package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryMinDateTest {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String METRIC_NAME = Mocks.metric();
    private static final String SAMPLE_DATE = "2017-12-01T00:00:00.000Z";
    private static final int SAMPLE_VALUE = 1;

    @BeforeClass
    public static void prepareData() throws Exception {
        Series historySeries = new Series(ENTITY_NAME, METRIC_NAME);
        historySeries.setType(SeriesType.HISTORY);
        historySeries.addSamples(Sample.ofDateInteger(SAMPLE_DATE, SAMPLE_VALUE));

        Series forecastSeries = new Series(ENTITY_NAME, METRIC_NAME);
        forecastSeries.setType(SeriesType.FORECAST);
        forecastSeries.addSamples(Sample.ofDateInteger(SAMPLE_DATE, SAMPLE_VALUE)
                .setDeviation(BigDecimal.ONE));

        SeriesMethod.insertSeriesCheck(historySeries, forecastSeries);
    }

    @DataProvider
    Object[][] seriesTypeProvider() {
        return new Object[][]{{SeriesType.HISTORY}, {SeriesType.FORECAST}, {SeriesType.FORECAST_DEVIATION}};
    }

    @Issue("4756")
    @Test(
            dataProvider = "seriesTypeProvider",
            description = "Test that series query correctly works with minimal ISO date for all of the series types"
    )
    public void minStartDateTest(SeriesType type) {
        SeriesQuery seriesQuery = new SeriesQuery(ENTITY_NAME, METRIC_NAME, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        seriesQuery.setType(type);

        List<Series> seriesList = SeriesMethod.querySeriesAsList(seriesQuery);
        assertEquals("Response to series query contains more than one element or empty", 1, seriesList.size());

        List<Sample> samples = seriesList.get(0).getData();
        assertEquals("Responsed series contains more than one sample or empty", 1, seriesList.size());

        Sample sample = samples.get(0);
        assertEquals("Responsed sample date is not the same as inserted", sample.getRawDate(), SAMPLE_DATE);
        assertTrue("Responsed sample value is not the same as inserted",
                sample.getValue().compareTo(BigDecimal.valueOf(SAMPLE_VALUE)) == 0);
    }
}
