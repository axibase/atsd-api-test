package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesMetaInfo;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.model.series.query.transformation.group.Place;
import com.axibase.tsd.api.model.series.query.transformation.group.PlaceFunction;
import com.axibase.tsd.api.util.Mocks;
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
 * <p>
 * Check that response contains correct data after grouping with placement.
 * <p>
 * Methods insertSeries() and addSamplesToSeries() create input series
 */

public class SeriesQueryPlacementOptimalPartitioningTest extends SeriesMethod {

    /**
     * Series parameters
     */
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String END_DATE = "2019-01-02T00:00:00Z";
    private static final String ZONE_ID = Mocks.TIMEZONE_ID;
    private static final String QUERY_ENTITY = "*";
    private static final String METRIC_NAME = Mocks.metric();

    /**
     * Parameter place for grouping
     */
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
                    .setPlace(PLACE));

    @BeforeClass
    private void prepareData() throws Exception {
        final double[][] seriesValuesToInsert = {
                {1.0, 1.0, 1.0, 1.0, 3.0, 3.0, 3.0, 3.0, 1.0, 1.0},
                {2.0, 2.0, 2.0, 2.0, 2.0, 3.0, 3.0, 3.0, 2.0, 2.0},
                {4.0, 4.0, 4.0, 4.0, 4.0, 3.0, 3.0, 3.0, 4.0, 4.0},
                {3.0, 3.1, 3.2, 3.3, 3.4, 3.6, 3.7, 3.8, 3.9, 4.0},
                {2.0, 1.9, 1.8, 1.7, 1.6, 1.4, 1.3, 1.2, 1.1, 1.0}
        };

        final int seriesCount = seriesValuesToInsert.length;
        Series[] seriesArray = new Series[seriesCount];
        for (int i = 0; i < seriesCount; i++) {
            seriesArray[i] = new Series(String.format("lp_%s", i + 1), METRIC_NAME);
        }

        final int totalSamplesCount = seriesValuesToInsert[0].length;

        for (int i = 0; i < totalSamplesCount; i++) {

            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.HOUR, i);
            for (int j = 0; j < seriesArray.length; j++) {
                seriesArray[j].addSamples(Sample.ofDateDecimal(time, new BigDecimal(seriesValuesToInsert[j][i])));
            }
        }

        insertSeriesCheck(seriesArray);
    }

    @Issue("5965")
    @Test(description = "Checks that grouping is correct ")
    public void testOfGroupSet() {
        List<Series> seriesList = querySeriesAsList(QUERY);
        Set<Set<String>> expectedGroupSetOfSeries = new HashSet<>(Arrays.asList(FIRST_SET, SECOND_SET));
        Set<Set<String>> actualGroupSetOfSeries = new HashSet<>();
        for (Series series : seriesList) {
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
        for (Series series : seriesList) {
            actualGroupScore.put(getGroupSetOfSeries(series), series.getGroup().getGroupScore().doubleValue());
        }
        assertEquals(actualGroupScore, expectedGroupScore, "Mismatch of parameters groupScore by expected is detected");
    }

    private Set<String> getGroupSetOfSeries(Series series) {
        Set<String> setSeries = new HashSet<>();
        for (SeriesMetaInfo seriesMetaInfo : series.getGroup().getSeries()) {
            setSeries.add(seriesMetaInfo.getEntity());
        }

        return setSeries;
    }
}
