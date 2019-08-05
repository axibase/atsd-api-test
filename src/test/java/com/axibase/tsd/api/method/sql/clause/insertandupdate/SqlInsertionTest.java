package com.axibase.tsd.api.method.sql.clause.insertandupdate;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.serialization.ValueDeserializer;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.ScientificNotationNumber;
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

    private final InsertionType insertionType;

    @Factory(dataProvider = "insertionType", dataProviderClass = InsertionType.class)
    public SqlInsertionTest(InsertionType insertionType) {
        this.insertionType = insertionType;
    }


    @Test(
            description = "Tests that if series already exists, new sample is inserted."
    )
    @Issue("5962")
    public void testInsertion() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        Series series = new Series(entity, metric);
        series.addSamples(Sample.ofDateInteger(ISO_TIME, VALUE));
        SeriesMethod.insertSeriesCheck(series);

        int newValue = VALUE + 1;
        String newTime = Util.ISOFormat(MILLIS_TIME + 1);
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "datetime", newTime, "value", newValue));
        assertOkRequest("Insertion of series with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(series.addSamples(
                Sample.ofDateInteger(newTime, newValue)
        ))));
    }

    @Test(
            description = "Tests that series with tags can be inserted."
    )
    @Issue("5962")
    public void testInsertionWithTags() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String tagKey = "tag";
        String tagValue = "value";
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", VALUE
                        , String.format("tags.%s", tagKey), tagValue));
        assertOkRequest("Insertion of series with tag with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity)
                        .setMetric(metric)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
                        .addTag(tagKey, tagValue)
        )));
    }

    @Test(
            description = "Tests insertion of series via atsd_series option."
    )
    @Issue("5962")
    public void testInsertionViaAtsdSeries() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String sqlQuery = insertionType.insertionQuery("atsd_series",
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME
                        , String.format("\"%s\"", metric), VALUE));
        assertOkRequest("Insertion of series via atsd_series with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity)
                        .setMetric(metric)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
        )));
    }

    @Test(
            description = "Tests insertion of series with time in millis."
    )
    @Issue("5962")
    public void testInsertionWithMillis() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "time", MILLIS_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with millis time with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity)
                        .setMetric(metric)
                        .addSamples(Sample.ofTimeInteger(MILLIS_TIME, VALUE))
        )));
    }

    @Test(
            description = "Tests insertion of series with text sample."
    )
    @Issue("5962")
    public void testInsertionOfText() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String textValue = Mocks.TEXT_VALUE;
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "text", textValue));
        assertOkRequest("Insertion of series with text sample with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity)
                        .setMetric(metric)
                        .addSamples(Sample.ofDateText(ISO_TIME, textValue))
        )));
    }

    @Test(
            description = "Tests insertion of series with negative value."
    )
    @Issue("5962")
    public void testInsertionWithNegativeValue() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        int negativeValue = -1;
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", negativeValue));
        assertOkRequest("Insertion of series with negative sample with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity)
                        .setMetric(metric)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, negativeValue))
        )));
    }

    @Test(
            description = "Tests that series can be inserted with scientific notation."
    )
    @Issue("5962")
    public void testInsertionWithScientificNotation() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        ScientificNotationNumber scientificNotationValue = Mocks.SCIENTIFIC_NOTATION_VALUE;
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", scientificNotationValue));
        assertOkRequest("Insertion of series with scientific notation with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity)
                        .setMetric(metric)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
        )));
    }

    @Test(
            description = "Test insertion of series with NaN value"
    )
    @Issue("5962")
    public void testInsertionWithNanValue() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", Double.NaN));
        assertOkRequest("Insertion of series with NaN value failed!", sqlQuery);
        String selectQuery = String.format("SELECT value FROM \"%s\"", metric); //NaN value cannot be added to sample, checker cannot be used
        String[][] expectedRow = {
                {TestUtil.NaN}
        };
        assertSqlQueryRows("NaN value not found after insertion!", expectedRow, selectQuery);
    }

    @Test(
            description = "Tests that if versioning is present, only no additional samples will be inserted."
    )
    @Issue("5962")
    @Issue("6342")
    public void testInsertionWithVersionedMetric() throws Exception {
        String entity = Mocks.entity();
        String metric = Mocks.metric();
        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with versioned with SQL failed!", sqlQuery);
        Checker.check(new SeriesCheck(Collections.singletonList(
                new Series()
                        .setEntity(entity)
                        .setMetric(metric)
                        .addSamples(Sample.ofDateInteger(ISO_TIME, VALUE))
        )));
    }
}
