package com.axibase.tsd.api.method.sql.function.math;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.junit.Test;

public class SqlFunctionRoundTest extends SqlTest {

    @Test
    public void test() throws Exception {
        String sql = "SELECT 1774*0.005,\n" +
                "  1774.0*0.005,\n" +
                "  1774.0*'0.005',\n" +
                "  ROUND(1774.3,0)*0.005," +
                "  ROUND(1774.3)*0.005," +
                "  ROUND(1774,0)*0.005";
        String[][] expected = new String[][]{
                {"8.87", "8.87", "8.87", "8.87", "8.87", "8.87"}
        };
        assertSqlQueryRows(expected, sql);
    }
}