package com.axibase.tsd.api.model.metric;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MetricRenameQuery {
    private final String name;
}
