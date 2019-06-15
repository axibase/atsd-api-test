package com.axibase.tsd.api.util;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.BinaryOperator;

@UtilityClass
public class Filters {
    private static <T> Collection<Filter<T>> crossProduct(Collection<Filter<T>> filters1, Collection<Filter<T>> filters2, String format, BinaryOperator<Set<T>> operator) {
        final List<Filter<T>> result = new ArrayList<>(filters1.size() * filters2.size());
        for (Filter<T> firstFilter : filters1) {
            for (Filter<T> secondFilter : filters2) {
                String filter = String.format(format, firstFilter.getExpression(), secondFilter.getExpression());
                Set<T> expectedValues = operator.apply(firstFilter.getExpectedResultSet(), secondFilter.getExpectedResultSet());
                result.add(new Filter<>(filter, expectedValues));
            }
        }
        return result;
    }

    public static <T> Collection<Filter<T>> crossProductAnd(Collection<Filter<T>> filters1, Collection<Filter<T>> filters2) {
        return crossProduct(filters1, filters2, "(%s) AND (%s)", Sets::intersection);
    }

    public static <T> Collection<Filter<T>> selfCrossProductAnd(Collection<Filter<T>> filters) {
        return crossProductAnd(filters, filters);
    }

    public static <T> Collection<Filter<T>> crossProductOr(Collection<Filter<T>> filters1, Collection<Filter<T>> filters2) {
        return crossProduct(filters1, filters2, "(%s) OR (%s)", Sets::union);
    }

    public static <T> Collection<Filter<T>> selfCrossProductOr(Collection<Filter<T>> filters) {
        return crossProductOr(filters, filters);
    }
}
