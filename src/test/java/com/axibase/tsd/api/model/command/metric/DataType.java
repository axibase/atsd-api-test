package com.axibase.tsd.api.model.command.metric;

public enum DataType {
    SHORT, INTEGER, LONG, FLOAT, DOUBLE, DECIMAL;
    private String text;

    DataType() {
        this.text = this.name();
    }


    @Override
    public String toString() {
        return text;
    }
}
