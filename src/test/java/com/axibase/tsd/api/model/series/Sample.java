package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.Util;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

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
    public String toString() {
        return "Sample{" +
                "d='" + d + '\'' +
                ", t=" + t +
                ", v=" + v +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sample)) return false;

        Sample sample = (Sample) o;

        if (!Objects.equals(t, sample.t)) return false;
        if (getD() != null ? !getD().equals(sample.getD()) : sample.getD() != null) return false;
        return getV().equals(sample.getV());

    }

    @Override
    public int hashCode() {
        int result = getD() != null ? getD().hashCode() : 0;
        result = 31 * result + (int) (t ^ (t >>> 32));
        result = 31 * result + getV().hashCode();
        return result;
    }
}