package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class EntityRecreateTest extends SqlTest {

    @Issue("4037")
    @Test
    public void testRecreateEntity() throws Exception {
        final String metricName = metric();
        final String entityName1 = entity();
        final String entityName2 = entity();

        Series series1 = new Series(entityName1, metricName);
        series1.addSamples(Mocks.SAMPLE);

        Series series2 = new Series(entityName2, metricName);
        series2.addSamples(Mocks.SAMPLE);

        /* Insert first entity*/
        SeriesMethod.insertSeriesCheck(series1);

        /* Remove first entity*/
        EntityMethod.deleteEntity(entityName1);

        /* Insert second series */
        SeriesMethod.insertSeriesCheck(series2);

        String sqlQuery = String.format("SELECT entity FROM \"%s\" ORDER BY entity", metricName);

        String[][] expectedRows = {{entityName2}};

        assertSqlQueryRows("Entity recreation gives wrong result", expectedRows, sqlQuery);
    }

}
