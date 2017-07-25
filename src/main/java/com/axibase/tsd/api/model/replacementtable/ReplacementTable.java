package com.axibase.tsd.api.model.replacementtable;

import com.axibase.tsd.api.util.Registry;

import java.util.HashMap;
import java.util.Map;

public class ReplacementTable {
    private String name = null;
    private Map<String, String> map = new HashMap<>();


    public ReplacementTable(String name, Map<String, String> map) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
        this.map = map;
    }

    public ReplacementTable(String name) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void addValue(String key, String value){
        map.put(key, value);
    }
}
