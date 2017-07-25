package com.axibase.tsd.api.model;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateRange {
    public final ZonedDateTime startDate;
    public final ZonedDateTime endDate;

    public DateRange(String startDate, String endDate) throws ParseException {
        this.startDate = ZonedDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
        this.endDate = ZonedDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
    }

    public DateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public int hashCode() {
        return startDate.hashCode() ^ endDate.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof DateRange))return false;
        DateRange otherRange = (DateRange)other;
        return this.startDate.equals(otherRange.startDate) &&
                this.endDate.equals(otherRange.endDate);
    }
}