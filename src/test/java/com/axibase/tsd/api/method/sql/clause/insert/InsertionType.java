package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.DataProvider;

import java.util.Map;

public enum InsertionType {
    INSERT_INTO{
        @Override
        public String insertionQuery(String tableName, Map<String, Object> columns) {
            String columnNames = "(";
            String columnValues = "(";
            int i = 0;
            for (Map.Entry<String, Object> entry: columns.entrySet()) {
                columnNames += entry.getKey();
                columnValues += encloseInQuotes(entry.getValue());
                i++;
                if(i != columns.entrySet().size()) {
                    columnNames += ", ";
                    columnValues += ", ";
                }
            }
            columnNames += ")";
            columnValues += ")";
            return String.format("INSERT INTO \"%s\"%s VALUES%s", tableName, columnNames, columnValues);
        }
    },
    UPDATE{
        @Override
        public String insertionQuery(String tableName, Map<String, Object> columns) {
            String keysAndValues = "";
            int i = 0;
            for(Map.Entry<String, Object> entry: columns.entrySet()) {
                keysAndValues += TestUtil.devideWithEqualityMark(entry.getKey(), encloseInQuotes(entry.getValue()));
                i++;
                if(i != columns.entrySet().size()) {
                    keysAndValues += ", ";
                }
            }
            return String.format("UPDATE \"%s\" SET %s", tableName, keysAndValues);
        }
    };

    @DataProvider
    public static Object[][] insertionType() {
        return TestUtil.convertTo2DimArray(values());
    }

    public abstract String insertionQuery(String tableName, Map<String, Object> columns);


    protected String encloseInQuotes(Object object) {
        if(object.getClass().getSuperclass().getSimpleName().equals("Number") || object.getClass().getSimpleName().equals("ScientificNotationNumber")) {
            return object.toString();
        } else {
            return String.format("'%s'", object.toString());
        }
    }
}
