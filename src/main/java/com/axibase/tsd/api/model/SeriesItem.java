package com.axibase.tsd.api.model;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SeriesItem {
    public final ZonedDateTime date;
    public final int value;

    public SeriesItem(String date, int value) throws ParseException {
        this.date = ZonedDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        this.value = value;
    }
}
