package com.axibase.tsd.api.util;

import java.text.DecimalFormat;

public class ScientificNotationNumber {
    private final String scientificNotation;

    public ScientificNotationNumber(double number) {
        this.scientificNotation = new DecimalFormat("0.0E0").format(number);
    }

    @Override
    public String toString() {
        return scientificNotation;
    }
}
