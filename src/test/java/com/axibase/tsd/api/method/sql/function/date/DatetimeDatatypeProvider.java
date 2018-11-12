package com.axibase.tsd.api.method.sql.function.date;

import org.testng.annotations.DataProvider;

public class DatetimeDatatypeProvider {

    @DataProvider
    public static Object[][] provideAggregateFunctions() {
        return new Object[][]{{"avg"}, {"first"}, {"lag"}, {"last"}, {"lead"}, {"max"}, {"MAX_VALUE_TIME"},
                {"median"}, {"min"}, {"MIN_VALUE_TIME"}, {"sum"}};
    }

    @DataProvider
    public static Object[][] provideOtherFunctions() {
        return new Object[][]{{"isnull"}, {"coalesce"}};
    }

    @DataProvider
    public static Object[][] provideDatetimeResource() {
        return new Object[][]{{"datetime"}, {"CURRENT_TIMESTAMP"}};
    }

    @DataProvider
    public static Object[][] provideCombinationAggregateResource() {
        Object[][] aggregateFunctions = provideAggregateFunctions();
        Object[][] datetimeResources = provideDatetimeResource();
        Object[][] result = new Object[aggregateFunctions.length * datetimeResources.length][2];

        int i = 0;
        for (Object[] aggregateFunction : aggregateFunctions)
            for (Object[] datetimeResource : datetimeResources) {
                result[i][0] = aggregateFunction[0];
                result[i++][1] = datetimeResource[0];
            }

        return result;
    }

    @DataProvider
    public static Object[][] provideCombinationOtherResource() {
        Object[][] otherFunctions = provideOtherFunctions();
        Object[][] datetimeResources = provideDatetimeResource();
        Object[][] result = new Object[otherFunctions.length * datetimeResources.length][2];

        int i = 0;
        for (Object[] otherFunction : otherFunctions)
            for (Object[] datetimeResource : datetimeResources) {
                result[i][0] = otherFunction[0];
                result[i++][1] = datetimeResource[0];
            }

        return result;
    }

    @DataProvider
    public static Object[][] provideCombinationFunctions() {
        Object[][] aggregateFunctions = provideAggregateFunctions();
        Object[][] otherFunctions = provideOtherFunctions();
        Object[][] datetimeResources = provideDatetimeResource();
        Object[][] result = new Object[aggregateFunctions.length * otherFunctions.length * datetimeResources.length][3];

        int i = 0;
        for (Object[] aggregateFunction : aggregateFunctions)
            for (Object[] otherFunction : otherFunctions)
                for (Object[] datetimeResource : datetimeResources) {
                    result[i][0] = aggregateFunction[0];
                    result[i][1] = otherFunction[0];
                    result[i++][2] = datetimeResource[0];
                }

        return result;
    }

}
