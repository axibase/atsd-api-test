package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.sql.SqlMetaTest;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

public class NegativeIntegerTest extends SqlMetaTest {
    @Issue("4444")
    @Test(
            description = "Test that negative integers have 'bigint' data type"
    )
    public void testNegativeIntegerType() {
        String sqlQuery = "SELECT 1, 0, -1, -0, -9223372036854775807";

        String[] expectedNames = {"1", "0", "-1", "-0", "-9223372036854775807"};
        String[] expectedTypes = {"bigint", "bigint", "bigint", "bigint", "bigint"};

        assertSqlMetaNamesAndTypes("Wrong data type for negative integers", expectedNames, expectedTypes, sqlQuery);
    }
}
