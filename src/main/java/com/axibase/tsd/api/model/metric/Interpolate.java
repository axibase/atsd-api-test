package com.axibase.tsd.api.model.metric;


public enum Interpolate {
    LINEAR, PREVIOUS;
    private String text;

    Interpolate() {
        this.text = this.name().toLowerCase();
    }


    @Override
    public String toString() {
        return text;
    }
}
