package com.axibase.tsd.api.model.replacementtable;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ReplacementTable {
    private String name;
    private String description;
    private String author;
    private SupportedFormat format;
    private Map<String, String> keys = new HashMap<>();

    public ReplacementTable(String name) {
        this.name = name;
    }

    public void addValue(String key, String value){
        keys.put(key, value);
    }
}
