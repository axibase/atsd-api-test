package com.axibase.tsd.api.model.version;


public enum ProductVersion {
    STANDARD("Standard Edition"), ENTERPRISE("Enterprise Edition");

    private String text;

    ProductVersion(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static ProductVersion fromString(final String productVersion) {
        if (ProductVersion.STANDARD.toString().equals(productVersion)) {
            return ProductVersion.STANDARD;
        } else {
            if (ProductVersion.ENTERPRISE.toString().equals(productVersion)) {
                return ProductVersion.ENTERPRISE;
            } else {
                throw new IllegalStateException("Incorrect Product version");
            }
        }
    }
}
