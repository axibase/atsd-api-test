package com.axibase.tsd.api.model.replacementtable;

import com.axibase.tsd.api.util.Registry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReplacementTable {
    private String name;
    private String description;
    private String author;
    private SupportedFormat valueFormat;
    private Map<String, String> keys = new HashMap<>();

    public static ReplacementTable of(String name, SupportedFormat format) {
        Registry.ReplacementTable.checkExists(name);
        return new ReplacementTable().setName(name).setValueFormat(format);
    }

    public ReplacementTable addValue(String key, String value) {
        keys.put(key, value);
        return this;
    }
}
