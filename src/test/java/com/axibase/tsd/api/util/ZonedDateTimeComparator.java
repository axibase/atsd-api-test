package com.axibase.tsd.api.util;

import java.time.ZonedDateTime;
import java.util.Comparator;

public class ZonedDateTimeComparator implements Comparator<ZonedDateTime> {
    @Override
    public int compare(ZonedDateTime left, ZonedDateTime right) {
        return left.toInstant().compareTo(right.toInstant());
    }
}
