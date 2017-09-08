package com.axibase.tsd.api.method.sql.function.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountTest extends SqlTest {
    private static final String TEST_ENTITY_NAME = "e-test-sql-aggregation-count-1";
    private static final String TEST_METRIC_NAME = "m-test-sql-aggregation-count-1";
    
    private int total;
    private String minDateTime;
    private String maxDateTime;
    
    @BeforeClass
    public void prepareTestSeries() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                Sample.ofDateInteger("2016-10-12T00:00:00.000Z", 1),
                Sample.ofDateInteger("2016-10-13T00:00:00.000Z", 2),
                Sample.ofDateInteger("2016-10-14T00:00:00.000Z", 3),
                Sample.ofDateInteger("2016-10-15T00:00:00.000Z", 4),
                Sample.ofDateInteger("2016-10-16T00:00:00.000Z", 5),
                Sample.ofDateInteger("2016-10-17T00:00:00.000Z", 6),
                Sample.ofDateInteger("2016-10-18T00:00:00.000Z", 7)
        );
        total = series.getData().size();
        minDateTime = series.getData().get(0).getRawDate();
        maxDateTime = series.getData().get(total - 1).getRawDate();
        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider(name = "countAggregationArgumentsProvider")
    private Object[][] provideArgumentsForCountAggregation() {
        return new String[][]{
            {"*"},
            {"value"},
            {"entity"}
        };
    }

    /**
     * #3325
     */
    @Test(dataProvider = "countAggregationArgumentsProvider")
    public void testCountValue(String countArg) {
        String sqlQuery = selectCount(countArg);

        String[][] expectedRows = {
                {String.valueOf(total)}
        };

        assertSqlQueryRows("Wrong result of COUNT() in query:\n" + sqlQuery, expectedRows, sqlQuery);
    }

    private String selectCount(String argument) {
        String queryTemplate = "SELECT COUNT(%s) %n" +
                "FROM \"%s\" %n" +
                "WHERE datetime >= '%s' AND datetime <= '%s' %n";

        return String.format(queryTemplate, argument,
                TEST_METRIC_NAME, minDateTime, maxDateTime);
    }
}
