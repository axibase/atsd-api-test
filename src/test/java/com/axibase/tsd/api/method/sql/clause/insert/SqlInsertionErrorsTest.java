package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

public class SqlInsertionErrorsTest extends SqlTest {
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    @Test(
            description = "Tests that if one of the declared parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetDeclaredParameterInsertion() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, not_set) VALUES('%s', '%s', %d)"
                ,Mocks.metric(), Mocks.entity(), ISO_TIME, VALUE);
        assertBadSqlRequest("Invalid SQL query with unset declared parameter was accepted!", sqlQuery);
    }

    @Test(
            description = "Tests that if one of the required parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetRequiredParameterInsertion() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime) VALUES('%s','%s')"
                ,Mocks.metric(), Mocks.entity(), ISO_TIME);
        assertBadSqlRequest("Invalid SQL query with unset required parameter was accepted!", sqlQuery);
    }
}
