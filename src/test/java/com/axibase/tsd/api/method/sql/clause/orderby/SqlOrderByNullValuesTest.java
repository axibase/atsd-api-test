package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class SqlOrderByNullValuesTest extends SqlTest {

    private static final String TEST_ENTITY = entity();
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series testSeries = new Series();
        testSeries.setEntity(TEST_ENTITY);
        testSeries.setMetric(TEST_METRIC);
        testSeries.setData(Collections.singletonList(Mocks.SAMPLE));
        HashMap<String, String> tags = new HashMap<>();
        tags.put("tag1", "null");
        testSeries.setTags(tags);

        SeriesMethod.insertSeriesCheck(Collections.singletonList(testSeries));
    }

    /**
     * #4024
     */
    @Test
    public void testNullTags() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' ORDER BY tags.tag",
                TEST_METRIC
        );

        String[][] expectedRows = { { "null" } };

        assertSqlQueryRows("ORDER BY null values error", expectedRows, sqlQuery);
    }
}
