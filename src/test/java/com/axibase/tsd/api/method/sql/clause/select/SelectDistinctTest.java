package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SelectDistinctTest extends SqlTest {
    private static final String METRIC = Mocks.metric();

    private static final String ENTITY_1 = Mocks.entity();
    private static final String ENTITY_2 = Mocks.entity();
    private static final String ENTITY_3 = Mocks.entity();

    private static final int[] DISTINCT_DATA = {1, 2, 3};
    private static final long[] DISTINCT_TIME = {Mocks.MILLS_TIME + 1, Mocks.MILLS_TIME  + 2, Mocks.MILLS_TIME + 3};

    @BeforeClass
    public void prepareData() throws Exception {
        Series series1 = new Series(ENTITY_1, METRIC);
        Series series2 = new Series().setEntity(ENTITY_2).setMetric(METRIC);
        Series series3 = new Series().setEntity(ENTITY_3).setMetric(METRIC);

        for(int i = 0; i < DISTINCT_DATA.length; i++) {
            Sample sample = Sample.ofTimeInteger(DISTINCT_TIME[i], DISTINCT_DATA[i]);
            series1.addSamples(sample);
            series2.addSamples(sample);
            series3.addSamples(sample);
        }
        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT entity' returns unique entities")
    public void testSelectDistinctEntity() {
        String sqlQuery = String.format("SELECT DISTINCT entity FROM \"%s\" ORDER BY entity", METRIC);
        String[][] expectedRows = {
                {ENTITY_1},
                {ENTITY_2},
                {ENTITY_3}
        };
        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT value' returns unique values")
    public void testSelectDistinctValue() {
        String sqlQuery = String.format("SELECT DISTINCT value FROM \"%s\" ORDER BY value", METRIC);
        String[][] expectedRows = {
                {String.valueOf(DISTINCT_DATA[0])},
                {String.valueOf(DISTINCT_DATA[1])},
                {String.valueOf(DISTINCT_DATA[2])}
        };
        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT time' returns unique values")
    public void testSelectDistinctTime() {
        String sqlQuery = String.format("SELECT DISTINCT time FROM \"%s\" ORDER BY time", METRIC);
        String[][] expectedRows = {
                {String.valueOf(DISTINCT_TIME[0])},
                {String.valueOf(DISTINCT_TIME[1])},
                {String.valueOf(DISTINCT_TIME[2])}
        };
        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
