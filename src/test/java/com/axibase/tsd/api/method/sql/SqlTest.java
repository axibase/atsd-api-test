package com.axibase.tsd.api.method.sql;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlTest extends SqlMethod {
    public static void assertTableRows(List<List<String>> row1, List<List<String>> row2) {
        assertEquals("Table rows  must  be identical", row1, row2);
    }
}
