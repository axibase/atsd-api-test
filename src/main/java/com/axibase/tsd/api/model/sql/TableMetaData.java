package com.axibase.tsd.api.model.sql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize(using = TableMetaDataDeserializer.class)
public class TableMetaData {
    private ColumnMetaData[] columnsMeta;

    TableMetaData(ColumnMetaData[] columnsMeta) {
        if (columnsMeta == null) {
            throw new IllegalArgumentException("Null reference to meta data");
        }
        this.columnsMeta = columnsMeta;
    }

    ColumnMetaData getColumnMeta(int index) {
        if (index < 0 || index >= columnsMeta.length) {
            throw new IllegalStateException("Table doesn't contain column with index " + index);
        }
        return columnsMeta[index];
    }

    ColumnMetaData[] getColumnsMeta() {
        return Arrays.copyOf(columnsMeta, columnsMeta.length);
    }

    public List<ColumnMetaData> asList() {
        return Arrays.asList(columnsMeta);
    }

    public int size() {
        return columnsMeta.length;
    }
}
