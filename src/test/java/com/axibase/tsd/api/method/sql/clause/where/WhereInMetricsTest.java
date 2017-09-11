package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereInMetricsTest extends SqlTest {
    private static final int METRIC_COUNT = 30;
    private static final String ENTITY_NAME = entity();
    private static String[] METRIC_NAMES = new String[METRIC_COUNT];

    @BeforeClass
    public static void prepareData() throws Exception {

        List<Series> seriesList = new ArrayList<>();

        for (int i = 0; i < METRIC_COUNT; i++) {
            METRIC_NAMES[i] = metric();
            Series series = new Series(ENTITY_NAME, METRIC_NAMES[i]);
            series.addSamples(Mocks.SAMPLE);

            seriesList.add(series);
        }
        Arrays.sort(METRIC_NAMES);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("4075")
    @Test
    public void testMetricsBasic() {
        String sqlQuery = String.format(
                "SELECT metric " +
                        "FROM atsd_series " +
                        "WHERE metric IN metrics('%s') " +
                        "ORDER BY metric",
                ENTITY_NAME
        );

        String[][] expectedRows = new String[METRIC_COUNT][];
        for (int i = 0; i < METRIC_COUNT; i++) {
            expectedRows[i] = new String[]{METRIC_NAMES[i]};
        }

        assertSqlQueryRows("Wrong result for WHERE metric in metrics()", expectedRows, sqlQuery);
    }
}
