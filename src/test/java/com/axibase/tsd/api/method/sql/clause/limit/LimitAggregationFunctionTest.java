package com.axibase.tsd.api.method.sql.clause.limit;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.ISO_TIME;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;


public class LimitAggregationFunctionTest extends SqlTest {
    private static String testMetric = metric();

    @BeforeClass
    public static void setTestSeries() throws Exception {
        final Integer seriesCount = 10;
        ZonedDateTime startDateTime = ZonedDateTime.parse(ISO_TIME);
        List<Series> seriesList = new ArrayList<>();
        int secondsOffset = 0;
        for (int i = 0; i < seriesCount; i++) {
            Series series = new Series(entity(), testMetric);
            for (int j = 0; j < (i + 1); j++) {
                series.addSamples(new Sample(startDateTime
                                        .plusSeconds(secondsOffset)
                                        .format(DateTimeFormatter.ISO_INSTANT),
                        j));

                secondsOffset++;
            }
            seriesList.add(series);
        }
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "aggregationFunctionProvider")
    private Object[][] provideAggregationFunction() {
        return new Object[][]{
                {"AVG"},
                {"SUM"},
                {"COUNT"},
                {"LAST"},
                {"COUNTER"},
                {"DELTA"},
                {"MIN"},
                {"MAX"},
                {"FIRST"},
                {"MIN_VALUE_TIME"},
                {"MAX_VALUE_TIME"},
                {"WTAVG"},
                {"WAVG "}
        };
    }

    /**
     * #3600
     */
    @Test(dataProvider = "aggregationFunctionProvider")
    public void testAggregateFunctionLimit(String function) {
        String sqlQuery = String.format(
                "SELECT %s(value) FROM '%s' %n",
                function, testMetric
        );
        StringTable tableWithoutLimit = queryTable(sqlQuery);
        String limitSqlQuery = sqlQuery.concat("LIMIT 1");
        assertSqlQueryRows(tableWithoutLimit.getRows().subList(0, 1), limitSqlQuery);
    }

    /**
     * #3600
     */
    @Test(dataProvider = "aggregationFunctionProvider")
    public void testAggregateFunctionLimitWithPredicate(String function) {
        String sqlQuery = String.format(
                "SELECT %s(value) " +
                "FROM '%s' " +
                "WHERE datetime > '2016-06-03T09:23:00.000Z' AND datetime < '2016-06-03T09:23:10.000Z' ",
                function, testMetric
        );
        StringTable tableWithoutLimit = queryTable(sqlQuery);
        String limitSqlQuery = sqlQuery.concat("LIMIT 1");
        assertSqlQueryRows(tableWithoutLimit.getRows().subList(0, 1), limitSqlQuery);
    }

    /**
     * #3600
     */
    @Test(dataProvider = "aggregationFunctionProvider")
    public void testAggregateFunctionLimitWithGrouping(String function) {
        String sqlQuery = String.format(
                "SELECT %s(value) " +
                "FROM '%s' " +
                "WHERE datetime > '2016-06-03T09:23:00.000Z' AND datetime < '2016-06-03T09:23:10.000Z' " +
                "GROUP BY entity ",
                function, testMetric
        );
        StringTable tableWithoutLimit = queryTable(sqlQuery);
        String limitSqlQuery = sqlQuery.concat("LIMIT 2");
        assertSqlQueryRows(tableWithoutLimit.getRows().subList(0, 2), limitSqlQuery);
    }
}
