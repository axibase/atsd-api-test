package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.serialization.ValueDeserializer;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

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
    @JsonProperty("v")
    private BigDecimal value;

    @JsonDeserialize(using = ValueDeserializer.class)
    @JsonProperty("s")
    private BigDecimal deviation;

    @JsonProperty("x")
    private String text;

    private SampleVersion version;

    private Sample(Long unixTime, String date, BigDecimal value, String text, boolean convertDateToISOFormat) {
        if (unixTime == null && date == null) {
            throw new IllegalArgumentException("Timestamp is required to create a sample instance.");
        }
        this.unixTime = unixTime;
        this.rawDate = convertDateToISOFormat && date != null
                ? convertDateToISO(date)
                : date;
        this.value = value;
        this.text = text;
    }

    public Sample copy() {
        return new Sample(unixTime, rawDate, value, text, false);
    }

    public static Sample ofDate(String date) {
        return new Sample(null, date, null, null, true);
    }

    public static Sample ofDateIntegerText(String date, int value, String text) {
        return new Sample(null, date, BigDecimal.valueOf(value), text, true);
    }

    public static Sample ofDateDecimalText(String date, BigDecimal value, String text) {
        return new Sample(null, date, value, text, true);
    }

    public static Sample ofDateInteger(String date, int value) {
        return new Sample(null, date, BigDecimal.valueOf(value), null, true);
    }

    public static Sample ofRawDateInteger(String date, int value) {
        return new Sample(null, null, BigDecimal.valueOf(value), null, false);
    }

    public static Sample ofDateDecimal(String date, BigDecimal value) {
        return new Sample(null, date, value, null, true);
    }

    public static Sample ofTimeInteger(long time, int value) {
        return new Sample(time, null, BigDecimal.valueOf(value), null, true);
    }

    public static Sample ofTimeDecimal(long time, BigDecimal value) {
        return new Sample(time, null, value, null, true);
    }

    public static Sample ofJavaDateInteger(Date d, int v) {
        return new Sample(null, Util.ISOFormat(d), BigDecimal.valueOf(v), null, true);
    }

    public static Sample ofJavaDateInteger(final ZonedDateTime d, final int v) {
        return new Sample(null, d.format(DateTimeFormatter.ISO_DATE_TIME), BigDecimal.valueOf(v), null, true);
    }

    public static Sample ofJavaDateInteger(final ZonedDateTime d, final int v, final String text) {
        return new Sample(null, d.format(ISO_ZONED_DATE_TIME), BigDecimal.valueOf(v), text, true);
    }

    public static Sample ofDateText(String date, String text) {
        return new Sample(null, date, null, text, true);
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
        if (this.rawDate != null) {
            return ZonedDateTime.parse(this.rawDate, DateTimeFormatter.ISO_DATE_TIME);
        }
        return Util.fromMillis(this.unixTime);
    }

    @JsonIgnore
    /** Return timestamp translated to epoch milliseconds.
     * Use the {@link #rawDate} if the {@link #unixTime} is null.*/
    public long getEpochMillis() {
        if (this.unixTime == null) {
            return Util.getUnixTime(this.rawDate);
        }
        return this.unixTime;
    }

    public static List<Sample> withOffset(final TemporalUnit unit, final int offset,
                                          final ZonedDateTime start, final ZonedDateTime end) {
        final List<Sample> samples = new ArrayList<>();
        int value = 1;
        for (ZonedDateTime current = start; current.compareTo(end) < 0; current = current.plus(offset, unit), value++) {
            samples.add(Sample.ofJavaDateInteger(current, value));
        }

        return samples;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }

    /**
     * Calculate epoch time from the samples time fields, and compare calculated epochs.
     * Compare values and deviations using {@link BigDecimal#compareTo(Object)}.
     */
    public boolean theSame(Sample sample) {
        return  (this.getEpochMillis() == sample.getEpochMillis()) &&
                !(this.value == null ^ sample.value == null) &&
                (this.value == null || this.value.compareTo(sample.value) == 0) &&
                !(this.deviation == null ^ sample.deviation == null) &&
                (this.deviation == null || this.deviation.compareTo(sample.deviation) == 0) &&
                !(this.text == null ^ sample.text == null) &&
                (this.text == null || this.text.equals(sample.text)) &&
                !(this.version == null ^ sample.version == null) &&
                (this.version == null || this.version.equals(sample.version));
    }
}
