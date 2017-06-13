package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereIsNullTagsTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Metric.checkExists(METRIC_NAME);

        Series series1 = new Series();
        series1.setEntity(entity());
        series1.setMetric(METRIC_NAME);
        series1.addSamples(new Sample("2017-01-01T12:00:00.000Z", 0));
        series1.addTag("t1", "z");

        Series series2 = new Series();
        series2.setEntity(entity());
        series2.setMetric(METRIC_NAME);
        series2.addSamples(new Sample("2017-01-02T12:00:00.000Z", 0));
        series2.addTag("t2", "y");

        Series series3 = new Series();
        series3.setEntity(entity());
        series3.setMetric(METRIC_NAME);
        series3.addSamples(new Sample("2017-01-03T12:00:00.000Z", 0));
        series3.addTag("t1", "a");

        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    /**
     * #4112
     */
    @Test
    public void testWhereIsNullTags() {
        String sqlQuery = String.format(
                "SELECT tags.t1, tags.t2 " +
                        "FROM '%s' " +
                        "WHERE isnull(tags.t1, 'a') = 'a' ",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"null", "y"},
                {"a", "null"},
        };

        assertSqlQueryRows("Wrong result with WHERE isnull(tags...)", expectedRows, sqlQuery);
    }
}
