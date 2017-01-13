package com.axibase.tsd.api.method.sql.clause.limit;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.Util.TestNames;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class LimitAggregationFunctionTest extends SqlTest {
    private static Metric testMetric;

    @BeforeClass
    public static void setTestSeries() throws Exception {
        final Integer seriesCount = 10;
        testMetric = new Metric(TestNames.metric());
        MetricMethod.createOrReplaceMetric(testMetric);
        Long time = Mocks.MILLS_TIME;
        List<Series> seriesList = new ArrayList<>();
        for (int i = 0; i < seriesCount; i++) {
            Series series = new Series();
            series.setMetric(testMetric.getName());
            series.setEntity(TestNames.entity());
            for (int j = 0; j < (i + 1); j++) {
                series.addData(new Sample(Util.ISOFormat(time), j));
                time += 1000L;
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
                function, testMetric.getName()
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
                "SELECT %s(value) FROM '%s'%nWHERE time > %s AND time < %s %n",
                function, testMetric.getName(), Mocks.MILLS_TIME, Mocks.MILLS_TIME + 10000L
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
                "SELECT %s(value) FROM '%s'%nWHERE time > %s AND time < %s GROUP BY entity%n",
                function, testMetric.getName(), Mocks.MILLS_TIME, Mocks.MILLS_TIME + 10000L
        );
        StringTable tableWithoutLimit = queryTable(sqlQuery);
        String limitSqlQuery = sqlQuery.concat("LIMIT 2");
        assertSqlQueryRows(tableWithoutLimit.getRows().subList(0, 2), limitSqlQuery);
    }
}
