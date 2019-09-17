package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectDistinctTest extends SqlTest {
    private static final String METRIC_WITH_ALL_ROWS_UNIQUE = Mocks.metric();

    private static final List<DistinctSample> DISTINCT_SAMPLES_ALL_ROWS_UNIQUE = Arrays.asList(
            DistinctSample.of(Mocks.entity(), Mocks.MILLS_TIME + 1, 1),
            DistinctSample.of(Mocks.entity(), Mocks.MILLS_TIME + 2, 2),
            DistinctSample.of(Mocks.entity(), Mocks.MILLS_TIME + 3, 3)
    );

    private static final String METRIC_CONTAINING_REPEATING_VALUES = Mocks.metric();
    private static final String REPEATED_ENTITY = Mocks.entity();
    private static final List<DistinctSample> DISTINCT_SAMPLES_WITH_REPEATING_VALUES = Arrays.asList(
            DistinctSample.of(REPEATED_ENTITY, Mocks.MILLS_TIME + 1, 1),
            DistinctSample.of(REPEATED_ENTITY, Mocks.MILLS_TIME + 2, 1),
            DistinctSample.of(REPEATED_ENTITY, Mocks.MILLS_TIME + 3, 2),
            DistinctSample.of(REPEATED_ENTITY, Mocks.MILLS_TIME + 4, 2)
    );


    @BeforeClass
    public static void prepareData() throws Exception {
        final List<Series> samples = DISTINCT_SAMPLES_ALL_ROWS_UNIQUE.stream()
                .map(s -> new Series()
                        .setMetric(METRIC_WITH_ALL_ROWS_UNIQUE)
                        .setEntity(s.entity)
                        .addSamples(Sample.ofTimeInteger(s.timestamp, s.value)))
                .collect(Collectors.toList());
        SeriesMethod.insertSeriesCheck(samples);

        final List<Series> repeatedSamples = DISTINCT_SAMPLES_WITH_REPEATING_VALUES.stream()
                .map(s -> new Series()
                        .setMetric(METRIC_CONTAINING_REPEATING_VALUES)
                        .setEntity(s.entity)
                        .addSamples(Sample.ofTimeInteger(s.timestamp, s.value)))
                .collect(Collectors.toList());
        SeriesMethod.insertSeriesCheck(repeatedSamples);
    }

    @DataProvider
    public static Object[][] distinctLists() {
        return new Object[][]{
                {DISTINCT_SAMPLES_ALL_ROWS_UNIQUE, METRIC_WITH_ALL_ROWS_UNIQUE},
                {DISTINCT_SAMPLES_WITH_REPEATING_VALUES, METRIC_CONTAINING_REPEATING_VALUES}
        };
    }

    @Issue("6536")
    @Test(
            description = "Tests that 'SELECT DISTINCT entity' returns unique entities",
            dataProvider = "distinctLists"
    )
    public void testSelectDistinctEntity(List<DistinctSample> distinctSamples, String metric) {
        String sqlQuery = String.format("SELECT DISTINCT entity FROM \"%s\" ORDER BY entity", metric);
        assertSqlQueryRows(composeExpectedRows(distinctSamples, DistinctSample::getEntity), sqlQuery);
    }

    @Issue("6536")
    @Test(
            description = "Tests that 'SELECT DISTINCT value' returns unique values",
            dataProvider = "distinctLists"
    )
    public void testSelectDistinctValue(List<DistinctSample> distinctSamples, String metric) {
        String sqlQuery = String.format("SELECT DISTINCT value FROM \"%s\" ORDER BY value", metric);
        assertSqlQueryRows(composeExpectedRows(distinctSamples, DistinctSample::getValue), sqlQuery);
    }

    @Issue("6536")
    @Test(
            description = "Tests that 'SELECT DISTINCT time' returns unique timestamps",
            dataProvider = "distinctLists"
    )
    public void testSelectDistinctTime(List<DistinctSample> distinctSamples, String metric) {
        String sqlQuery = String.format("SELECT DISTINCT time FROM \"%s\" ORDER BY time", metric);
        assertSqlQueryRows(composeExpectedRows(distinctSamples, DistinctSample::getTimestamp), sqlQuery);
    }

    @Issue("6536")
    @Test(
            description = "Tests that 'SELECT DISTINCT' returns expected rows if multiple columns are requested",
            dataProvider = "distinctLists"
    )
    public void testSelectAllDistinct(List<DistinctSample> distinctSamples, String metric) {
        String sqlQuery = String.format("SELECT DISTINCT entity, value, time FROM \"%s\"", metric);
        assertSqlQueryRows(composeExpectedRows(distinctSamples, DistinctSample::getEntity, DistinctSample::getValue, DistinctSample::getTimestamp), sqlQuery);
    }

    @Issue("6536")
    @Test(
            description = "Tests that 'SELECT DISTINCT entity, value' returns expected rows if multiple columns are requested",
            dataProvider = "distinctLists"
    )
    public void testSelectEntityValueDistinct(List<DistinctSample> distinctSamples, String metric) {
        String sqlQuery = String.format("SELECT DISTINCT entity, value FROM \"%s\"", metric);
        assertSqlQueryRows(composeExpectedRows(distinctSamples, DistinctSample::getEntity, DistinctSample::getValue), sqlQuery);
    }

    @Issue("6536")
    @Test(
            description = "Tests that 'SELECT DISTINCT entity, time' returns expected rows if multiple columns are requested",
            dataProvider = "distinctLists"
    )
    public void testSelectEntityTimeDistinct(List<DistinctSample> distinctSamples, String metric) {
        String sqlQuery = String.format("SELECT DISTINCT entity, time FROM \"%s\"", metric);
        assertSqlQueryRows(composeExpectedRows(distinctSamples, DistinctSample::getEntity, DistinctSample::getTimestamp), sqlQuery);
    }

    @Issue("6536")
    @Test(
            description = "Tests that 'SELECT DISTINCT value, time' returns expected rows if multiple columns are requested",
            dataProvider = "distinctLists"
    )
    public void testSelectValueTimeDistinct(List<DistinctSample> distinctSamples, String metric) {
        String sqlQuery = String.format("SELECT DISTINCT value, time FROM \"%s\"", metric);
        assertSqlQueryRows(composeExpectedRows(distinctSamples, DistinctSample::getValue, DistinctSample::getTimestamp), sqlQuery);
    }

    @SafeVarargs
    private static String[][] composeExpectedRows(List<DistinctSample> sampleList, Function<DistinctSample, Object>... fieldGetters) {
        return sampleList.stream()
                .map(sample -> Arrays.stream(fieldGetters)
                        .map(getter -> getter.apply(sample))
                        .map(String::valueOf)
                        .collect(Collectors.toList())) // distinct doesn't work on arrays
                .distinct()
                .map(col -> col.toArray(ArrayUtils.EMPTY_STRING_ARRAY))
                .toArray(String[][]::new);
    }

    @Data(staticConstructor = "of")
    private static final class DistinctSample {
        private final String entity;
        private final long timestamp;
        private final int value;
    }
}
