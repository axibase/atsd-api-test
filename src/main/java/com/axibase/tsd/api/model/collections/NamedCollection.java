package com.axibase.tsd.api.model.collections;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Data
@RequiredArgsConstructor
public class NamedCollection {
    private final String name;
    private final Collection<String> items;
}
