package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Collections;

public class SqlInsertionTest extends SqlTest {
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final long MILLIS_TIME = Mocks.MILLS_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private final String entity1 = Mocks.entity();
    private final String metric1 = Mocks.metric();
    private Series series;

    private final String entity2 = Mocks.entity();
    private final String metric2 = Mocks.metric();
    private static final String TAG_KEY = "tag";
    private static final String TAG_VALUE = "value";

    private final String entity3 = Mocks.entity();
    private final String metric3 = Mocks.metric();

    private final String entity4 = Mocks.entity();
    private final String metric4 = Mocks.metric();

    private final String entity5 = Mocks.entity();
    private final String metric5 = Mocks.metric();
    private static final String TEXT = Mocks.TEXT_VALUE;

    private final String entity6 = Mocks.entity();
    private final String metric6 = Mocks.metric();
    private final int NEGATIVE_VALUE = -1;

    private final String entity7 = Mocks.entity();
    private final String metric7 = Mocks.metric();
    private static final String SCIENTIFIC_NOTATION_VALUE = Mocks.SCIENTIFIC_NOTATION_VALUE;

    private final String entity8 = Mocks.entity();
    private final String metric8 = Mocks.metric();
    private static final String NaN = TestUtil.NaN;

    private final String entity9 = Mocks.entity();
    private final String metric9 = Mocks.metric();

    private final InsertionType insertionType;

    @Factory(dataProvider = "insertionType", dataProviderClass = InsertionType.class)
    public SqlInsertionTest(InsertionType insertionType) {
        this.insertionType = insertionType;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        series = new Series(entity1, metric1);
        series.addSamples(Sample.ofDateInteger(ISO_TIME, VALUE));
        SeriesMethod.insertSeriesCheck(series);

        EntityMethod.createOrReplaceEntityCheck(entity2);
        EntityMethod.createOrReplaceEntityCheck(entity3);
        EntityMethod.createOrReplaceEntityCheck(entity4);
        EntityMethod.createOrReplaceEntityCheck(entity5);
        EntityMethod.createOrReplaceEntityCheck(entity6);
        EntityMethod.createOrReplaceEntityCheck(entity7);
        EntityMethod.createOrReplaceEntityCheck(entity8);
        EntityMethod.createOrReplaceEntityCheck(entity9);

        MetricMethod.createOrReplaceMetricCheck(metric2);
        MetricMethod.createOrReplaceMetricCheck(metric3);
        MetricMethod.createOrReplaceMetricCheck(metric4);
        MetricMethod.createOrReplaceMetricCheck(metric5);
        MetricMethod.createOrReplaceMetricCheck(metric6);
        MetricMethod.createOrReplaceMetricCheck(metric7);
        MetricMethod.createOrReplaceMetricCheck(metric8);
        MetricMethod.createOrReplaceMetricCheck(new Metric()
                .setName(metric9)
                .setVersioned(true));
    }

    @Test(
            description = "Tests that if series already exists, new sample is inserted."
    )
    @Issue("5962")
    public void testInsertion() {
        int newValue = VALUE + 1;
        String newTime = Util.ISOFormat(MILLIS_TIME + 1);
        String sqlQuery = insertionType.insertionQuery(metric1,ImmutableMap.of("entity", entity1, "datetime", newTime, "value", newValue));
        assertOkRequest("Insertion of series with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(series.addSamples(
                Sample.ofDateInteger(newTime, newValue)
        ))));
    }

    @Test(
            description = "Tests that series with tags can be inserted."
    )
    @Issue("5962")
    public void testInsertionWithTags() {
        String sqlQuery = insertionType.insertionQuery(metric2, ImmutableMap.of("entity", entity2, "datetime", ISO_TIME, "value", VALUE
                , String.format("tags.%s", TAG_KEY), TAG_VALUE));
        assertOkRequest("Insertion of series with tag with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity2)
                        .setMetric(metric2)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
                        .addTag(TAG_KEY, TAG_VALUE)
        )));
    }

    @Test(
            description = "Tests insertion of series via atsd_series option."
    )
    @Issue("5962")
    public void testInsertionViaAtsdSeries() {
        String sqlQuery = insertionType.insertionQuery("atsd_series", ImmutableMap.of("entity", entity3, "datetime", ISO_TIME
                ,String.format("\"%s\"", metric3), VALUE));
        assertOkRequest("Insertion of series via atsd_series with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity3)
                        .setMetric(metric3)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
        )));
    }

    @Test(
            description = "Tests insertion of series with time in millis."
    )
    @Issue("5962")
    public void testInsertionWithMillis() {
        String sqlQuery = insertionType.insertionQuery(metric4, ImmutableMap.of("entity", entity4, "time", MILLIS_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with millis time with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity4)
                        .setMetric(metric4)
                        .addSamples(Sample.ofTimeInteger(MILLIS_TIME, VALUE))
        )));
    }

    @Test(
            description = "Tests insertion of series with text sample."
    )
    @Issue("5962")
    public void testInsertionOfText() {
        String sqlQuery = insertionType.insertionQuery(metric5, ImmutableMap.of("entity", entity5, "datetime", ISO_TIME, "text", TEXT));
        assertOkRequest("Insertion of series with text sample with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity5)
                        .setMetric(metric5)
                        .addSamples(Sample.ofDateText(ISO_TIME, TEXT))
        )));
    }

    @Test(
            description = "Tests insertion of series with negative value."
    )
    @Issue("5962")
    public void testInsertionWithNegativeValue() {
        String sqlQuery = insertionType.insertionQuery(metric6, ImmutableMap.of("entity", entity6, "datetime", ISO_TIME, "value", NEGATIVE_VALUE));
        assertOkRequest("Insertion of series with negative sample with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity6)
                        .setMetric(metric6)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, NEGATIVE_VALUE))
        )));
    }

    @Test(
            description = "Tests that series can be inserted with scientific notation."
    )
    @Issue("5962")
    public void testInsertionWithScientificNotation() {
        //TODO resolve problems with scientific notation. Probably, new class ScientificNotationNumber extends Number should be created
        String sqlQuery = insertionType.insertionQuery(metric7, ImmutableMap.of("entity", entity7, "datetime", ISO_TIME, "value", SCIENTIFIC_NOTATION_VALUE));
        assertOkRequest("Insertion of series with scientific notation with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity7)
                        .setMetric(metric7)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
        )));
    }

    @Test(
            description = "Test insertion of series with NaN value"
    )
    @Issue("5962")
    public void testInsertionWithNanValue() {
        //TODO do we still need String representation of NaN?
        String sqlQuery = insertionType.insertionQuery(metric8, ImmutableMap.of("entity", entity8, "datetime", ISO_TIME, "value", Double.NaN));
        assertOkRequest("Insertion of series with NaN value failed!", sqlQuery);
        String selectQuery = String.format("SELECT value FROM \"%s\"", metric8); //NaN value cannot be added to sample, checker cannot be used
        String[][] expectedRow = {
                {NaN}
        };
        assertSqlQueryRows("NaN value not found after insertion!", expectedRow, selectQuery);
    }

    @Test(
            description = "Tests that if versioning is present, only no additional samples will be inserted."
    )
    @Issue("5962")
    @Issue("6342")
    public void testInsertionWithVersionedMetric() {
        String sqlQuery = insertionType.insertionQuery(metric9, ImmutableMap.of("entity", entity9, "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with versioned with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity9)
                        .setMetric(metric9)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
        )));
    }
}
