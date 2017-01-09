package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sample {
    private String d;
    private Long t;
    private BigDecimal v;
    private String text;
    public Sample() {
    }

    public Sample(String d, BigDecimal v, String text) {
        this.d = d;
        this.v = v;
        this.text = text;
    }

    public Sample(Sample sourceSample) {
        this(sourceSample.getT(), sourceSample.getV());
        setD(sourceSample.getD());
        setText(sourceSample.getText());
    }

    public Sample(long t, String v) {
        this.t = t;
        this.v = new BigDecimal(String.valueOf(v));
    }

    public Sample(String d, int v) {
        this.d = d;
        this.v = new BigDecimal(String.valueOf(v));
    }

    public Sample(Date d, String v) {
        this.d = Util.ISOFormat(d);
        this.v = new BigDecimal(String.valueOf(v));
    }

    public Sample(Long t, BigDecimal v) {
        this.t = t;
        this.v = v;
    }

    public Sample(String d, BigDecimal v) {
        this.d = d;
        this.v = v;
    }

    @JsonCreator
    public Sample(@JsonProperty("d") String d, @JsonProperty("v") String v) {
        this.d = d;
        this.v = new BigDecimal(String.valueOf(v));
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

    protected void setD(String d) {
        this.d = d;
    }

    public BigDecimal getV() {
        return v;
    }

    protected void setV(BigDecimal v) {
        this.v = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        if (d != null ? !d.equals(sample.d) : sample.d != null) return false;
        if (t != null ? !t.equals(sample.t) : sample.t != null) return false;
        if (text != null ? !text.equals(sample.text) : sample.text != null) return false;
        return v != null ? v.equals(sample.v) : sample.v == null;

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
    public int hashCode() {
        int result = d != null ? d.hashCode() : 0;
        result = 31 * result + (t != null ? t.hashCode() : 0);
        result = 31 * result + (v != null ? v.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}