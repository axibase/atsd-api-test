package com.axibase.tsd.api.method.sql.clause.insertandupdate;

import com.axibase.tsd.api.util.ScientificNotationNumber;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.DataProvider;

import java.util.Map;
import java.util.stream.Collectors;

public enum InsertionType {
    INSERT_INTO {
        @Override
        public String insertionQuery(String tableName, Map<String, Object> columns) {
            String columnNames = String.join(", ", columns.keySet());
            String columnValues = columns.values().stream()
                    .map(this::encloseInQuotes)
                    .collect(Collectors.joining(", "));
            return String.format("INSERT INTO \"%s\"(%s) VALUES(%s)", tableName, columnNames, columnValues);
        }
    },
    UPDATE {
        @Override
        public String insertionQuery(String tableName, Map<String, Object> columns) {
            String keysAndValues = columns.entrySet()
                    .stream()
                    .map(e -> e.getKey() + "=" + encloseInQuotes(e.getValue()))
                    .collect(Collectors.joining(", "));
            return String.format("UPDATE \"%s\" SET %s", tableName, keysAndValues);
        }
    };

    @DataProvider
    public static Object[][] insertionType() {
        return TestUtil.convertTo2DimArray(values());
    }

    public abstract String insertionQuery(String tableName, Map<String, Object> columns);


    protected String encloseInQuotes(Object object) {
        if(object == null) {
            return "";
        }
        if (object instanceof Number) {
            return object.toString();
        } else {
            return String.format("'%s'", object);
        }
    }
}
