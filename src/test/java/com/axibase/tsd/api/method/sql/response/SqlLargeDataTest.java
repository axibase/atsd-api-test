package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class SqlLargeDataTest  extends SqlTest {

    private final static int ENTITIES_COUNT = 70000;
    private final static int ENTITIES_COUNT_PER_REQUEST = 10000;
    private final static String ENTITY_NAME = "test-sql-large-data-test-entity";
    private final static String METRIC_NAME = "test-sql-large-data-test-metric";

    @BeforeClass
    public static void initialize() throws Exception {

        ArrayList<ArrayList<Series>> seriesRequests = new ArrayList<>(ENTITIES_COUNT / ENTITIES_COUNT_PER_REQUEST);
        seriesRequests.add(new ArrayList<Series>(ENTITIES_COUNT_PER_REQUEST));

        Registry.Metric.register(METRIC_NAME);

        for (int i = 0; i < ENTITIES_COUNT; i++) {
            Series series = new Series();

            // manually creating entity name and tags due to performance issues
            String entityName = ENTITY_NAME + i;
            Registry.Entity.register(entityName);

            series.setEntity(entityName);
            series.setMetric(METRIC_NAME);
            series.addTag("tag", String.valueOf(i));
            series.addData(Mocks.SAMPLE);

            ArrayList<Series> currentRequest = seriesRequests.get(seriesRequests.size() - 1);
            if (currentRequest.size() < ENTITIES_COUNT_PER_REQUEST) {
                currentRequest.add(series);
                continue;
            }

            currentRequest = new ArrayList<>(ENTITIES_COUNT_PER_REQUEST);
            currentRequest.add(series);
            seriesRequests.add(currentRequest);
        }

        for (ArrayList<Series> request : seriesRequests) {
            SeriesMethod.insertSeriesCheck(request);
        }
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
