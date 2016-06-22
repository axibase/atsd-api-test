package com.axibase.tsd.api;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Dmitry Korchagin.
 */
public enum Registry {
    Entity("Entity"), Metric("Metric"), Type("Type");

    private String registryType;
    private Set<String> registredSet = new TreeSet<>();

    public void register(String value) {
        if (registredSet.contains(value)) {
            throw new IllegalArgumentException("REGISTRY ERROR: " + getRegisterType() + "=" + value + " already registered.");
        }
        registredSet.add(value);
    }

    private String getRegisterType() {
        return registryType;
    }

    Registry(String registryType) {
        this.registryType = registryType;
    }

}
