package com.axibase.tsd.api.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthorizationType {
    BEARER("Bearer"),
    BASIC("Basic");

    private final String name;
}
