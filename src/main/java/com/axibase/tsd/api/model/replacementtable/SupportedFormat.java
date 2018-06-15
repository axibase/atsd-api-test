package com.axibase.tsd.api.model.replacementtable;

public enum SupportedFormat {
    TEXT("TEXT"),
    JSON("JSON"),
    SQL("SQL"),
    GRAPHQL("GRAPHQL"),
    LIST("LIST");

    private String name;

    SupportedFormat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
