package com.axibase.tsd.api.util;

public class ScientificNotationNumber {
    private final String scientificNotation;

    public ScientificNotationNumber(double number) {
        int eRank = 0;
        if(number > 10) {
            while(number >= 10) {
                number /= 10;
                eRank++;
            }
        } else if(number < 1) {
            while(number < 1) {
                number *= 10;
                eRank++;
            }
        }
        this.scientificNotation = String.format("%fe%d", number, eRank);
    }

    @Override
    public String toString() {
        return scientificNotation;
    }
}
