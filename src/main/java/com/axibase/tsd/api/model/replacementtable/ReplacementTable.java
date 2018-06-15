package com.axibase.tsd.api.model.replacementtable;

import com.axibase.tsd.api.util.Registry;

import java.util.HashMap;
import java.util.Map;

public class ReplacementTable {
    private String name = null;
    private Map<String, String> keys = new HashMap<>();


    public ReplacementTable(String name, Map<String, String> keys) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
        this.keys = keys;
    }

    public ReplacementTable(String name) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getKeys() {
        return keys;
    }

    public void addValue(String key, String value){
        keys.put(key, value);
    }
}
