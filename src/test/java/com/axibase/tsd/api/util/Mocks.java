package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class Mocks {
    public static final String ISO_TIME = "2016-06-03T09:23:00.000Z";
    public static final Long MILLS_TIME = date().getTime();
    public static final String DECIMAL_VALUE = "123.4567";
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
    public static final String ALERT_OPEN_VALUE = "1";
    public static final String ENTITY_TAGS_PROPERTY_TYPE = "$entity_tags";


    public static Date date() {
        return TestUtil.parseDate(ISO_TIME);
    }

    public static Entity entity() {
        Entity resultEntity = new Entity(TestUtil.TestNames.entity());
        resultEntity.setTags(TAGS);
        resultEntity.setEnabled(false);
        resultEntity.setTimeZoneID(TIMEZONE_ID);
        resultEntity.setInterpolationMode(InterpolationMode.LINEAR);
        resultEntity.setLabel(LABEL);
        return resultEntity;
    }

    public static Metric metric() {
        Metric resultMetric = new Metric(TestUtil.TestNames.metric());
        resultMetric.setLabel(LABEL);
        resultMetric.setTags(TAGS);
        resultMetric.setTimeZoneID(TIMEZONE_ID);
        resultMetric.setEnabled(false);
        resultMetric.setDescription(DESCRIPTION);
        resultMetric.setDataType(DataType.DOUBLE);
        resultMetric.setVersioned(true);
        return resultMetric;
    }

    public static Series series() {
        Series resultSeries = new Series(TestUtil.TestNames.entity(), TestUtil.TestNames.metric());
        resultSeries.setTags(TAGS);
        resultSeries.addData(SAMPLE);
        return resultSeries;
    }
}