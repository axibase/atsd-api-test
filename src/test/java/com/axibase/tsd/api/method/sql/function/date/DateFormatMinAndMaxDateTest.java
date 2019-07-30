package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DateFormatMinAndMaxDateTest extends SqlTest {
    private static final String ENTITY = Mocks.entity();
    private static final String METRIC = Mocks.metric();
    private static final String STORED_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    @BeforeClass
    public void prepareData() throws Exception{
        Series series = new Series(ENTITY, METRIC);
        series.addSamples(Sample.ofDateInteger(STORED_TIME, VALUE));
        SeriesMethod.insertSeriesCheck(series);
    }

    @Test
    @Issue("6416")
    public void testMinDate() {
        String sqlQuery = String.format("SELECT date_format(min(time)) FROM \"%s\" WHERE datetime BETWEEN '%s' AND '%s'" +
                        " GROUP BY PERIOD(1 day)", METRIC,
                Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        String[][] expectedRow = {
                {STORED_TIME}
        };
        assertSqlQueryRows("Min date is incorrect for date_format function", expectedRow, sqlQuery);
    }

    @Test
    @Issue("6416")
    public void testMaxDate() {
        String sqlQuery = String.format("SELECT date_format(max(time)) FROM \"%s\" WHERE datetime BETWEEN '%s' AND '%s'" +
                        " GROUP BY PERIOD(1 day)", METRIC,
                Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        String[][] expectedRow = {
                {STORED_TIME}
        };
        assertSqlQueryRows("Min date is incorrect for date_format function", expectedRow, sqlQuery);
    }
}
