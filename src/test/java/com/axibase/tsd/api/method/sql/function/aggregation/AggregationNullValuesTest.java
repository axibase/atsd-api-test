package com.axibase.tsd.api.method.sql.function.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class AggregationNullValuesTest extends SqlTest {

    private static final String FIRST_ENTITY = entity();
    private static final String SECOND_ENTITY = entity();

    private static final String FIRST_METRIC = metric();
    private static final String SECOND_METRIC = metric();

    @BeforeClass
    public void prepareTestSeries() throws Exception {

        Series firstSeries = new Series(FIRST_ENTITY, FIRST_METRIC);
        for (int i = 0; i < 3; i++) {
            firstSeries.addSamples(Sample.ofDateInteger(String.format("2017-01-0%sT00:00:00.000Z", i + 1), i));
        }

        Series secondSeries = new Series(SECOND_ENTITY, SECOND_METRIC);
        secondSeries.addSamples(Sample.ofDateInteger("2016-12-25T00:00:00.000Z", 0));

        SeriesMethod.insertSeriesCheck(firstSeries, secondSeries);
    }

    /**
     * #3880
     */
    @Test
    public void testNullValues() {
        String sqlQuery = String.format("SELECT \"%1$s\".entity as \"Item\", LAST(\"%1$s\".value) * LAST(\"%2$s\".value) " +
                "FROM \"%1$s\" " +
                "OUTER JOIN USING ENTITY \"%2$s\" " +
                "GROUP BY \"Item\"",
                FIRST_METRIC,
                SECOND_METRIC);

        String[][] expectedRows = {
                {"null", "NaN"},
                {FIRST_ENTITY, "NaN"}
        };

        assertSqlQueryRows("Wrong result in JOIN query with null values", expectedRows, sqlQuery);
    }
}
