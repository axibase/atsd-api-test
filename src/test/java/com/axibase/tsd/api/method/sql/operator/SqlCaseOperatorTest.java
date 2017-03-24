package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class SqlCaseOperatorTest extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series();
        series.setEntity(TEST_ENTITY_NAME);
        series.setMetric(TEST_METRIC_NAME);
        series.setData(Collections.singletonList(Mocks.SAMPLE));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     *  #3913
     */
    @Test
    public void testCaseInExpression() throws Exception {
        String sqlQuery = String.format("SELECT 100 - CASE WHEN value IS NULL THEN 0 ELSE 100 END FROM '%s'", TEST_METRIC_NAME);

        String[][] expectedRows = {{"0"}};

        assertSqlQueryRows("Incorrect query result with CASE operator in expression", expectedRows, sqlQuery);
    }

    /**
     *  #3913
     */
    @Test
    public void testCaseInAggregationFunction() throws Exception {
        String sqlQuery = String.format("SELECT SUM(100 - CASE WHEN value IS NULL THEN 0 ELSE 100 END) FROM '%s'", TEST_METRIC_NAME);

        String[][] expectedRows = {{"0"}};

        assertSqlQueryRows("Incorrect query result with CASE operator in aggregation function", expectedRows, sqlQuery);
    }

    /**
     *  #3913
     */
    @Test
    public void testCaseInCastFunction() throws Exception {
        String sqlQuery = String.format("SELECT CAST(100 - CASE WHEN value IS NULL THEN 0 ELSE 100 END AS STRING) FROM '%s'", TEST_METRIC_NAME);

        String[][] expectedRows = {{"0"}};

        assertSqlQueryRows("Incorrect query result with CASE operator in cast function", expectedRows, sqlQuery);
    }
}
