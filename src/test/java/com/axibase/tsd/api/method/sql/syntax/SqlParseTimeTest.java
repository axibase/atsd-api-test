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

public class SqlParseTimeTest extends SqlTest {
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
    public void testIfParseTimeIsAppropriate() {
        Integer numberOfTimes = 27;

        String sqlQuery = String.format(
                "SELECT " + sumNTimes(numberOfTimes, "value") + " FROM '%s'",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {numberOfTimes.toString()}
        };

        assertSqlQueryRows("Summation gives wrong result", expectedRows, sqlQuery);
    }

    private String sumNTimes(int numberOfTimes, String expression){
        StringBuilder stringbuilder = new StringBuilder(expression);

        for (int i = 0; i < numberOfTimes - 1; i++) {
            stringbuilder.append("+" + expression);
        }
        return stringbuilder.toString();
    }
}
