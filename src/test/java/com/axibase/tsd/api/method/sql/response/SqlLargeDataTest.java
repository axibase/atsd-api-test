package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class SqlLargeDataTest  extends SqlTest {

    private final static int ENTITIES_COUNT = 70000;
    private final static int EXPECTED_ALL_ENTITIES_PROCESSING_TIME_MS = 20000;
    private final static String ENTITY_NAME = "test-sql-large-data-test-entity";
    private final static String METRIC_NAME = "test-sql-large-data-test-metric";

    @BeforeClass
    public static void initialize() throws Exception {

        List<Series> seriesList = new ArrayList<>(ENTITIES_COUNT);

        for (int i = 0; i < ENTITIES_COUNT; i++) {
            Series series = new Series();

            // manually creating entity name and tags due to performance issues
            series.setEntity(ENTITY_NAME + i);
            series.setMetric(METRIC_NAME);
            series.addTag("tag", String.valueOf(i));
            series.addData(Mocks.SAMPLE);

            seriesList.add(series);
        }

        // inserting without check due to performance issues
        SeriesMethod.insertSeries(seriesList);

        // wait until all entities processed
        Thread.sleep(EXPECTED_ALL_ENTITIES_PROCESSING_TIME_MS);
    }

    /**
     * #3890
     */
    @Test
    public void testQueryLargeData() {
        String sqlQuery = String.format("SELECT COUNT(value) FROM '%s'", METRIC_NAME);

        String[][] expectedRows = { { String.valueOf(ENTITIES_COUNT) } };

        assertSqlQueryRows("Large data query error", expectedRows, sqlQuery);
    }
}
