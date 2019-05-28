package com.axibase.tsd.api.model;

public enum TimeUnit {
    NANOSECOND,
    MILLISECOND,
    SECOND,
    MINUTE,
    HOUR,
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR;

    public long toMilliseconds(long count) {
        switch (this) {
            case NANOSECOND: return java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(count);
            case MILLISECOND: return count;
            case SECOND: return java.util.concurrent.TimeUnit.SECONDS.toMillis(count);
            case MINUTE: return java.util.concurrent.TimeUnit.MINUTES.toMillis(count);
            case HOUR: return java.util.concurrent.TimeUnit.HOURS.toMillis(count);
            case DAY: return java.util.concurrent.TimeUnit.DAYS.toMillis(count);
        }

        throw new UnsupportedOperationException(String.format("Time unit %s does not support conversion to milliseconds", this));
    }
}