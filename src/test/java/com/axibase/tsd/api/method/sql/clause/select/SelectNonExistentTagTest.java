package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SelectNonExistentTagTest extends SqlTest {
    private static String metricName;

    @BeforeClass
    public static void prepareData() throws Exception {
        Series s = Mocks.series();
        metricName = s.getMetric();

        SeriesMethod.insertSeriesCheck(s);
    }

    @Issue("4007")
    @Test
    public void testNonExistentTag() {
        String sqlQuery = String.format("SELECT tags.do_not_exist FROM \"%s\"", metricName);

        String[][] expectedRows = {
                {"null"}
        };

        assertSqlQueryRows("Can't select tag that does not exist", expectedRows, sqlQuery);
    }
}
