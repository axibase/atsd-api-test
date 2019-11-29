package com.axibase.tsd.api.method.sql.clause.limit;

import com.axibase.tsd.api.method.compaction.CompactionMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Flaky;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CompactedLimitTest extends SqlTest {
    private static final String METRIC_NAME = Mocks.metric();

    private static final int SAMPLE_COUNT = 15;
    private static final int SAMPLE_LIMIT = 10;

    @BeforeTest
    public static void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        Sample[] samples = new Sample[SAMPLE_COUNT];
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            String sampleDate = String.format("2018-01-%02dT00:00:00.000Z", i + 1);
            samples[i] = Sample.ofDateInteger(sampleDate, i + 1);
        }
        series.addSamples(samples);
        SeriesMethod.insertSeriesCheck(series);

        CompactionMethod.performCompaction();
    }

    @Flaky
    @Issue("4947")
    @Test(description = "Test if limit returns requested number of records " +
            "after performing compaction")
    public void testLimitOnCompactedColumnFamily() {
        String sqlQuery = String.format("SELECT * FROM \"%s\" " +
                "WHERE datetime >= '2018-01-01T00:00:00.000Z' " +
                "ORDER BY time DESC " +
                "LIMIT %d", METRIC_NAME, SAMPLE_LIMIT);
        StringTable result = queryTable(sqlQuery);
        int resultSize = result.getRows().size();
        assertEquals(resultSize, SAMPLE_LIMIT, "Wrong limit result after compaction");
    }
}
