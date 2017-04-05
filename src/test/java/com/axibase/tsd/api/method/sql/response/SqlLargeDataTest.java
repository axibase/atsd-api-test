package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.Checker;
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

import static org.testng.AssertJUnit.fail;

public class SqlLargeDataTest extends SqlTest {

    private final static int ENTITIES_COUNT = 70000;
    private final static String ENTITY_NAME = "test-sql-large-data-test-entity";
    private final static String METRIC_NAME = "test-sql-large-data-test-metric";
    private final static int FULL_CHECK_TRIES_COUNT = 5;

    /**
     * #3890
     */
    @Test
    public void testQueryLargeData() throws IOException, InterruptedException {

        ArrayList<SeriesCommand> seriesRequests = new ArrayList<>(ENTITIES_COUNT);

        Registry.Metric.register(METRIC_NAME);

        for (int i = 1; i <= ENTITIES_COUNT; i++) {
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

        TCPSender.sendChecked(
                new LargeDataQuickCheck(METRIC_NAME, ENTITIES_COUNT),
                seriesRequests);

        LargeDataFullCheck fullCheck = new LargeDataFullCheck(METRIC_NAME, ENTITIES_COUNT);
        Exception lastException = null;
        for (int i = 0; i < FULL_CHECK_TRIES_COUNT; i++) {
            try {
                Checker.check(fullCheck);
                lastException = null;
                break;
            } catch (Exception ex) {
                lastException = ex;
            }
        }

        if (lastException != null) {
            fail(lastException.getMessage());
        }
    }

    private class LargeDataQuickCheck extends AbstractCheck {

        private final String ERROR_TEXT = "Large data query error";
        private final String metricName;
        private final int entitiesCount;

        public LargeDataQuickCheck(String metricName, int entitiesCount) {
            this.metricName = metricName;
            this.entitiesCount = entitiesCount;
        }

        @Override
        public boolean isChecked() {

            String sqlQuery = String.format(
                    "SELECT COUNT(value) FROM '%s' m " +
                    "WHERE m.tags.tag='%s' OR " +
                          "m.tags.tag='%s' OR " +
                          "m.tags.tag='%s' OR " +
                          "m.tags.tag='%s'",
                    metricName,
                    entitiesCount,
                    entitiesCount - 5000,
                    entitiesCount - 10000,
                    entitiesCount - 15000);

            String[][] expectedRows = {{"4"}};

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

    private class LargeDataFullCheck extends AbstractCheck {

        private final String ERROR_TEXT = "Large data query error";
        private final String metricName;
        private final int entitiesCount;

        public LargeDataFullCheck(String metricName, int entitiesCount) {
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

