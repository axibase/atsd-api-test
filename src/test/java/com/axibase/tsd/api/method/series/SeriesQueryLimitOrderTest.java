package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;

public class SeriesQueryLimitOrderTest extends SeriesMethod {
    private final String TEST_ENTITY = entity();
    private final String TEST_METRIC = metric();
    private final Sample[] TEST_SAMPLES = {
            Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
            Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
            Sample.ofDateInteger("2017-01-03T00:00:00Z", 3),
            Sample.ofDateInteger("2017-01-04T00:00:00Z", 4),
            Sample.ofDateInteger("2017-01-05T00:00:00Z", 5)
    };

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY, TEST_METRIC);
        series.addSamples(TEST_SAMPLES);

        insertSeriesCheck(series);
    }

    @Issue("4635")
    @Test(description = "test default DESC order")
    public void testDefaultOrderLimit1() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setLimit(1);
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 1, default order",
                query,
                TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 1, ASC")
    public void testAscOrderLimit1() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setLimit(1);
        query.setDirection("ASC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 1, ASC",
                query,
                TEST_SAMPLES[0]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 1, DESC")
    public void testDescOrderLimit1() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setLimit(1);
        query.setDirection("DESC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 1, DESC",
                query,
                TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 1, cache = true, ASC")
    public void testAscOrderLimit1Cache() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setLimit(1);
        query.setCache(true);
        query.setDirection("ASC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 1, cache = true, ASC",
                query,
                TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 1, cache = true, DESC")
    public void testDescOrderLimit1Cache() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setLimit(1);
        query.setCache(true);
        query.setDirection("DESC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 1, cache = true, DESC",
                query,
                TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 3, ASC")
    public void testAscOrderLimit3() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setLimit(3);
        query.setDirection("ASC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 3, ASC",
                query,
                TEST_SAMPLES[0], TEST_SAMPLES[1],TEST_SAMPLES[2]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 3, DESC")
    public void testDescOrderLimit3() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setLimit(3);
        query.setDirection("DESC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 3, DESC",
                query,
                TEST_SAMPLES[2], TEST_SAMPLES[3], TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with no LIMIT, ASC")
    public void testAscOrderNoLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setDirection("ASC");
        assertSeriesQueryResult(
                "Incorrect series query result with no LIMIT, ASC",
                query,
                TEST_SAMPLES[0], TEST_SAMPLES[1],TEST_SAMPLES[2], TEST_SAMPLES[3], TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with no LIMIT, DESC")
    public void testDescOrderNoLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setDirection("DESC");
        assertSeriesQueryResult(
                "Incorrect series query result with no LIMIT, DESC",
                query,
                TEST_SAMPLES[0], TEST_SAMPLES[1],TEST_SAMPLES[2], TEST_SAMPLES[3], TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 3, datetime filter, ASC")
    public void testAscOrderLimit3WithFilter() throws Exception {
        SeriesQuery query = new SeriesQuery(
                TEST_ENTITY,
                TEST_METRIC,
                TEST_SAMPLES[3].getRawDate(),
                Util.addOneMS(TEST_SAMPLES[4].getRawDate()));
        query.setLimit(3);
        query.setDirection("ASC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 3, datetime filter, ASC",
                query,
                TEST_SAMPLES[3], TEST_SAMPLES[4]);
    }

    @Issue("4635")
    @Test(description = "test series query result with LIMIT = 3, datetime filter, DESC")
    public void testDescOrderLimit3WithFilter() throws Exception {
        SeriesQuery query = new SeriesQuery(
                TEST_ENTITY,
                TEST_METRIC,
                TEST_SAMPLES[3].getRawDate(),
                Util.addOneMS(TEST_SAMPLES[4].getRawDate()));
        query.setLimit(3);
        query.setDirection("DESC");
        assertSeriesQueryResult(
                "Incorrect series query result with LIMIT = 3, datetime filter, DESC",
                query,
                TEST_SAMPLES[3], TEST_SAMPLES[4]);
    }

    private void assertSeriesQueryResult(
            String errorMessage,
            SeriesQuery query,
            Sample... expectedResult) throws Exception {
        List<Series> result = querySeriesAsList(query);
        Series actualSeries;
        if (result == null || result.size() == 0) {
            actualSeries = null;
        } else {
            actualSeries = result.get(0);
        }

        Series expectedSeries = new Series();
        expectedSeries.setEntity(TEST_ENTITY);
        expectedSeries.setMetric(TEST_METRIC);
        expectedSeries.addSamples(expectedResult);

        assertEquals(errorMessage, expectedSeries, actualSeries);
    }
}
