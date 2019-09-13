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
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectDistinctTest extends SqlTest {
    private static final String METRIC = Mocks.metric();

    private static final List<DistinctSample> DISTINCT_SAMPLES = Arrays.asList(
            DistinctSample.of(Mocks.entity(), Mocks.MILLS_TIME + 1, 1),
            DistinctSample.of(Mocks.entity(), Mocks.MILLS_TIME + 2, 2),
            DistinctSample.of(Mocks.entity(), Mocks.MILLS_TIME + 3, 3)
    );

    private static final String METRIC_2 = Mocks.metric();
    private static final List<DistinctSample> EQUAL_DISTINCT_SAMPLES = Arrays.asList(
            DistinctSample.of("6536", 6536, 6536),
            DistinctSample.of("6537", 6537, 6537),
            DistinctSample.of("6538", 6538, 6538)
    );

    @BeforeClass
    public static void prepareData() throws Exception {
        final List<Series> samples = DISTINCT_SAMPLES.stream()
                .map(s -> new Series()
                        .setMetric(METRIC)
                        .setEntity(s.entity)
                        .addSamples(Sample.ofTimeInteger(s.timestamp, s.value)))
                .collect(Collectors.toList());
        SeriesMethod.insertSeriesCheck(samples);

        final List<Series> equalSamples = EQUAL_DISTINCT_SAMPLES .stream()
                .map(s -> new Series()
                            .setMetric(METRIC_2)
                            .setEntity(s.entity)
                            .addSamples(Sample.ofTimeInteger(s.timestamp, s.value)))
                .collect(Collectors.toList());
        SeriesMethod.insertSeriesCheck(equalSamples);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT entity' returns unique entities")
    public void testSelectDistinctEntity() {
        String sqlQuery = String.format("SELECT DISTINCT entity FROM \"%s\" ORDER BY entity", METRIC);
        assertSqlQueryRows(composeExpectedRows(DistinctSample::getEntity), sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT value' returns unique values")
    public void testSelectDistinctValue() {
        String sqlQuery = String.format("SELECT DISTINCT value FROM \"%s\" ORDER BY value", METRIC);
        assertSqlQueryRows(composeExpectedRows(DistinctSample::getValue), sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT time' returns unique values")
    public void testSelectDistinctTime() {
        String sqlQuery = String.format("SELECT DISTINCT time FROM \"%s\" ORDER BY time", METRIC);
        assertSqlQueryRows(composeExpectedRows(DistinctSample::getTimestamp), sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT' returns unique values if multiple columns are requested")
    public void testSelectMultipleDistinct() {
        String sqlQuery = String.format("SELECT DISTINCT entity, value, time FROM \"%s\"", METRIC);
        assertSqlQueryRows(composeExpectedRowsFromStringArray(sample ->
            new String[] {sample.getEntity(), String.valueOf(sample.getValue()), String.valueOf(sample.getTimestamp())}
        ), sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Tests that 'SELECT DISTINCT' returns correct data if all columns have equal values")
    public void testSelectDistinctEqualData() {
        String sqlQuery = String.format("SELECT DISTINCT entity, value, time FROM \"%s\"", METRIC_2);
        assertSqlQueryRows(composeExpectedRowsFromStringArray(EQUAL_DISTINCT_SAMPLES, sample ->
                new String[] {sample.getEntity(), String.valueOf(sample.getValue()), String.valueOf(sample.getTimestamp())}
        ), sqlQuery);
    }

    private static String[][] composeExpectedRowsFromStringArray(List<DistinctSample> sampleList, Function<DistinctSample, String[]> arrayComposer) {
        return sampleList.stream()
                .map(arrayComposer)
                .toArray(String[][]::new);
    }

    private static String[][] composeExpectedRowsFromStringArray(Function<DistinctSample, String[]> arrayComposer) {
        return composeExpectedRowsFromStringArray(DISTINCT_SAMPLES, arrayComposer);
    }

    private static String[][] composeExpectedRows(Function<DistinctSample, Object> fieldGetter) {
        return DISTINCT_SAMPLES.stream()
                .map(fieldGetter)
                .map(String::valueOf)
                .map(ArrayUtils::toArray)
                .toArray(String[][]::new);
    }

    @Data(staticConstructor = "of")
    private static final class DistinctSample {
        private final String entity;
        private final long timestamp;
        private final int value;
    }
}
