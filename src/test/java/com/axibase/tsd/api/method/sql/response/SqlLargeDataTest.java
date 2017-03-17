package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.testng.Assert.fail;

public class SqlLargeDataTest  extends SqlTest {

    private final static int ENTITIES_COUNT = 70000;
    private final static int ENTITIES_COUNT_PER_REQUEST = 500;
    private final static String ENTITY_NAME = "test-sql-large-data-test-entity";
    private final static String METRIC_NAME = "test-sql-large-data-test-metric";

    /**
     * #3890
     */
    @Test
    public void testQueryLargeData() throws IOException, InterruptedException {

        ArrayList<ArrayList<SeriesCommand>> seriesRequests = new ArrayList<>(ENTITIES_COUNT / ENTITIES_COUNT_PER_REQUEST);
        seriesRequests.add(new ArrayList<SeriesCommand>(ENTITIES_COUNT_PER_REQUEST));

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

            ArrayList<SeriesCommand> currentRequest = seriesRequests.get(seriesRequests.size() - 1);
            if (currentRequest.size() < ENTITIES_COUNT_PER_REQUEST) {
                currentRequest.addAll(series.toCommands());
                continue;
            }

            currentRequest = new ArrayList<>(ENTITIES_COUNT_PER_REQUEST);
            currentRequest.addAll(series.toCommands());
            seriesRequests.add(currentRequest);
        }

        for (ArrayList<SeriesCommand> request : seriesRequests) {
            TCPSender.send(request);
        }

        // wait for atsd insert series
        Thread.sleep(30000);

        String sqlQuery = String.format("SELECT COUNT(value) FROM '%s'", METRIC_NAME);
        String[][] expectedRows = { { String.valueOf(ENTITIES_COUNT) } };

        // some series may be not inserted yet, so trying to execute request several times
        int triesCount = 5;
        boolean success = false;
        AssertionError lastError = null;

        for (int i = 0; i < triesCount; i++) {
            try {
                assertSqlQueryRows("Large data query error", expectedRows, sqlQuery);
                success = true;
                break;
            } catch (AssertionError ex) {
                lastError = ex;
            }
        }

        if (!success) {
            fail("Large data query error", lastError);
        }
    }
}
