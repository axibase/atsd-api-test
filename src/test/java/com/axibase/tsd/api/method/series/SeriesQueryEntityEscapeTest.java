package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;

public class SeriesQueryEntityEscapeTest extends SeriesMethod {
    private final String TEST_ENTITY = "*?%_\\-1\\*\\?\\%\\_\\\\-2\\\\*\\\\?\\\\%\\\\_\\\\\\-3" + entity();
    private final String TEST_METRIC = metric();
    private Series TEST_SERIES;

    @BeforeClass
    public void prepareData() throws Exception {
        TEST_SERIES = new Series(TEST_ENTITY, TEST_METRIC);
        TEST_SERIES.addSamples(Mocks.SAMPLE);
        SeriesMethod.insertSeriesCheck(TEST_SERIES);
    }

    @DataProvider
    public Object[][] provideEntityFilters() {
        return new Object[][] {
                {"*-1*"},
                {"\\**-1*"},
                {"\\*\\??*-1*"},
                {"\\*\\?%_\\\\-1*"},
                {"*-1\\\\\\**-2*"},
                {"*-1\\\\\\*\\\\\\?*-2*"},
                {"*-1\\\\\\*\\\\\\?\\\\%\\\\_*-2*"},
                {"*-1\\\\\\*\\\\\\?\\\\%\\\\_\\\\\\\\-2*"},
                {"*-2*-3*"},
                {"*-2\\\\\\\\\\**-3*"},
                {"*-2\\\\\\\\\\*\\\\\\\\\\?*-3*"},
                {"*-2\\\\\\\\\\*\\\\\\\\\\?\\\\\\\\%*-3*"},
                {"*-2\\\\\\\\\\*\\\\\\\\\\?\\\\\\\\%\\\\\\\\_*-3*"},
                {"*-2\\\\\\\\\\*\\\\\\\\\\?\\\\\\\\%\\\\\\\\_\\\\\\\\\\\\-3*"}
        };
    }

    @Issue("4640")
    @Test(
            dataProvider = "provideEntityFilters",
            description = "test entity name escaping in series query")
    public void testEntityEscape(String filter) throws Exception {
        List<Series> series =  SeriesMethod.querySeriesAsList(
                new SeriesQuery(filter, TEST_METRIC, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE));

        assertEquals(String.format("Incorrect series count with filter %s", filter),1, series.size());
        assertEquals(String.format("Incorrect series returned with filter %s", filter), TEST_SERIES, series.get(0));
    }
}
