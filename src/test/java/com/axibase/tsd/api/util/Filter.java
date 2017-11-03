package com.axibase.tsd.api.util;

import org.testng.collections.Sets;

import java.util.Collections;
import java.util.Set;

public class Filter<T> {
    private final String expression;
    private final Set<T> expectedResult;

    public Filter(String expression, T... expectedResult) {
        this.expression = expression;
        this.expectedResult = Sets.newHashSet();
        Collections.addAll(this.expectedResult, expectedResult);
    }

    public Filter(String expression, Set<T> expectedResult) {
        this.expression = expression;
        this.expectedResult = expectedResult;
    }

    public String getExpression() {
        return expression;
    }

    public Set<T> getExpectedResultSet() {
        return expectedResult;
    }
}
