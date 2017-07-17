package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnMetaData implements Comparable<ColumnMetaData> {

    private String name;
    private Integer columnIndex;
    private String table;

    @JsonDeserialize(using = StringObjectDeserializer.class)
    @JsonProperty("datatype")
    private String dataType;

    private String propertyUrl;
    private String titles;

    @Override
    public int compareTo(ColumnMetaData o) {
        return columnIndex.compareTo(o.columnIndex);
    }
}
