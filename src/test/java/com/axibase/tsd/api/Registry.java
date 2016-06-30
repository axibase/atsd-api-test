package com.axibase.tsd.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Korchagin.
 */
public enum Registry {
    Entity("Entity"), Metric("Metric"), Type("Type");

    private String registryType;
    private Set<String> registeredSet = new HashSet<>();
    private Set<String> registeredPrefixSet = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    Registry(String registryType) {
        this.registryType = registryType;
    }

    public void register(String value) {
        if (registeredSet.contains(value)) {
            throw new IllegalArgumentException("REGISTRY ERROR: " + registryType + "=" + value + " already registered.");
        }
        if (hasRegisteredPrefix(value)) {
            throw new IllegalArgumentException("REGISTRY ERROR: " + registryType + "=" + value + " has registered prefix.");
        }
        registeredSet.add(value);
    }

    public void registerPrefix(String valuePrefix) {
        if (registeredPrefixSet.contains(valuePrefix)) {
            throw new IllegalArgumentException("REGISTRY ERROR: " + registryType + " prefix \"" + valuePrefix + "\" already registered.");
        }
        if (hasRegisteredValueWithPrefix(valuePrefix)) {
            throw new IllegalArgumentException("REGISTRY ERROR: " + registryType + " registry has already values with prefix \"" + valuePrefix + "\".");
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
