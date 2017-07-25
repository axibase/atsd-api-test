package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.Test;

public class SelectOneTest extends SqlTest {
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
        String sqlQuery = "SELECT 12";
        String[][] expectedRows = {
                {"12"}
        };
        assertSqlQueryRows("'SELECT number' query should return one row with that number", expectedRows, sqlQuery);
    }

    /**
     * #4067
     */
    @Test
    public void testSelectNumberSum() {
        String sqlQuery = "SELECT 12 + 40";
        String[][] expectedRows = {
                {"52"}
        };
        assertSqlQueryRows("'SELECT number' query should return one row with that number", expectedRows, sqlQuery);
    }

    /**
     * #4067
     */
    @Test
    public void testSelectComplexMath() {
        String sqlQuery = "SELECT (1 + 2) * 3";
        String[][] expectedRows = {
                {"9"}
        };
        assertSqlQueryRows("'SELECT number' query should return one row with that number", expectedRows, sqlQuery);
    }

    /**
     * #4067
     */
    @Test
    public void testSelectNumberDividedByNull() {
        String sqlQuery = "SELECT 1/0";
        String[][] expectedRows = {
                {"Infinity"}
        };
        assertSqlQueryRows("'SELECT number' query should return one row with that number", expectedRows, sqlQuery);
    }
}
