package com.axibase.tsd.api.model.sql;


public class ColumnMetaData implements Comparable<ColumnMetaData> {
    private String name;
    private Integer columnIndex;
    private String table;
    private String dataType;
    private String propertyUrl;
    private String titles;


    public ColumnMetaData(String name, Integer columnIndex) {
        this.name = name;
        this.columnIndex = columnIndex;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    private void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

    public String getName() {
        return name;
    }

    public String getTitles() {
        return titles;
    }

    public void setTitles(String titles) {
        this.titles = titles;
    }

    public String getPropertyUrl() {
        return propertyUrl;
    }

    public void setPropertyUrl(String propertyUrl) {
        this.propertyUrl = propertyUrl;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public int compareTo(ColumnMetaData o) {
        return this.columnIndex.compareTo(o.getColumnIndex());
    }
}
