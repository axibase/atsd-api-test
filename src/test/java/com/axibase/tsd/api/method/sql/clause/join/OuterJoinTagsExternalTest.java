package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class OuterJoinTagsExternalTest extends SqlTest {
    private static final String METRIC_NAME1 = metric();
    private static final String METRIC_NAME2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String entityName = entity();

        Registry.Entity.register(entityName);
        Registry.Metric.register(METRIC_NAME1);
        Registry.Metric.register(METRIC_NAME2);

        Series series1 = new Series();
        series1.setEntity(entityName);
        series1.setMetric(METRIC_NAME1);
        series1.addData(Mocks.SAMPLE);

        Series series2 = new Series();
        series2.setEntity(entityName);
        series2.setMetric(METRIC_NAME2);
        series2.addData(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    /**
     * #4058
     */
    @Test
    public void testOuterJoinTagsExternal() {
        String sqlQuery = String.format(
                "SELECT '%s'.tags FROM '%s' OUTER JOIN '%s' " +
                        "OPTION (ROW_MEMORY_THRESHOLD 0)",
                METRIC_NAME1,
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedRows = {{"null"}};

        assertSqlQueryRows(
                "Incorrect result for metric.tags in outer join (external memory)",
                expectedRows,
                sqlQuery
        );
    }
}
