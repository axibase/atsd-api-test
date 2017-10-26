package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Sample {
    private static final ISO8601DateFormat isoDateFormat = new ISO8601DateFormat();

    @JsonProperty("d")
    private String rawDate;

    @JsonProperty("t")
    private Long unixTime;

    @JsonDeserialize(using = ValueDeserializer.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonProperty("v")
    private BigDecimal value;

    @JsonProperty("x")
    private String text;

    private SampleVersion version;

    private Sample(Long unixTime, String date, BigDecimal value, String text) {
        this.unixTime = unixTime;
        if (date != null) {
            this.rawDate = convertDateToISO(date);
        }
        this.value = value;
        this.text = text;
    }

    public Sample copy() {
        return new Sample(unixTime, null, value, text).setRawDate(rawDate);
    }

    public static Sample ofDate(String date) {
        return new Sample(null, date, null, null);
    }

    public static Sample ofDateIntegerText(String date, int value, String text) {
        return new Sample(null, date, BigDecimal.valueOf(value), text);
    }

    public static Sample ofDateDecimalText(String date, BigDecimal value, String text) {
        return new Sample(null, date, value, text);
    }

    public static Sample ofDateInteger(String date, int value) {
        return new Sample(null, date, BigDecimal.valueOf(value), null);
    }

    public static Sample ofRawDateInteger(String date, int value) {
        return new Sample(null, null, BigDecimal.valueOf(value), null).setRawDate(date);
    }

    public static Sample ofDateDecimal(String date, BigDecimal value) {
        return new Sample(null, date, value, null);
    }

    public static Sample ofTimeInteger(long time, int value) {
        return new Sample(time, null, BigDecimal.valueOf(value), null);
    }

    public static Sample ofTimeDecimal(long time, BigDecimal value) {
        return new Sample(time, null, value, null);
    }

    public static Sample ofJavaDateInteger(Date d, int v) {
        return new Sample(null, Util.ISOFormat(d), BigDecimal.valueOf(v), null);
    }

    public static Sample ofDateText(String date, String text) {
        return new Sample(null, date, null, text);
    }

    private String convertDateToISO(String dateString) {
        Date date;
        try {
            date = isoDateFormat.parse(dateString);
        } catch (ParseException ex) {
            return null;
        }
        return Util.ISOFormat(date, true, Util.DEFAULT_TIMEZONE_NAME);
    }

    @JsonIgnore
    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.parse(this.rawDate, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
