package com.axibase.tsd.api.model;

public class Interval {
    private int count;
    private TimeUnit unit;

    public Interval(int count, TimeUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }
}
