package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereIsNullTagsTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(entity(), METRIC_NAME, "t1", "z");
        series1.addSamples(Sample.ofDateInteger("2017-01-01T12:00:00.000Z", 0));

        Series series2 = new Series(entity(), METRIC_NAME, "t2", "y");
        series2.addSamples(Sample.ofDateInteger("2017-01-02T12:00:00.000Z", 0));

        Series series3 = new Series(entity(), METRIC_NAME, "t1", "a");
        series3.addSamples(Sample.ofDateInteger("2017-01-03T12:00:00.000Z", 0));

        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    /**
     * #4112
     */
    @Test
    public void testWhereIsNullTags() {
        String sqlQuery = String.format(
                "SELECT tags.t1, tags.t2 " +
                        "FROM \"%s\" " +
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
