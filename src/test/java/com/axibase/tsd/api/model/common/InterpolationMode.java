package com.axibase.tsd.api.model.common;


public enum InterpolationMode {
    LINEAR, PREVIOUS;
    private String text;

    InterpolationMode() {
        this.text = this.name().toLowerCase();
    }


    @Override
    public String toString() {
        return text;
    }
}
