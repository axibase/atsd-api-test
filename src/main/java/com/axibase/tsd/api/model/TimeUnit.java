package com.axibase.tsd.api.model;

import static java.util.concurrent.TimeUnit.*;

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
            case NANOSECOND: return NANOSECONDS.toMillis(count);
            case MILLISECOND: return count;
            case SECOND: return SECONDS.toMillis(count);
            case MINUTE: return MINUTES.toMillis(count);
            case HOUR: return HOURS.toMillis(count);
            case DAY: return DAYS.toMillis(count);
        }

        throw new UnsupportedOperationException(String.format("Time unit %s does not support conversion to milliseconds", this));
    }
}