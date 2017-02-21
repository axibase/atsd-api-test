package com.axibase.tsd.api.model.sql;

import com.axibase.tsd.api.util.Util;

import static com.axibase.tsd.api.util.Util.parseDate;

public class TimePeriod {
    private long startTime;
    private long endTime;

    public TimePeriod(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimePeriod(String startISODate, String endISODate) {
        this.startTime = parseDate(startISODate).getTime();
        this.endTime = parseDate(endISODate).getTime();
    }

    public Long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
