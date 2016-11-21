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

    public Sample() {
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

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public BigDecimal getV() {
        return v;
    }

    public void setV(BigDecimal v) {
        this.v = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        if (d != null ? !d.equals(sample.d) : sample.d != null) return false;
        if (t != null ? !t.equals(sample.t) : sample.t != null) return false;
        return v != null ? v.equals(sample.v) : sample.v == null;

    }

    @Override
    public int hashCode() {
        int result = d != null ? d.hashCode() : 0;
        result = 31 * result + (t != null ? t.hashCode() : 0);
        result = 31 * result + (v != null ? v.hashCode() : 0);
        return result;
    }
}