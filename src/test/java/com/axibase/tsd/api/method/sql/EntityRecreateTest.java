package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class EntityRecreateTest extends SqlTest {

    /**
     * #4037
     */
    @Test
    public void testRecreateEntity() throws Exception {
        final String metricName = metric();
        final String entityName1 = entity();
        final String entityName2 = entity();

        Registry.Entity.register(entityName1);
        Registry.Entity.register(entityName2);
        Registry.Metric.register(metricName);

        Series series1 = new Series();
        series1.setEntity(entityName1);
        series1.setMetric(metricName);
        series1.addData(Mocks.SAMPLE);

        Series series2 = new Series();
        series2.setEntity(entityName2);
        series2.setMetric(metricName);
        series2.addData(Mocks.SAMPLE);

        /* Insert first entity*/
        SeriesMethod.insertSeriesCheck(series1);

        /* Remove first entity*/
        EntityMethod.deleteEntity(entityName1);

        /* Insert second series */
        SeriesMethod.insertSeriesCheck(series2);

        String sqlQuery = String.format("SELECT entity FROM '%s' ORDER BY entity", metricName);

        String[][] expectedRows = {{entityName2}};

        assertSqlQueryRows("Entity recreation gives wrong result", expectedRows, sqlQuery);
    }

}
