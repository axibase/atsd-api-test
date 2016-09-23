package com.axibase.tsd.api.method.version;

/**
 * Created by shmagrinskiy on 19.09.16.
 */
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
