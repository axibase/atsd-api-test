package com.axibase.tsd.api;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Korchagin.
 */
public enum Registry {
    Entity("Entity"), Metric("Metric"), Type("Type");

    private String registryType;
    private Set<String> registeredSet = new HashSet<>();

    Registry(String registryType) {
        this.registryType = registryType;
    }

    public void register(String value) {
        if (registeredSet.contains(value)) {
            throw new IllegalArgumentException("REGISTRY ERROR: " + getRegisterType() + "=" + value + " already registered.");
        }
        registeredSet.add(value);
    }

    private String getRegisterType() {
        return registryType;
    }

}
