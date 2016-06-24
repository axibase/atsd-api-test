package com.axibase.tsd.api.model.sql;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.IllegalFormatException;

/**
 * Created by shmgrinsky on 23.06.16.
 */
public interface Table<K, V> {


    public void addColumn(K columnName);

    public void addRow(V row);

    public K getColumnName(int index);

    public V getRow(int index);
}
