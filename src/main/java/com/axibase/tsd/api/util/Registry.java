package com.axibase.tsd.api.util;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.method.replacementtable.ReplacementTableMethod;

import java.util.function.Predicate;

public enum Registry {
    Entity(EntityMethod::entityExist),
    Metric(MetricMethod::metricExist),
    Type(PropertyMethod::propertyTypeExist),
    EntityGroup(EntityGroupMethod::entityGroupExist),
    ReplacementTable(ReplacementTableMethod::replacementTableExist);

    private final Predicate<String> existenceChecker;

    Registry(Predicate<String> existenceChecker) {
        this.existenceChecker = existenceChecker;
    }

    public synchronized void checkExists(String value) {
        if (existenceChecker.test(value)) {
            throw new IllegalArgumentException("REGISTRY ERROR: " + name() + "=" + value + " already registered.");
        }
    }
}
