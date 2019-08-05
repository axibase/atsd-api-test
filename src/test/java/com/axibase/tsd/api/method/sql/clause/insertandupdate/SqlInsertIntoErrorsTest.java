package com.axibase.tsd.api.method.sql.clause.insertandupdate;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

public class SqlInsertIntoErrorsTest extends SqlTest {
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private static final InsertionType INSERTION_TYPE = InsertionType.INSERT_INTO;


    @Test(
            description = "Tests that if one of the declared parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetDeclaredParameterInsertion() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, not_set) VALUES('%s', '%s', %d)"
                ,Mocks.metric(), Mocks.entity(), ISO_TIME, VALUE);
        String assertMessage = "IllegalArgumentException: No value specified for column 'not_set'";
        assertBadRequest("Invalid SQL query with unset declared parameter was accepted!", assertMessage, sqlQuery);
    }

    @Test(
            description = "Tests that if one of the required parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetRequiredParameterInsertion() {
        String sqlQuery = INSERTION_TYPE.insertionQuery(Mocks.metric(),
                ImmutableMap.of("entity", Mocks.entity(), "datetime", ISO_TIME)); //value is not set
        assertBadRequest("Invalid SQL query with unset required parameter was accepted!", "IllegalArgumentException: Either value or text is required",sqlQuery);
    }
}