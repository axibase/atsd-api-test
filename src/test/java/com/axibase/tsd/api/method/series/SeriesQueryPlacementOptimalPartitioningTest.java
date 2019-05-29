package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesMetaInfo;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolateType;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.model.series.query.transformation.group.Place;
import com.axibase.tsd.api.model.series.query.transformation.group.PlaceFunction;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * Take some setting of parameter {@link Group#place}.
 *
 * Check that response contains correct data after grouping with placement.
 *
 * Methods insertSeries() and addSamplesToSeries() create input series
 */

public class SeriesQueryPlacementOptimalPartitioningTest extends SeriesMethod {

    /**
     * Series parameters
     */
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String END_DATE = "2019-01-02T00:00:00Z";
    private static final String ZONE_ID = "Etc/UTC";
    private static final String QUERY_ENTITY = "*";
    private static final String METRIC_NAME = "lp_usage";

    /**
     * Parameter for group
     */
    private static final int GROUP_PERIOD_COUNT = 1;
    private static final Period PERIOD = new Period(GROUP_PERIOD_COUNT, TimeUnit.HOUR, PeriodAlignment.START_TIME);
    private static final Place PLACE = new Place(2, "max() < 9", PlaceFunction.MAX.toString());

    /**
     * Expected data
     */
    private static final Set<String> FIRST_SET = new HashSet<>(Arrays.asList("lp_1", "lp_4", "lp_5"));
    private static final Set<String> SECOND_SET = new HashSet<>(Arrays.asList("lp_2", "lp_3"));
    private static final int EXPECTED_GROUP_COUNT = 2;
    private static final double EXPECTED_TOTAL_SCORE = 14.0;
    private static final double EXPECTED_GROUP_SCORE_FIRST = 8.0;
    private static final double EXPECTED_GROUP_SCORE_SECOND = 6.0;

    private static final SeriesQuery QUERY = new SeriesQuery(QUERY_ENTITY, METRIC_NAME, START_DATE, END_DATE)
            .setGroup(new Group()
                    .setType(GroupType.SUM)
                    .setPeriod(PERIOD)
                    .setInterpolate(new AggregationInterpolate(AggregationInterpolateType.NONE, false))
                    .setPlace(PLACE));

    @BeforeClass
    private void insertSeries() throws Exception {
        int seriesCount = 5;
        Series[] seriesArray = new Series[seriesCount];
        for (int i = 0; i < seriesCount; i++) {
            seriesArray[i] = new Series(String.format("lp_%s", i + 1), METRIC_NAME);
        }

        addSamplesToSeries(seriesArray);
        insertSeriesCheck(seriesArray);
    }

    @Issue("5965")
    @Test(description = "Checks that grouping is correct ")
    public void testOfGroupSet() {
        List<Series> seriesList = querySeriesAsList(QUERY);
        Set<Set<String>> expectedGroupSetOfSeries = new HashSet<>(Arrays.asList(FIRST_SET, SECOND_SET));
        Set<Set<String>> actualGroupSetOfSeries = new HashSet<>();
        for (Series series: seriesList) {
            actualGroupSetOfSeries.add(getGroupSetOfSeries(series));
        }

        assertEquals(actualGroupSetOfSeries, expectedGroupSetOfSeries, "The sets of grouped series do not match expected.");
    }

    @Issue("5965")
    @Test(description = "Checks that count of group is correct and that parameter totalScore is correct")
    public void testOfTotalScore() {
        List<Series> seriesList = querySeriesAsList(QUERY);
        assertEquals(seriesList.size(), EXPECTED_GROUP_COUNT, "Incorrect number of grouped series in response");
        double actualTotalScore = seriesList.get(0).getGroup().getTotalScore().doubleValue();
        assertEquals(actualTotalScore, EXPECTED_TOTAL_SCORE, "Mismatch of parameters totalScore by expected is detected");
    }

    @Issue("5965")
    @Test(description = "Checks that each grouped series has correct parameter groupScore")
    public void testOfGroupScore() {
        List<Series> seriesList = querySeriesAsList(QUERY);
        Map<Set<String>, Double> expectedGroupScore = new HashMap<>();
        expectedGroupScore.put(FIRST_SET, EXPECTED_GROUP_SCORE_FIRST);
        expectedGroupScore.put(SECOND_SET, EXPECTED_GROUP_SCORE_SECOND);

        Map<Set<String>, Double> actualGroupScore = new HashMap<>();
        for (Series series: seriesList) {
            actualGroupScore.put(getGroupSetOfSeries(series), series.getGroup().getGroupScore().doubleValue());
        }
        assertEquals(actualGroupScore, expectedGroupScore, "Mismatch of parameters groupScore by expected is detected");
    }

    private void addSamplesToSeries(Series... seriesArray) {
        long totalSamplesCount = 10;
        for (int i = 0; i < totalSamplesCount; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.HOUR, i);

            seriesArray[0].addSamples(Sample.ofDateDecimal(time, new BigDecimal(1 + ((i % 8 < 4) ? 0 : 2))));
            seriesArray[1].addSamples(Sample.ofDateDecimal(time, new BigDecimal(2 + ((i % 8 < 5) ? 0 : 1))));
            seriesArray[2].addSamples(Sample.ofDateDecimal(time, new BigDecimal(3 + ((i % 8 < 5) ? 1 : 0))));
            seriesArray[3].addSamples(Sample.ofDateDecimal(time, new BigDecimal(3 + i/Double.valueOf(totalSamplesCount - 1))));
            seriesArray[4].addSamples(Sample.ofDateDecimal(time, new BigDecimal(2 - i/Double.valueOf(totalSamplesCount - 1))));
        }
    }

    private Set<String> getGroupSetOfSeries(Series series) {
            Set<String> setSeries = new HashSet<>();
            for (SeriesMetaInfo seriesMetaInfo: series.getGroup().getSeries()) {
                setSeries.add(seriesMetaInfo.getEntity());
            }

            return setSeries;
    }
}
