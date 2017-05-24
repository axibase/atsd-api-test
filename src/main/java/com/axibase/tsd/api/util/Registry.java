package com.axibase.tsd.api.util;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;

public enum Registry {
    Entity("Entity"), Metric("Metric"), Type("Type"), EntityGroup("EntityGroup"), ReplacementTable("ReplacementTable");
    final static String ERROR_ALREADY_REGISTRED_TPL = "REGISTRY ERROR: %s=%s already registered.";
    private String registryType;

    Registry(String registryType) {
        this.registryType = registryType;
    }

    public synchronized void register(String value) {
        boolean exists;
        switch (registryType) {
            case "Entity":
                exists = EntityMethod.entityExist(value);
                break;
            case "Metric":
                exists = MetricMethod.metricExist(value);
                break;
            case "Type":
                exists = PropertyMethod.propertyTypeExist(value);
                break;
            case "EntityGroup":
                exists = EntityGroupMethod.entityGroupExist(value);
                break;
            case "ReplacementTable":
                // todo
            default:
                exists = true;
        }

        if (exists) {
            throw new IllegalArgumentException(String.format(ERROR_ALREADY_REGISTRED_TPL, registryType, value));
        }
    }
}
