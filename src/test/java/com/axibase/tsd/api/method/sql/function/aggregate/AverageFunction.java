package com.axibase.tsd.api.method.sql.function.aggregate;


import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;


public class AverageFunction extends SqlTest {

    @DataProvider(name = "sqlStringArg")
    private static Object[][] strings() {
        Object[][] result = new Object[POSSIBLE_STRING_FUNCTION_ARGS.size()][1];
        for (int i = 0; i < POSSIBLE_STRING_FUNCTION_ARGS.size(); i++) {
            result[i] = new Object[]{POSSIBLE_STRING_FUNCTION_ARGS.get(i)};
        }
        return result;
    }


    @Test(dataProvider = "sqlStringArg")
    public void testLengthFunction(String param) throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        String sqlQuery = String.format("SELECT AVG(LENGTH(%s)) FROM '%s'%nGROUP BY entity",
                param, series.getMetric()
        );
        String assertMessage = String.format("Failed to aggregate average values from length function with param" +
                        " %s%n\tQuery: %s",
                param, sqlQuery
        );
        assertOkRequest(assertMessage, queryResponse(sqlQuery));
    }
}
