package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.util.Util;

import java.util.Date;


public class TextSample extends Sample {
    public TextSample(Date date, String text) {
        this(Util.ISOFormat(date), text);
    }

    public TextSample(String isoDate, String text) {
        setD(isoDate);
        setText(text);
    }

    public TextSample(Long millsTime, String text) {
        setT(millsTime);
        setText(text);
    }
}
