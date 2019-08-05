package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SqlInsertionErrorsTest extends SqlTest {
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private final InsertionType insertionType;

    @Factory(dataProvider = "insertionType", dataProviderClass = InsertionType.class)
    public SqlInsertionErrorsTest(InsertionType insertionType) {
        this.insertionType = insertionType;
    }

    @Test(
            description = "Tests that if one of the declared parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetDeclaredParameterInsertion() {
        String sqlQuery;
        if(insertionType == InsertionType.INSERT_INTO) { //cannot create ImmutableMap with unset parameter
            sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, not_set) VALUES('%s', '%s', %d)"
                    , Mocks.metric(), Mocks.entity(), ISO_TIME, VALUE);
        } else {
            sqlQuery = String.format("UPDATE \"%s\" SET entity='%s', datetime='%s', value=%d, not_set="
                    , Mocks.metric(), Mocks.entity(), ISO_TIME, VALUE);
        }
        assertBadSqlRequest("Invalid SQL query with unset declared parameter was accepted!", sqlQuery);
    }

    @Test(
            description = "Tests that if one of the required parameters is not set, error will be thrown."
    )
    @Issue("5962")
    public void testUnsetRequiredParameterInsertion() {
        String sqlQuery = insertionType.insertionQuery(Mocks.metric(),
                ImmutableMap.of("entity", Mocks.entity(), "datetime", ISO_TIME)); //value is not set
        assertBadSqlRequest("Invalid SQL query with unset required parameter was accepted!", sqlQuery);
    }
}