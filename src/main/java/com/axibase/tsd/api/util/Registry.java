package com.axibase.tsd.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

public enum Registry {
    Entity("Entity"), Metric("Metric"), Type("Type"), EntityGroup("EntityGroup"), ReplacementTable("ReplacementTable");
    final static String ERROR_ALREADY_REGISTRED_TPL = "REGISTRY ERROR: %s=%s already registered.";
    final static String ERROR_HAS_REGISTRED_PREFIX_TPL = "REGISTRY ERROR: %s=%s has registered prefix.";
    final static String ERROR_PREFIX_ALREADY_REGISTRED_TPL = "REGISTRY ERROR: %s prefix \"%s\" already registered.";
    final static String ERROR_VALUE_WITH_PREFIX_EXIST_TPL = "REGISTRY ERROR: %s registry has already values with prefix \"%s\".";

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String registryType;
    private Set<String> registeredSet = new HashSet<>();
    private Set<String> registeredPrefixSet = new HashSet<>();

    Registry(String registryType) {
        this.registryType = registryType;
    }

    public void register(String value) {
        if (registeredSet.contains(value)) {
            throw new IllegalArgumentException(String.format(ERROR_ALREADY_REGISTRED_TPL, registryType, value));
        }
        if (hasRegisteredPrefix(value)) {
            throw new IllegalArgumentException(String.format(ERROR_HAS_REGISTRED_PREFIX_TPL, registryType, value));
        }
        registeredSet.add(value);
    }

    public void registerPrefix(String valuePrefix) {
        if (registeredPrefixSet.contains(valuePrefix)) {
            throw new IllegalArgumentException(String.format(ERROR_PREFIX_ALREADY_REGISTRED_TPL, registryType, valuePrefix));
        }
        if (hasRegisteredValueWithPrefix(valuePrefix)) {
            throw new IllegalArgumentException(String.format(ERROR_VALUE_WITH_PREFIX_EXIST_TPL, registryType, valuePrefix));
        }
        registeredPrefixSet.add(valuePrefix);
    }

    private boolean hasRegisteredValueWithPrefix(String prefix) {
        for (String value : registeredSet) {
            if (value.startsWith(prefix)) {
                logger.debug("value \"{}\" starts with prefix \"{}\"", value, prefix);
                return true;
            }
        }
        return false;
    }

    private boolean hasRegisteredPrefix(String value) {
        for (String prefix : registeredPrefixSet) {
            if (value.startsWith(prefix)) {
                logger.debug("value \"{}\" starts with prefix \"{}\"", value, prefix);
                return true;
            }
        }
        return false;
    }

}
