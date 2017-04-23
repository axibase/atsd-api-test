package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.Test;

public class Select1Test extends SqlTest {
    /**
     * #4067
     */
    @Test
    public void testFunctionResult() {
        String sqlQuery = "SELECT 1";
        String[][] expectedRows = {
                {"1"}
        };
        assertSqlQueryRows("'SELECT 1' query should return one row with symbol '1'", expectedRows, sqlQuery);
    }
}
