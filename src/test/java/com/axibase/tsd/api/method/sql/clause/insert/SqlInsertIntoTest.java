package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

public class SqlInsertIntoTest extends SqlTest {
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final long MILLIS_TIME = Mocks.MILLS_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private static final String ENTITY_1 = Mocks.entity();
    private static final String METRIC_1 = Mocks.metric();
    private Series series;

    private static final String ENTITY_2 = Mocks.entity();
    private static final String METRIC_2 = Mocks.metric();
    private static final String TAG_KEY = "tag";
    private static final String TAG_VALUE = "value";

    private static final String ENTITY_3 = Mocks.entity();
    private static final String METRIC_3 = Mocks.metric();

    private static final String ENTITY_4 = Mocks.entity();
    private static final String METRIC_4 = Mocks.metric();

    private static final String ENTITY_5 = Mocks.entity();
    private static final String METRIC_5 = Mocks.metric();
    private static final String TEXT = Mocks.TEXT_VALUE;

    private static final String ENTITY_6 = Mocks.entity();
    private static final String METRIC_6 = Mocks.metric();
    private static final int NEGATIVE_VALUE = -1;

    @BeforeClass
    public void prepareData() throws Exception {
        series = new Series(ENTITY_1, METRIC_1);
        series.addSamples(Sample.ofDateInteger(ISO_TIME, VALUE));
        SeriesMethod.insertSeriesCheck(series);

        EntityMethod.createOrReplaceEntityCheck(ENTITY_2);
        EntityMethod.createOrReplaceEntityCheck(ENTITY_3);
        EntityMethod.createOrReplaceEntityCheck(ENTITY_4);
        EntityMethod.createOrReplaceEntityCheck(ENTITY_5);
        EntityMethod.createOrReplaceEntityCheck(ENTITY_6);

        MetricMethod.createOrReplaceMetricCheck(METRIC_2);
        MetricMethod.createOrReplaceMetricCheck(METRIC_3);
        MetricMethod.createOrReplaceMetricCheck(METRIC_4);
        MetricMethod.createOrReplaceMetricCheck(METRIC_5);
        MetricMethod.createOrReplaceMetricCheck(METRIC_6);
    }

    @Test(
            description = "Tests that if series already exists, new sample is inserted."
    )
    @Issue("5962")
    public void testInsertion() {
        int newValue = VALUE + 1;
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value) VALUES('%s', '%s', %d)"
                , METRIC_1, ENTITY_1, ISO_TIME, newValue);
        assertOkRequest("Insertion of series with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(series.addSamples(Sample.ofDateInteger(ISO_TIME, newValue)))));
    }

    @Test(
            description = "Tests that series with tags can be inserted."
    )
    @Issue("5962")
    public void testInsertionWithTags() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, tags.%s) VALUES('%s', '%s', %d, '%s')",
                METRIC_2, TAG_KEY, ENTITY_2, ISO_TIME, VALUE, TAG_VALUE);
        assertOkRequest("Insertion of series with tag with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(ENTITY_2)
                        .setMetric(METRIC_2)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
                        .addTag(TAG_KEY, TAG_VALUE)
        )));
    }

    @Test(
            description = "Tests insertion of series via atsd_series option."
    )
    @Issue("5962")
    public void testInsertionViaAtsdSeries() {
        String sqlQuery = String.format("INSERT INTO \"atsd_series\"(entity, datetime, \"%s\") VALUES('%s', '%s', %d)"
                ,METRIC_3, ENTITY_3, ISO_TIME, VALUE );
        assertOkRequest("Insertion of series via atsd_series with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(ENTITY_3)
                        .setMetric(METRIC_3)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
        )));
    }

    @Test(
            description = "Tests insertion of series with time in millis"
    )
    @Issue("5962")
    public void testInsertionWithMillis() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, time, value) VALUES('%s', %d, %d)"
                , METRIC_4, ENTITY_4, MILLIS_TIME, VALUE);
        assertOkRequest("Insertion of series with millis time with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(ENTITY_4)
                        .setMetric(METRIC_4)
                        .addSamples(Sample.ofTimeInteger(MILLIS_TIME, VALUE))
        )));
    }

    @Test(
            description = "Tests insertion of series with text sample"
    )
    @Issue("5962")
    public void testInsertionOfText() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, text) VALUES('%s', '%s', '%s')"
                ,METRIC_5, ENTITY_5, ISO_TIME, TEXT );
        assertOkRequest("Insertion of series with text sample with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(ENTITY_5)
                        .setMetric(METRIC_5)
                        .addSamples(Sample.ofDateText(ISO_TIME, TEXT))
        )));
    }

    @Test(
            description = "Tests insertion of series with negative value"
    )
    @Issue("5962")
    public void testInsertionWithNegativeValue() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value) VALUES('%s', '%s', %d)"
                ,METRIC_6, ENTITY_6, ISO_TIME, NEGATIVE_VALUE );
        assertOkRequest("Insertion of series with negative sample with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(ENTITY_6)
                        .setMetric(METRIC_6)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, NEGATIVE_VALUE))
        )));
    }
}
