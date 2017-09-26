package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlParseTimeTest extends SqlTest {
    private static final String TEST_METRIC_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series1.addSamples(Sample.ofDateInteger(Mocks.ISO_TIME, 1));

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1));
    }

    @Issue("3711")
    @Test(timeOut = 10000)
    public void testIfParseTimeIsAppropriate() {
        Integer numberOfTimes = 27;

        String sqlQuery = String.format(
                "SELECT " + sumNTimes(numberOfTimes, "value") + " FROM \"%s\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {numberOfTimes.toString()}
        };

        assertSqlQueryRows("Summation gives wrong result", expectedRows, sqlQuery);
    }

    private String sumNTimes(int numberOfTimes, String expression){
        StringBuilder stringbuilder = new StringBuilder(expression);

        for (int i = 0; i < numberOfTimes - 1; i++) {
            stringbuilder.append("+" + expression);
        }
        return stringbuilder.toString();
    }

    @Issue("4437")
    @Test
    public void testDateStringNotConverting() {
        String sqlQuery = String.format("SELECT '%s'", Mocks.ISO_TIME);

        String[][] expectedRows = {
                {Mocks.ISO_TIME}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4437")
    @Test
    public void testDateStringNotConvertingWithAlias() {
        String sqlQuery =  String.format("SELECT '%s' as \"date\"", Mocks.ISO_TIME);

        String[][] expectedRows = {
                {Mocks.ISO_TIME}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4437")
    @Test
    public void testDateStringNotConvertingWithFrom() {
        String sqlQuery = String.format("SELECT '%s' as \"date\" FROM \"%s\"", Mocks.ISO_TIME, TEST_METRIC_NAME);

        String[][] expectedRows = {
                {Mocks.ISO_TIME}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4437")
    @Test
    public void testDateStringNotConvertingWithStringFunction() {
        String sqlQuery = String.format("SELECT CONCAT('2017-08-01T00:00:00.000', 'Z') as \"date\" FROM \"%s\"", TEST_METRIC_NAME);

        String[][] expectedRows = {
                {Mocks.ISO_TIME}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
