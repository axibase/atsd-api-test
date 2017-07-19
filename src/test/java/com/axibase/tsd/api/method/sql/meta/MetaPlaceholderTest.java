package com.axibase.tsd.api.method.sql.meta;

import com.axibase.tsd.api.method.sql.SqlMetaTest;
import org.testng.annotations.Test;

public class MetaPlaceholderTest extends SqlMetaTest {
    @Test
    public void testWildcardSelect() {
        String sqlQuery = "SELECT value * ? " +
                "FROM table_size";

        queryMetaData(sqlQuery);
    }

    @Test
    public void testPlaceholderCase() {
        String sqlQuery = "SELECT tags.table, case ? when ? then text else text end " +
                "FROM table_size " +
                "WHERE datetime < ? GROUP BY tags.table " +
                "ORDER BY value * ?";

        queryMetaData(sqlQuery);
    }

    @Test
    public void testPlaceholder() {
        String sqlQuery = "SELECT tags.table, AVG(value) " +
                "FROM table_size " +
                "WHERE datetime < ? GROUP BY tags.table";

        queryMetaData(sqlQuery);
    }
}
