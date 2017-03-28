package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class EntityRecreateTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME1 = entity();
    private static final String ENTITY_NAME2 = entity();

    @BeforeClass
    private void prepareData() throws Exception {
        Registry.Entity.register(ENTITY_NAME1);
        Registry.Entity.register(ENTITY_NAME2);
        Registry.Metric.register(METRIC_NAME);

        Series series1 = new Series();
        series1.setEntity(ENTITY_NAME1);
        series1.setMetric(METRIC_NAME);
        series1.addData(Mocks.SAMPLE);

        Series series2 = new Series();
        series2.setEntity(ENTITY_NAME2);
        series2.setMetric(METRIC_NAME);
        series2.addData(Mocks.SAMPLE);

        /* Insert first entity*/
        SeriesMethod.insertSeriesCheck(series1);

        /* Remove first entity*/
        EntityMethod.deleteEntity(ENTITY_NAME1);

        /* Insert second series */
        SeriesMethod.insertSeriesCheck(series2);
    }

    /**
     * 4037
     */
    @Test
    public void testRecreateEntity() throws Exception {
        String sqlQuery = String.format("SELECT entity FROM '%s' ORDER BY entity", METRIC_NAME);

        String[][] expectedRows = new String[][]{
                {ENTITY_NAME2}
        };

        assertSqlQueryRows("Entity recreation gives wrong result", expectedRows, sqlQuery);
    }

}
