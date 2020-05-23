package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;

import javax.ws.rs.client.Entity;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class Mocks {
    private static final TestNameGenerator NAME_GENERATOR = new TestNameGenerator();

    public static final String ISO_TIME = "2017-08-01T00:00:00.000Z";
    public static final long MILLS_TIME = Util.getUnixTime(ISO_TIME);
    public static final BigDecimal DECIMAL_VALUE = new BigDecimal("123.4567");
    public static final String TEXT_VALUE = "text";
    public static final Sample SAMPLE = Sample.ofDateDecimal(ISO_TIME, DECIMAL_VALUE);
    public static final Sample TEXT_SAMPLE = Sample.ofDateText(ISO_TIME, TEXT_VALUE);
    public static final String TIMEZONE_ID = "GMT0";
    public static final String LABEL = "label";
    public static final String DESCRIPTION = "description";
    public static final Map<String, String> TAGS = Collections.singletonMap("tag", "value");
    public static final int ALERT_OPEN_VALUE = 1;
    public static final String ENTITY_TAGS_PROPERTY_TYPE = "$entity_tags";
    public static final int INT_VALUE = 22;
    public static final ScientificNotationNumber SCIENTIFIC_NOTATION_VALUE = new ScientificNotationNumber(INT_VALUE);
    public static final Entity<?> JSON_OBJECT = Entity.json(TAGS); //forming json object from map

    public static String metric() {
        return NAME_GENERATOR.newMetricName();
    }

    public static String entity() {
        return NAME_GENERATOR.newEntityName();
    }

    public static String entityGroup() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.ENTITY_GROUP);
    }

    public static String property() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.PROPERTY);
    }

    public static String message() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.MESSAGE);
    }

    public static String propertyType() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.PROPERTY_TYPE);
    }

    public static String replacementTable() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.REPLACEMENT_TABLE).replaceAll(":", "_");
    }

    public static String namedCollection() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.NAMED_COLLECTION);
    }

    public static Series series() {
        Series resultSeries = new Series(entity(), metric(), TAGS);
        resultSeries.addSamples(SAMPLE);
        return resultSeries;
    }

    public static String tradeExchange() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.TRADE_EXCHANGE);
    }

    public static String tradeClass() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.TRADE_CLASS);
    }

    public static String tradeSymbol() {
        return NAME_GENERATOR.newTestName(TestNameGenerator.Key.TRADE_SYMBOL);
    }
}
