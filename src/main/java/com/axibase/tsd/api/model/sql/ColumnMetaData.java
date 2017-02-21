package com.axibase.tsd.api.model.sql;


import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnMetaData)) return false;
        ColumnMetaData that = (ColumnMetaData) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getColumnIndex(), that.getColumnIndex()) &&
                Objects.equals(getTable(), that.getTable()) &&
                Objects.equals(getDataType(), that.getDataType()) &&
                Objects.equals(getPropertyUrl(), that.getPropertyUrl()) &&
                Objects.equals(getTitles(), that.getTitles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getColumnIndex(), getTable(), getDataType(), getPropertyUrl(), getTitles());
    }

    @Override
    public int compareTo(ColumnMetaData o) {
        return this.columnIndex.compareTo(o.getColumnIndex());
    }
}
