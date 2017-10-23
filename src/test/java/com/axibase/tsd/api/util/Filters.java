package com.axibase.tsd.api.util;

import com.google.common.collect.Sets;

import java.util.*;

public class Filters {
    public static <T> Collection<Filter<T>> crossProductAnd(Collection<Filter<T>> filters1, Collection<Filter<T>> filters2) {
        List<Filter<T>> result = new ArrayList<>(filters1.size() * filters2.size());
        for (Filter<T> firstFilter : filters1) {
            for (Filter<T> secondFilter : filters2) {
                String filter = String.format("(%s) AND (%s)", firstFilter.getExpression(), secondFilter.getExpression());
                Set<T> expectedValues = Sets.intersection(firstFilter.getExpectedResultSet(), secondFilter.getExpectedResultSet());
                result.add(new Filter<>(filter, expectedValues));
            }
        }

        return result;
    }

    public static <T> Collection<Filter<T>> crossProductAnd(Filter<T>[] filters1, Filter<T>[] filters2) {
        return crossProductAnd(Arrays.asList(filters1), Arrays.asList(filters2));
    }

    public static <T> Collection<Filter<T>> crossProductOr(Collection<Filter<T>> filters1, Collection<Filter<T>> filters2) {
        List<Filter<T>> result = new ArrayList<>(filters1.size() * filters2.size());
        for (Filter<T> firstFilter : filters1) {
            for (Filter<T> secondFilter : filters2) {
                String filter = String.format("(%s) OR (%s)", firstFilter.getExpression(), secondFilter.getExpression());
                Set<T> expectedValues = Sets.union(firstFilter.getExpectedResultSet(), secondFilter.getExpectedResultSet());
                result.add(new Filter<>(filter, expectedValues));
            }
        }

        return result;
    }

    public static <T> Collection<Filter<T>> crossProductOr(Filter<T>[] filters1, Filter<T>[] filters2) {
        return crossProductOr(Arrays.asList(filters1), Arrays.asList(filters2));
    }

    public static <T> Object[][] formatForDataProvider(Collection<Filter<T>> filters) {
        Object[][] result = new Object[filters.size()][1];
        int i = 0;
        for (Filter<T> filter : filters) {
            result[i][0] = filter;
            i++;
        }

        return result;
    }

    public static <T> Object[][] formatForDataProvider(Filter<T>[] filters) {
        return formatForDataProvider(Arrays.asList(filters));
    }
}
