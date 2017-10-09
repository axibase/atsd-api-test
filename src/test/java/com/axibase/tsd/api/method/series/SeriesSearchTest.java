package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SeriesSearchTest extends SeriesMethod {
    private final String prefix = "search_test_";
    private Entity TEST_ENTITY;
    private Metric TEST_METRIC;

    @BeforeTest
    public void prepareData() throws Exception {
        TEST_ENTITY = new Entity(prefix + Mocks.entity());
        TEST_METRIC = new Metric(prefix + Mocks.metric());

        Series series = new Series(TEST_ENTITY.getName(), TEST_METRIC.getName());
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1)
        );

        insertSeriesCheck(series);
        updateSearchIndex();
    }

    @Issue("4404")
    @Test
    public void testSearchAll() {
        SeriesSearchQuery query = new SeriesSearchQuery(prefix + "*");

        SeriesSearchResult result = searchSeries(query);
        SeriesSearchResultRecord[] expectedRecords = {
                new SeriesSearchResultRecord(TEST_ENTITY, TEST_METRIC, null, 1.0)
        };

        assertEquals(
                result.getData(),
                expectedRecords,
                "Incorrect result when searching all series");
    }
}