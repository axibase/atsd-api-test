package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static com.axibase.tsd.api.util.CommonAssertions.assertCheck;

public class SqlLargeDataTest extends SqlTest {

    private final static int ENTITIES_COUNT = 70000;
    private final static String ENTITY_NAME = "test-sql-large-data-test-entity";
    private final static String METRIC_NAME = "test-sql-large-data-test-metric";

    /**
     * #3890
     */
    @Test
    public void testQueryLargeData() throws IOException, InterruptedException {

        ArrayList<SeriesCommand> seriesRequests = new ArrayList<>(ENTITIES_COUNT);

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

            seriesRequests.addAll(series.toCommands());
        }

        TCPSender.sendChecked(new LargeDataCheck(METRIC_NAME, ENTITIES_COUNT), seriesRequests);
    }

    private class LargeDataCheck extends AbstractCheck {

        private final String ERROR_TEXT = "Large data query error";
        private final String metricName;
        private final int entitiesCount;

        public LargeDataCheck(String metricName, int entitiesCount) {
            this.metricName = metricName;
            this.entitiesCount = entitiesCount;
        }

        @Override
        public boolean isChecked() {

            String sqlQuery = String.format("SELECT COUNT(value) FROM '%s'", metricName);
            String[][] expectedRows = {{String.valueOf(entitiesCount)}};

            try {
                assertSqlQueryRows(ERROR_TEXT, expectedRows, sqlQuery);
            } catch (Error e) {
                return false;
            }

            return true;
        }

        @Override
        public String getErrorMessage() {
            return ERROR_TEXT;
        }
    }
}

