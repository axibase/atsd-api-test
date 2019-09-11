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

    @BeforeClass
    public static void prepareData() throws Exception {
        final List<Series> samples = DISTINCT_SAMPLES.stream()
                .map(s -> new Series()
                        .setMetric(METRIC)
                        .setEntity(s.entity)
                        .addSamples(Sample.ofTimeInteger(s.timestamp, s.value)))
                .collect(Collectors.toList());
        SeriesMethod.insertSeriesCheck(samples);
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
