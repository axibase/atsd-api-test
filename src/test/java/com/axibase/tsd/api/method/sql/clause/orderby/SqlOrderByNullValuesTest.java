package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlOrderByNullValuesTest extends SqlTest {

    private static final String TEST_ENTITY = entity();
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series testSeries = new Series(TEST_ENTITY, TEST_METRIC, "tag1", "null");
        testSeries.addSamples(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(Collections.singletonList(testSeries));
    }

    @Issue("4024")
    @Test
    public void testNullTags() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%s\" ORDER BY tags.tag",
                TEST_METRIC
        );

        String[][] expectedRows = { { "null" } };

        assertSqlQueryRows("ORDER BY null values error", expectedRows, sqlQuery);
    }
}
