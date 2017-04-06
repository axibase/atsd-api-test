package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.Test;

public class Select1Test extends SqlTest {
    /**
     * #4067
     */
    @Test
    public void testSelectOne() {
        String sqlQuery = "SELECT 1";
        String[][] expectedRows = {
                {"1"}
        };
        assertSqlQueryRows("'SELECT 1' query should return one row with symbol '1'", expectedRows, sqlQuery);
    }

    /**
     * #4067
     */
    @Test
    public void testSelectNumber() {
        String twelve = "12";
        String sqlQuery = "SELECT " + twelve;
        String[][] expectedRows = {
                {twelve}
        };
        assertSqlQueryRows("'SELECT number' query should return one row with that number", expectedRows, sqlQuery);
    }

    /**
     * #4067
     */
    @Test
    public void testSelectNumberSum() {
        String twelve = "12";
        String fourty = "40";
        String sqlQuery = "SELECT " + twelve + "+" + fourty;
        String[][] expectedRows = {
                {"52"}
        };
        assertSqlQueryRows("'SELECT number' query should return one row with that number", expectedRows, sqlQuery);
    }
}
