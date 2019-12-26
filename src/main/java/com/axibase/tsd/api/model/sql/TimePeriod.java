package com.axibase.tsd.api.model.sql;

import com.axibase.tsd.api.util.Util;
import lombok.Getter;

@Getter
public class TimePeriod {
    private final long startTime;
    private final long endTime;

    public TimePeriod(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimePeriod(String startISODate, String endISODate) {
        this(Util.getUnixTime(startISODate), Util.getUnixTime(endISODate));
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
