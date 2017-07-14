package com.axibase.tsd.api.model.sql;

import com.axibase.tsd.api.model.sql.ColumnMetaData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize(using = TableMetaDataDeserializer.class)
public class TableMetaData {
    private ColumnMetaData[] columnsMeta;

    public TableMetaData(ColumnMetaData[] columnsMeta) {
        if (columnsMeta == null || columnsMeta.length == 0) {
            throw new IllegalArgumentException("Null or empty meta data");
        }
        this.columnsMeta = columnsMeta;
    }

    public ColumnMetaData getColumnMeta(int index) {
        if (index < 0 || index >= columnsMeta.length) {
            throw new IllegalStateException("Table doesn't contain column with index " + index);
        }
        return columnsMeta[index];
    }

    public List<ColumnMetaData> asList() {
        return Arrays.asList(columnsMeta);
    }

    public int size() {
        return columnsMeta.length;
    }
}
