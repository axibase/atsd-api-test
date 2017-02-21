package com.axibase.tsd.api.model.version;


public enum ProductVersion {
    COMMUNITY("Community Edition"), ENTERPRISE("Enterprise Edition");

    private String text;

    ProductVersion(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
