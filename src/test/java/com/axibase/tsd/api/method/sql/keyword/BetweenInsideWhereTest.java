package com.axibase.tsd.api.method.sql.keyword;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class BetweenInsideWhereTest extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.addSamples(
                new Sample("2017-03-09T12:00:00.000Z", 1),
                new Sample("2017-03-10T12:00:00.000Z", 2),
                new Sample("2017-03-11T12:00:00.000Z", 3),
                new Sample("2017-03-12T12:00:00.000Z", 4)
        );

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #4014
     */
    @Test
    public void checkIfBetweenSuccededByAndWorks() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE datetime BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z' AND value != 0",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
                {"3"}
        };

        assertSqlQueryRows("BETWEEN fails when joined with another condition with AND", expectedRows, sqlQuery);
    }
}
