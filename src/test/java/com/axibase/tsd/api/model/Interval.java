package com.axibase.tsd.api.model;

public class Interval {
    private int count;
    private IntervalUnit unit;

    public Interval(int count, IntervalUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public IntervalUnit getUnit() {
        return unit;
    }

    public void setUnit(IntervalUnit unit) {
        this.unit = unit;
    }
}
