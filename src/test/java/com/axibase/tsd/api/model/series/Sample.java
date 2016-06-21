package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.Model;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sample extends Model {
    private String d;
    private BigDecimal v;

    public Sample(long t, String v) {
        this.d = Util.ISOFormat(t);
        this.v = new BigDecimal(v);
    }

    public Sample(String d, int v) {
        this.d = d;
        this.v = new BigDecimal(String.valueOf(v));
    }

    public Sample(String d, String v) {
        this.d = d;
        this.v = new BigDecimal(String.valueOf(v));
    }

    public String getD() {
        return d;
    }

    public BigDecimal getV() {
        return v;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "d='" + d + '\'' +
                ", v='" + v + '\'' +
                '}';
    }
}
