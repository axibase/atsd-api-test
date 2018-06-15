package com.axibase.tsd.api.model.replacementtable;

import com.axibase.tsd.api.util.Registry;
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

    public ReplacementTable(String name, Map<String, String> keys) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
        this.keys = keys;
    }

    public ReplacementTable(String name) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
    }

    public ReplacementTable(String name, String author, String description) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
        this.author = author;
        this.description = description;
    }

    public ReplacementTable(String name, String description, String author, SupportedFormat format, Map<String, String> keys) {
        Registry.ReplacementTable.checkExists(name);
        this.name = name;
        this.description = description;
        this.author = author;
        this.format = format;
        this.keys = keys;
    }

    public void addValue(String key, String value){
        keys.put(key, value);
    }
}
