package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;

public class SqlLargeDataTest extends SqlTest {

    private final static int ENTITIES_COUNT = 70000;
    private final static String ENTITY_NAME = "test-sql-large-data-test-entity";
    private final static String METRIC_NAME = "test-sql-large-data-test-metric";

    @Issue("3890")
    @Test(enabled = false)
    public void testQueryLargeData() throws Exception {
        ArrayList<SeriesCommand> seriesRequests = new ArrayList<>(ENTITIES_COUNT);
        Registry.Entity.checkExists(ENTITY_NAME);
        Registry.Metric.checkExists(METRIC_NAME);

        for (int i = 1; i <= ENTITIES_COUNT; i++) {
            // manually set entity and metric to avoid check
            Series series = new Series();
            series.setEntity(ENTITY_NAME);
            series.setMetric(METRIC_NAME);
            series.addTag("tag", String.valueOf(i));
            series.addSamples(createTestSample(i));

            seriesRequests.addAll(series.toCommands());
        }

        TCPSender.send(seriesRequests);
        Checker.check(new LargeDataCheck(ENTITY_NAME, METRIC_NAME, ENTITIES_COUNT));

        String sqlQuery = String.format(
                "SELECT COUNT(*) " +
                "FROM \"%s\" " +
                "GROUP BY entity",
                METRIC_NAME);

        String[][] expectedRows = {{String.valueOf(ENTITIES_COUNT)}};

        assertSqlQueryRows("Large data query error", expectedRows, sqlQuery);
    }

    private static Sample createTestSample(int value) {
        long millisTime = Mocks.MILLS_TIME + value * 1000;
        return Sample.ofJavaDateInteger(new Date(millisTime), value);
    }

    private class LargeDataCheck extends AbstractCheck {

        private final String ERROR_TEXT = "Large data query error";
        private final String entityName;
        private final String metricName;
        private final int entitiesCount;

        public LargeDataCheck(String entityName, String metricName, int entitiesCount) {
            this.entityName = entityName;
            this.metricName = metricName;
            this.entitiesCount = entitiesCount;
        }

        @Override
        public boolean isChecked() {

            String sqlQuery = String.format(
                    "SELECT COUNT(value) " +
                    "FROM \"%s\" " +
                    "WHERE entity = '%s'",
                    metricName,
                    entityName);

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

