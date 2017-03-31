package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.Test;

public class LikeOperatorTest extends SqlTest {
    /**
     * #4030
     */
    @Test
    public void testLikeOperatorForMetricInWhereClause() throws Exception {
        final String uniquePrefix = "unique";

        Series series = Mocks.series();
        series.setMetric(uniquePrefix + series.getMetric());
        Registry.Metric.register(series.getMetric());

        Series otherSeries = Mocks.series();
        SeriesMethod.insertSeriesCheck(series, otherSeries);

        String sql = String.format(
                "SELECT metric%n" +
                "FROM atsd_series%n" +
                "WHERE metric in ('%s', '%s')%n" +
                      "AND metric LIKE '%s*'%n" +
                "LIMIT 2",
                series.getMetric(), otherSeries.getMetric(), uniquePrefix
        );

        String[][] expected = {
                { series.getMetric() }
        };

        assertSqlQueryRows(expected, sql);
    }
}
