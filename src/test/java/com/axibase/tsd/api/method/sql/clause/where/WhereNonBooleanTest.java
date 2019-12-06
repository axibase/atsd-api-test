package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WhereNonBooleanTest extends SqlTest {
    private static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    private static void prepareData() throws Exception {
        MetricMethod.createOrReplaceMetricCheck(new Metric(METRIC_NAME));
    }

    @Issue("4926")
    @Test(description = "Test that WHERE does not accept numeric value as condition")
    public void testWhereTextFails() {
        String sqlQuery = String.format("SELECT * FROM \"%s\" WHERE 1", METRIC_NAME);
        assertBadSqlRequest("Invalid WHERE clause expression: 1 at line 1 position 103 near \"1\"", sqlQuery);
    }

    @Issue("4926")
    @Test(description = "Test that WHERE does not accept string value as condition")
    public void testWhereStringFails() {
        String sqlQuery = String.format("SELECT * FROM \"%s\" WHERE 'abc'", METRIC_NAME);
        assertBadSqlRequest("Invalid WHERE clause expression: 'abc' at line 1 position 103 near \"'abc'\"", sqlQuery);
    }

    @Issue("4926")
    @Test(description = "Test that we have correct error description " +
            "with bad-formed time interval for interpolation")
    public void testWhereBadTimeIntervalFail() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "WHERE datetime >= '2017-01-01T07:50:00Z' AND '2017-01-01T10:50:00Z' OR datetime " +
                        "BETWEEN '2017-01-01T11:50:00Z' and '2017-01-01T12:50:00Z' " +
                        "WITH INTERPOLATE(1 MONTH, LINEAR, INNER, VALUE NaN, START_TIME)", METRIC_NAME);
        assertBadSqlRequest("Invalid expression: 'datetime >= '2017-01-01T07:50:00Z' and " +
                "'2017-01-01T10:50:00Z'' at line 1 position 107 near \"datetime\"", sqlQuery);
    }
}
