package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Mocks.DECIMAL_VALUE;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class GroupByNullTagValue extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.setData(Arrays.asList(
                new Sample("2017-02-09T12:00:00.000Z", DECIMAL_VALUE),
                new Sample("2017-02-10T12:00:00.000Z", DECIMAL_VALUE)
                )
        );
        series.addTag("tag1", "tagname");

        Series seriesWithoutTag = new Series();
        seriesWithoutTag.setEntity(TEST_ENTITY_NAME);
        seriesWithoutTag.setMetric(TEST_METRIC_NAME);
        seriesWithoutTag.setData(Arrays.asList(
                new Sample("2017-02-11T12:00:00.000Z", DECIMAL_VALUE),
                new Sample("2017-02-12T12:00:00.000Z", DECIMAL_VALUE)
                )
        );

        SeriesMethod.insertSeriesCheck(series, seriesWithoutTag);
    }

    /**
     * #4028
     */
    @Test
    public void testGroupingByTagnameThatHasNullValues() {
        String sqlQuery = String.format(
                        "SELECT tags.tag1, avg(value) " +
                        "FROM '%s' " +
                        "GROUP BY tags.tag1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"null", DECIMAL_VALUE},
                {"tagname", DECIMAL_VALUE}
        };

        assertSqlQueryRows("GROUP BY tag name that has null values gives wrong result", expectedRows, sqlQuery);
    }
}
