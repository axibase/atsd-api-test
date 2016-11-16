package com.axibase.tsd.api.util;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.parseDate;

public class Mocks {
    public static final String ISO_TIME = "2016-06-03T09:23:00.000Z";

    public static Date date() {
        return parseDate(ISO_TIME);
    }

    public static final Long MILLS_TIME = date().getTime();

    public static final String DECIMAL_VALUE = "123.4567";

    public static final Sample SAMPLE = new Sample(ISO_TIME, DECIMAL_VALUE);

    public static final String TIMEZONE_ID = "GMT12";

    public static final String LABEL = "label";

    public static final String DESCRIPTION = "description";

    public static final Map<String, String> TAGS = Collections.singletonMap("tag", "value");

    public static Entity entity() {
        Entity resultEntity = new Entity(Util.TestNames.entity());
        resultEntity.setTags(TAGS);
        resultEntity.setEnabled(false);
        resultEntity.setTimeZoneID(TIMEZONE_ID);
        resultEntity.setInterpolationMode(InterpolationMode.LINEAR);
        resultEntity.setLabel(LABEL);
        return resultEntity;
    }

    public static Metric metric() {
        Metric resultMetric = new Metric(Util.TestNames.metric());
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
        Series resultSeries = new Series(Util.TestNames.entity(), Util.TestNames.metric());
        resultSeries.setTags(TAGS);
        resultSeries.addData(SAMPLE);
        return resultSeries;
    }
}