package com.axibase.tsd.api.method.sql.function.rows;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class FunctionsInsideBetweenOperatorTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(entity(), METRIC_NAME);

        series.addSamples(
                Sample.ofDateInteger("2017-01-01T12:00:00.000Z", 1),
                Sample.ofDateInteger("2017-01-02T12:00:00.000Z", 2),
                Sample.ofDateInteger("2017-01-03T12:00:00.000Z", 4),
                Sample.ofDateInteger("2017-01-04T12:00:00.000Z", 3),
                Sample.ofDateInteger("2017-01-05T12:00:00.000Z", 5),
                Sample.ofDateInteger("2017-01-06T12:00:00.000Z", 6)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("4060")
    @Test
    public void testLagLeadInsideBetweenOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE value BETWEEN lag(value) and lead(value)",
                METRIC_NAME
        );

        String[][] expectedRows = {{"2"}, {"5"}};

        assertSqlQueryRows("Incorrect result with lag/lead in BETWEEN operator",
                expectedRows, sqlQuery);
    }
    @Issue("4060")
    @Test
    public void testSqrtInsideBetweenOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE value BETWEEN sqrt(value) and value",
                METRIC_NAME
        );

        String[][] expectedRows = {{"1"}, {"2"}, {"4"}, {"3"}, {"5"}, {"6"}};

        assertSqlQueryRows("Incorrect result with sqrt in BETWEEN operator",
                expectedRows, sqlQuery);
    }
}
