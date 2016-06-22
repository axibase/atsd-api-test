package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.Util;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sample {
    private String d;
    private BigDecimal v;

    public Sample() {
    }

    public Sample(long t, String v) {
        this.d = Util.ISOFormat(t);
        this.v = new BigDecimal(v);
    }

    public Sample(String d, int v) {
        this.d = d;
        this.v = new BigDecimal(String.valueOf(v));
    }

    @JsonCreator
    public Sample(@JsonProperty("d") String d, @JsonProperty("v") String v) {
        this.d = d;
        this.v = new BigDecimal(String.valueOf(v));
    }


    public String getD() {
        return d;
    }

    public BigDecimal getV() {
        return v;
    }

    public void setD(String d) {
        this.d = d;
    }

    public void setV(BigDecimal v) {
        this.v = v;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "d='" + d + '\'' +
                ", v='" + v + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sample)) return false;

        Sample sample = (Sample) o;

        return getD().equals(sample.getD()) && getV().equals(sample.getV());

    }

    @Override
    public int hashCode() {
        int result = getD().hashCode();
        result = 31 * result + getV().hashCode();
        return result;
    }
}
