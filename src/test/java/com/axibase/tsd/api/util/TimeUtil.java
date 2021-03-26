package com.axibase.tsd.api.util;

import java.time.ZonedDateTime;

public class TimeUtil {

    public static long epochNano(String isoTime) {
        ZonedDateTime time = ZonedDateTime.parse(isoTime);
        return time.toEpochSecond() * 1_000_000_000L + time.getNano();
    }
}
