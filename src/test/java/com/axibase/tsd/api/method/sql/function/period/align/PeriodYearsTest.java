package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames;

public class PeriodYearsTest extends SqlTest {
    private static final String ENTITY_NAME1 = TestNames.entity();
    private static final String ENTITY_NAME2 = TestNames.entity();
    private static final String METRIC_NAME = TestNames.metric();

    @BeforeClass
    public static void prepareDate() throws Exception {
        Registry.Entity.register(ENTITY_NAME1);
        Registry.Entity.register(ENTITY_NAME2);
        Registry.Metric.register(METRIC_NAME);

        Series series1 = new Series();
        series1.setEntity(ENTITY_NAME1);
        series1.setMetric(METRIC_NAME);
        series1.addData(new Sample("1970-01-01T12:00:00.000Z", 0));
        series1.addData(new Sample("2015-06-01T12:00:00.000Z", 0));
        series1.addData(new Sample("2017-06-01T12:00:00.000Z", 0));
        series1.addData(new Sample("2018-08-01T12:00:00.000Z", 0));

        Series series2 = new Series();
        series2.setEntity(ENTITY_NAME2);
        series2.setMetric(METRIC_NAME);
        series2.addData(new Sample("2016-06-01T12:00:00.000Z", 0));

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Test
    public void testPeriodYears() {
        String sqlQuery = String.format(
                "SELECT entity, count(*), datetime FROM '%s' " +
                        "GROUP BY entity, period(12 year) " +
                        "ORDER BY entity, time",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, "1", TestUtil.translateLocalToUniversal("1970-01-01T00:00:00.000Z")},
                {ENTITY_NAME1, "2", TestUtil.translateLocalToUniversal("2006-01-01T00:00:00.000Z")},
                {ENTITY_NAME1, "1", TestUtil.translateLocalToUniversal("2018-01-01T00:00:00.000Z")},

                {ENTITY_NAME2, "1", TestUtil.translateLocalToUniversal("2006-01-01T00:00:00.000Z")},
        };

        assertSqlQueryRows("Wrong result with grouping by multiple years period",
                expectedRows, sqlQuery);
    }
}
