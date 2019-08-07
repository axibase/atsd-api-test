package com.axibase.tsd.api.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.text.DecimalFormat;

@RequiredArgsConstructor
public class ScientificNotationNumber extends Number {
    @Delegate
    private final Number delegate;

    @Override
    public String toString() {
        return new DecimalFormat("0.0E0").format(delegate);
    }
}