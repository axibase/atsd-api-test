package com.axibase.tsd.api.method.sql.meta;

import com.axibase.tsd.api.method.sql.SqlMetaMethod;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Test;

public class MetaTest extends SqlMetaMethod {

    @Test
    public void testAtsdSeriesMeta() {
        String sqlQuery = "SELECT * from 'atsd_series'";

        queryMetaData(sqlQuery);
    }

    @Test
    public void testNonExistentMeta() {
        String nonExistentMetric = Mocks.metric();
        String sqlQuery = String.format("SELECT * from '%s'", nonExistentMetric);

        queryMetaData(sqlQuery);
    }

    @Test
    public void testNonExistentCustomColumnsMeta() {
        String nonExistentMetric = Mocks.metric();
        String sqlQuery = String.format("SELECT value, text from '%s'", nonExistentMetric);

        queryMetaData(sqlQuery);
    }

    @Test
    public void testNonExistentJoinMeta() {
        String nonExistentMetric1 = Mocks.metric();
        String nonExistentMetric2 = Mocks.metric();
        String sqlQuery = String.format("SELECT * from '%s' JOIN '%s'", nonExistentMetric1, nonExistentMetric2);

        queryMetaData(sqlQuery);
    }

    @Test
    public void testWildcardSelect() {
        String sqlQuery = "SELECT value * ? " +
                "FROM table_size";

        queryMetaData(sqlQuery);
    }

    @Test
    public void testWildcardCase() {
        String sqlQuery = "SELECT tags.table, case ? when ? then text else text end " +
                "FROM table_size " +
                "WHERE datetime < ? ORDER BY tags.table " +
                "GROUP BY value * ?";

        queryMetaData(sqlQuery);
    }

    @Test
    public void testWildcard() {
        String sqlQuery = "SELECT tags.table, AVG(value) " +
                "FROM table_size " +
                "WHERE datetime < ? GROUP BY tags.table";

        queryMetaData(sqlQuery);
    }

}
