package com.axibase.tsd.api.model.replacementtable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SupportedFormat {
    TEXT("TEXT"),
    JSON("JSON"),
    SQL("SQL"),
    GRAPHQL("GRAPHQL"),
    LIST("LIST");

    private String name;
}
