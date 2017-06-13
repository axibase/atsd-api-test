package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class Mocks {
    private static final TestNameGenerator NAME_GENERATOR = new TestNameGenerator();

    public static final String ISO_TIME = "2016-06-03T09:23:00.000Z";
    public static final Long MILLS_TIME = date().getTime();
    public static final BigDecimal DECIMAL_VALUE = new BigDecimal("123.4567");
    public static final String TEXT_VALUE = "text";
    public static final Sample SAMPLE = new Sample(ISO_TIME, DECIMAL_VALUE);
    public static final Sample TEXT_SAMPLE = new TextSample(ISO_TIME, TEXT_VALUE);
    public static final String TIMEZONE_ID = "GMT0";
    public static final String LABEL = "label";
    public static final String DESCRIPTION = "description";
    public static final Map<String, String> TAGS = Collections.singletonMap("tag", "value");
    public static final String MIN_QUERYABLE_DATE = "1000-01-01T00:00:00.000Z";
    public static final String MAX_QUERYABLE_DATE = "9999-12-31T23:59:59.999Z";
    public static final String MIN_STORABLE_DATE = "1970-01-01T00:00:00.000Z";
    public static final String MAX_STORABLE_DATE = "2106-02-07T06:59:59.999Z";
    public static final int ALERT_OPEN_VALUE = 1;
    public static final String ENTITY_TAGS_PROPERTY_TYPE = "$entity_tags";

    public static Date date() {
        return TestUtil.parseDate(ISO_TIME);
    }

    public static String metric() { return NAME_GENERATOR.getMetricName(); }

    public static String entity() { return NAME_GENERATOR.getEntityName(); }

    public static String entityGroup() { return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.ENTITY_GROUP); }

    public static String property() { return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.PROPERTY); }

    public static String message() { return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.MESSAGE); }

    public static String propertyType() {
            return NAME_GENERATOR.getTestName(TestNameGenerator.Keys.PROPERTY_TYPE);
        }

    public static Series series() {
        Series resultSeries = new Series(entity(), metric(), TAGS);
        resultSeries.addSamples(SAMPLE);
        return resultSeries;
    }
}