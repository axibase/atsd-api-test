package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryMultipleYearsGroupTest extends SeriesMethod {
    private static final String ENTITY_NAME1 = entity();
    private static final String ENTITY_NAME2 = entity();
    private static final String METRIC_NAME = metric();
    private static long zeroTimeOffset;

    @BeforeClass
    public static void prepareData() throws Exception {
        TimeZone serverTimeZone = Util.getServerTimeZone();
        zeroTimeOffset = serverTimeZone.getOffset(0);

        Series series1 = new Series(ENTITY_NAME1, METRIC_NAME);
        series1.addSamples(
                Sample.ofDateInteger("1970-01-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2015-06-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2017-06-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2018-08-01T12:00:00.000Z", 0)
        );

        Series series2 = new Series(ENTITY_NAME2, METRIC_NAME);
        series2.addSamples(
                Sample.ofDateInteger("2012-06-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2016-06-01T12:00:00.000Z", 0)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Issue("4101")
    @Issue("4591")
    @Test
    public void testSeriesQueryMultipleYearGroupBothEntities() throws Exception {
        SeriesQuery query = new SeriesQuery();
        query.setEntities(Arrays.asList(ENTITY_NAME1, ENTITY_NAME2));
        query.setMetric(METRIC_NAME);
        query.setStartDate("1900-01-01T00:00:00.000Z");
        query.setEndDate("2100-01-01T00:00:00.000Z");

        query.setAggregate(new Aggregate(AggregationType.COUNT, new Period(12, TimeUnit.YEAR)));

        List<Series> resultSeries = querySeriesAsList(query);

        List<Sample> samples1 = new ArrayList<>();
        /* See #4101#note-18 */
        if (zeroTimeOffset <= 0) {
            samples1.add(Sample.ofDateInteger("1970-01-01T00:00:00.000Z", 1));
        }
        samples1.add(Sample.ofDateInteger("2006-01-01T00:00:00.000Z", 2));
        samples1.add(Sample.ofDateInteger("2018-01-01T00:00:00.000Z", 1));

        List<Sample> samples2 = new ArrayList<>();
        samples2.add(Sample.ofDateInteger("2006-01-01T00:00:00.000Z", 2));

        assertSamples(samples1, resultSeries.get(0).getData());
        assertSamples(samples2, resultSeries.get(1).getData());
    }

    @Issue("4101")
    @Issue("4591")
    @Test
    public void testSeriesQueryMultipleYearGroupSingleEntity() throws Exception {
        SeriesQuery query = new SeriesQuery();
        query.setEntities(Collections.singletonList(ENTITY_NAME2));
        query.setMetric(METRIC_NAME);
        query.setStartDate("1900-01-01T00:00:00.000Z");
        query.setEndDate("2100-01-01T00:00:00.000Z");

        query.setAggregate(new Aggregate(AggregationType.COUNT, new Period(12, TimeUnit.YEAR)));

        List<Series> resultSeries = querySeriesAsList(query);

        List<Sample> samples = new ArrayList<>();

        samples.add(Sample.ofDateInteger("2006-01-01T00:00:00.000Z", 2));

        assertSamples(samples, resultSeries.get(0).getData());
    }

    private void assertSamples(List<Sample> expectedSamples, List<Sample> actualSamples) throws Exception {
        List<Sample> translatedSamples = new ArrayList<>();
        for (Sample s : expectedSamples) {
            String translatedDate = TestUtil.timeTranslateDefault(s.getRawDate(),
                    TestUtil.TimeTranslation.LOCAL_TO_UNIVERSAL);
            translatedSamples.add(Sample.ofDateInteger(translatedDate, s.getValue().intValue()));
        }

        final String actual = jacksonMapper.writeValueAsString(actualSamples);
        final String expected = jacksonMapper.writeValueAsString(translatedSamples);
        assertTrue("Grouped series do not match with expected", compareJsonString(expected, actual));
    }

}
