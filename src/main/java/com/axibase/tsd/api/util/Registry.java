package com.axibase.tsd.api.util;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.method.replacementtable.ReplacementTableMethod;

public enum Registry {
    Entity("Entity"), Metric("Metric"), Type("Type"), EntityGroup("EntityGroup"), ReplacementTable("ReplacementTable");
    final static String ERROR_ALREADY_REGISTRED_TPL = "REGISTRY ERROR: %s=%s already registered.";
    private String registryType;

    Registry(String registryType) {
        this.registryType = registryType;
    }

    public synchronized void checkExists(String value) {
    }
}
