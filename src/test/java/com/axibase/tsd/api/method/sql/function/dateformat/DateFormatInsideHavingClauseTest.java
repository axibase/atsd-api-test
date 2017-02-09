package com.axibase.tsd.api.method.sql.function.dateformat;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class DateFormatInsideHavingClauseTest extends SqlTest {
    /**
     * #3893
     */
    @Test
    public void testDateFormatInsideHavingGroupingByPeriod() throws Exception {
        String entityName = entity();
        String metric_name = metric();

        Series series = new Series(entityName, metric_name);
        series.setData(Arrays.asList(
                new Sample("2017-02-09T12:00:00.000Z", "0"),
                new Sample("2017-02-09T13:00:00.000Z", "0"),
                new Sample("2017-02-10T12:00:00.000Z", "0"),
                new Sample("2017-02-11T12:00:00.000Z", "0"),
                new Sample("2017-02-12T12:00:00.000Z", "0")
                )
        );

        SeriesMethod.insertSeriesCheck(series);

        String sqlQuery = String.format(
                "SELECT count(value) FROM '%s' " +
                        "GROUP BY period(1 day) " +
                        "HAVING date_format(time, 'u') = '4'",
                metric_name
        );

        String[][] expectedRows = {
                {"2"}
        };

        assertSqlQueryRows("Query with date_format inside HAVING gives wrong result", expectedRows, sqlQuery);
    }
}
