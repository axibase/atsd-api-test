package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryMultipleYearsGroupTest extends SeriesMethod {
    private static final String ENTITY_NAME1 = entity();
    private static final String ENTITY_NAME2 = entity();
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareDate() throws Exception {
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
    @Test
    public void testSeriesQueryMultipleYearGroupBothEntities() throws Exception {
        SeriesQuery query = new SeriesQuery();
        query.setEntities(Arrays.asList(ENTITY_NAME1, ENTITY_NAME2));
        query.setMetric(METRIC_NAME);
        query.setStartDate("1900-01-01T00:00:00.000Z");
        query.setEndDate("2100-01-01T00:00:00.000Z");

        query.setAggregate(new Aggregate(AggregationType.COUNT, new Interval(12, TimeUnit.YEAR)));

        List<Series> resultSeries = executeQueryReturnSeries(query);

        Sample[] sampleDates1 = {
                Sample.ofDateInteger("1970-01-01T00:00:00.000Z", 1),
                Sample.ofDateInteger("2006-01-01T00:00:00.000Z", 2),
                Sample.ofDateInteger("2018-01-01T00:00:00.000Z", 1)
        };

        Sample[] sampleDates2 = {
                Sample.ofDateInteger("2006-01-01T00:00:00.000Z", 2)
        };

        assertSamples(sampleDates2, resultSeries.get(1).getData());
        assertSamples(sampleDates1, resultSeries.get(0).getData());
    }

    @Issue("4101")
    @Test
    public void testSeriesQueryMultipleYearGroupSingleEntity() throws Exception {
        SeriesQuery query = new SeriesQuery();
        query.setEntities(Collections.singletonList(ENTITY_NAME2));
        query.setMetric(METRIC_NAME);
        query.setStartDate("1900-01-01T00:00:00.000Z");
        query.setEndDate("2100-01-01T00:00:00.000Z");

        query.setAggregate(new Aggregate(AggregationType.COUNT, new Interval(12, TimeUnit.YEAR)));

        List<Series> resultSeries = executeQueryReturnSeries(query);

        Sample[] sampleDates2 = {
                Sample.ofDateInteger("2006-01-01T00:00:00.000Z", 2)
        };

        assertSamples(sampleDates2, resultSeries.get(0).getData());
    }

    private void assertSamples(Sample[] expectedSamples, List<Sample> actualSamples) throws Exception {
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
