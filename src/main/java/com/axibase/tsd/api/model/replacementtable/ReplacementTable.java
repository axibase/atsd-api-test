package com.axibase.tsd.api.model.replacementtable;

import com.axibase.tsd.api.util.Registry;

import java.util.HashMap;
import java.util.Map;

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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public SupportedFormat getFormat() {
        return format;
    }

    public void setFormat(SupportedFormat format) {
        this.format = format;
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
