package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.axibase.tsd.api.util.Util.DEFAULT_TIMEZONE_NAME;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sample {
    private static final ISO8601DateFormat isoDateFormat = new ISO8601DateFormat();

    private String d;
    private Long t;

    @JsonDeserialize(using = ValueDeserializer.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private BigDecimal v;
    private String text;
    private SampleVersion version;

    public Sample() {
    }

    public Sample(String d, int v, String text) {
        this.d = convertDateToISO(d);
        this.v = new BigDecimal(v);
        this.text = text;
    }

    public Sample(String d, BigDecimal v, String text) {
        this.d = convertDateToISO(d);
        this.v = v;
        this.text = text;
    }

    public Sample(Sample sourceSample) {
        this(sourceSample.getD(), sourceSample.getV());
        setT(sourceSample.getT());
        setText(sourceSample.getText());
    }

    public Sample(String d, int v) {
        this.d = convertDateToISO(d);
        this.v = new BigDecimal(v);
    }

    public Sample(String d, BigDecimal v) {
        this.d = convertDateToISO(d);
        this.v = v;
    }

    public Sample(Date d, BigDecimal v) {
        this.d = Util.ISOFormat(d);
        this.v = v;
    }

    public Sample(Date d, int v) {
        this.d = Util.ISOFormat(d);
        this.v = new BigDecimal(v);
    }

    private String convertDateToISO(String dateString) {
        Date date;
        try {
            date = isoDateFormat.parse(dateString);
        } catch (ParseException ex) {
            return null;
        }
        return Util.ISOFormat(date, true, DEFAULT_TIMEZONE_NAME);
    }

    public Long getT() {
        return t;
    }

    protected void setT(Long t) {
        this.t = t;
    }

    public String getD() {
        return d;
    }

    @JsonIgnore
    public ZonedDateTime getZonedDateTime() { return ZonedDateTime.parse(this.d, DateTimeFormatter.ISO_DATE_TIME); }

    protected void setD(String d) {
        this.d = convertDateToISO(d);
    }

    protected void setDUnsafe(String d) {
        this.d = d;
    }

    public BigDecimal getV() {
        return v;
    }

    protected void setV(BigDecimal v) {
        this.v = v;
    }


    public String getText() {
        return text;
    }

    @JsonProperty("x")
    protected void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (!Sample.class.isInstance(o)))
            return false;

        Sample sample = (Sample) o;

        if (d != null ? !d.equals(convertDateToISO(sample.d)) : sample.d != null)
            return false;
        if (t != null ? !t.equals(sample.t) : sample.t != null)
            return false;
        if (v != null ? !(v.compareTo(sample.v) == 0) : sample.v != null)
            return false;
        if (text != null ? !text.equals(sample.text) : sample.text != null)
            return false;
        return version != null ? version.equals(sample.version) : sample.version == null;
    }

    @Override
    public int hashCode() {
        int result = d != null ? d.hashCode() : 0;
        result = 31 * result + (t != null ? t.hashCode() : 0);
        result = 31 * result + (v != null ? v.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    public SampleVersion getVersion() {
        return version;
    }
}
