package com.axibase.tsd.api.model.collections;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.Collection;

@Data
@RequiredArgsConstructor
public class NamedCollection implements Collection<String> {
    private final String name;
    @Delegate
    private final Collection<String> collection;
}
