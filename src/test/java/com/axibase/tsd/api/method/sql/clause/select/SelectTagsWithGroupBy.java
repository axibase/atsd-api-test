package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SelectTagsWithGroupBy extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME, "tag1", "tag1value", "tag2", "tag2value");
        series.addSamples(new Sample(Mocks.ISO_TIME, Mocks.DECIMAL_VALUE));

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #4013
     */
    @Test
    public void testIfTagsAsteriskCanBeAppliedWhenGroupedByTags() {
        String sqlQuery = String.format(
                "SELECT tags.* FROM '%s' t1 GROUP BY tags",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"tag1value", "tag2value"}
        };

        assertSqlQueryRows("tags.* in SELECT can't be applied when used with GROUP BY tags", expectedRows, sqlQuery);
    }
}
