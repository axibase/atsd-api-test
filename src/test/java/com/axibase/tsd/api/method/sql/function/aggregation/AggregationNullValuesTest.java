package com.axibase.tsd.api.method.sql.function.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class AggregationNullValuesTest extends SqlTest {

    private static final String FIRST_ENTITY = entity();
    private static final String SECOND_ENTITY = entity();

    private static final String FIRST_METRIC = metric();
    private static final String SECOND_METRIC = metric();

    @BeforeClass
    public void prepareTestSeries() throws Exception {

        Series firstSeries = new Series(FIRST_ENTITY, FIRST_METRIC);
        for (int i = 0; i < 3; i++) {
            firstSeries.addData(new Sample(String.format("2017-01-0%sT00:00:00.000Z", i + 1), i));
        }

        Series secondSeries = new Series(SECOND_ENTITY, SECOND_METRIC);
        secondSeries.addData(new Sample("2016-12-25T00:00:00.000Z", 0));

        SeriesMethod.insertSeriesCheck(firstSeries, secondSeries);
    }

    /**
     * #3325
     */
    @Test
    public void testNullValues() {
        String sqlQuery = String.format("SELECT '%1$s'.entity as \"Item\", LAST('%1$s'.value) * LAST('%2$s'.value) " +
                "FROM '%1$s' " +
                "OUTER JOIN USING ENTITY '%2$s' " +
                "GROUP BY \"Item\"",
                FIRST_METRIC,
                SECOND_METRIC);

        String[][] expectedRows = {
                {FIRST_ENTITY, "NaN"},
                {SECOND_ENTITY, "NaN"}
        };

        assertSqlQueryRows("Wrong result in JOIN query with null values", expectedRows, sqlQuery);
    }
}
