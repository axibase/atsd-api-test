package com.axibase.tsd.api.model.series.query.transformation.group;

public enum PlaceFunction {
    AVG("avg()"),
    MIN("min()"),
    MAX("max()"),
    SUM("sum()"),
    COUNT("count()"),
    MEDIAN("median()"),
    MEDIAN_ABS_DEV("median_abs_dev()"),
    STANDARD_DEVIATION("stdev()"),
    PERCENTILE_999("percentile(99.9)"),
    PERCENTILE_995("percentile(99.5)"),
    PERCENTILE_99("percentile(99)"),
    PERCENTILE_95("percentile(95)"),
    PERCENTILE_90("percentile(90)"),
    PERCENTILE_75("percentile(75)"),
    PERCENTILE_50("percentile(50)"),
    PERCENTILE_25("percentile(25)"),
    PERCENTILE_10("percentile(10)"),
    PERCENTILE_5("percentile(5)"),
    PERCENTILE_1("percentile(1)"),
    PERCENTILE_05("percentile(0.5)"),
    PERCENTILE_01("percentile(0.1)");

    private String text;

    PlaceFunction(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
