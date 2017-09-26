package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.TimeZone;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.TestUtil.TimeTranslation;

public class PeriodMultipleYearsTest extends SqlTest {
    private static final String ENTITY_NAME1 = entity();
    private static final String ENTITY_NAME2 = entity();
    private static final String METRIC_NAME = metric();
    private static long zeroTimeOffset;

    @BeforeClass
    public static void prepareDate() throws Exception {
        TimeZone serverTimeZone = Util.getServerTimeZone();
        zeroTimeOffset = serverTimeZone.getOffset(0);

        Series series1 = new Series(ENTITY_NAME1, METRIC_NAME);
        series1.addSamples(
                Sample.ofDateInteger("1970-01-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2015-06-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2017-06-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2018-08-01T12:00:00.000Z", 0)
        );

        Series series2 = new Series(ENTITY_NAME2, METRIC_NAME);
        series2.addSamples(
                Sample.ofDateInteger("2012-06-01T12:00:00.000Z", 0),
                Sample.ofDateInteger("2016-06-01T12:00:00.000Z", 0)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Issue("4101")
    @Test
    public void testPeriodYearsBoth() {
        String sqlQuery = String.format(
                "SELECT entity, count(*), datetime FROM \"%s\" " +
                        "GROUP BY entity, period(12 year) " +
                        "ORDER BY entity, time",
                METRIC_NAME
        );

        final int DATE_COLUMN = 2;
        String[][] expectedRows;
        /* This condition due to #4591 and should be removed later */
        if (zeroTimeOffset < 0) {
            expectedRows = new String[][]{
                    {ENTITY_NAME1, "1", "1969-01-01T00:00:00.000Z"},
                    {ENTITY_NAME1, "1", "2005-01-01T00:00:00.000Z"},
                    {ENTITY_NAME1, "2", "2017-01-01T00:00:00.000Z"},

                    {ENTITY_NAME2, "2", "2005-01-01T00:00:00.000Z"}
            };
        } else {
            expectedRows = new String[][]{
                    {ENTITY_NAME1, "1", "1970-01-01T00:00:00.000Z"},
                    {ENTITY_NAME1, "2", "2006-01-01T00:00:00.000Z"},
                    {ENTITY_NAME1, "1", "2018-01-01T00:00:00.000Z"},

                    {ENTITY_NAME2, "2", "2006-01-01T00:00:00.000Z"}
            };
        }

        for (int i = 0; i < expectedRows.length; i++)
            expectedRows[i][DATE_COLUMN] = TestUtil.timeTranslateDefault(expectedRows[i][DATE_COLUMN],
                    TimeTranslation.LOCAL_TO_UNIVERSAL);

        assertSqlQueryRows("Wrong result with grouping by multiple years period",
                expectedRows, sqlQuery);
    }

    @Issue("4101")
    @Test
    public void testPeriodYears() {
        String sqlQuery = String.format(
                "SELECT entity, count(*), datetime FROM \"%s\" " +
                        "WHERE entity = '%s' " +
                        "GROUP BY entity, period(12 year) " +
                        "ORDER BY entity, time",
                METRIC_NAME,
                ENTITY_NAME2
        );

        final int DATE_COLUMN = 2;

        String[][] expectedRows;
        /* This condition due to #4591 and should be removed later */
        if (zeroTimeOffset < 0) {
            expectedRows = new String[][]{
                    {ENTITY_NAME2, "2", "2005-01-01T00:00:00.000Z"}
            };
        } else {
            expectedRows = new String[][]{
                    {ENTITY_NAME2, "2", "2006-01-01T00:00:00.000Z"}
            };
        }

        for (int i = 0; i < expectedRows.length; i++)
            expectedRows[i][DATE_COLUMN] = TestUtil.timeTranslateDefault(expectedRows[i][DATE_COLUMN],
                    TimeTranslation.LOCAL_TO_UNIVERSAL);

        assertSqlQueryRows("Wrong result with grouping by multiple years period",
                expectedRows, sqlQuery);
    }
}
