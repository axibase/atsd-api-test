package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class SqlParseTimeCheck extends SqlTest {
    private static final String TEST_METRIC_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series();

        series1.setMetric(TEST_METRIC_NAME);
        series1.setEntity(TEST_ENTITY_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1")
                )
        );

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1));
    }

    /**
     * #3711
     */
    @Test(timeOut = 10000)
    public void testLongParseTime() {
        String sqlQuery = String.format(
                "SELECT value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value+value FROM '%s'",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"27"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Join without Group by gives wrong result");
    }
}
