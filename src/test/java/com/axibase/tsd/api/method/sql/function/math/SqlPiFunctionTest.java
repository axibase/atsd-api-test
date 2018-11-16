package com.axibase.tsd.api.method.sql.function.math;

import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class SqlPiFunctionTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-pi-function-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @DataProvider
    public static Object[][] provideFunctionNames() {
        return new Object[][]{{"DEGREES"}, {"RADIANS"}};
    }

    @DataProvider
    public static Object[][] provideDegreesValues() {
        return new Object[][]{{"pi()/4", "45.0"}, {"pi()/2", "90.0"}, {"pi()*3/4", "135.0"}, {"pi()", "180.0"},
                {"pi()*5/4", "225.0"}, {"pi()*3/2", "270.0"}, {"pi()*7/4", "315.0"}, {"pi()*2", "360.0"}};
    }

    @DataProvider
    public static Object[][] provideRadianValues() {
        return new Object[][]{{"45.0", "0.7853981633974483"}, {"90.0", "1.5707963267948966"},
                {"135.0", "2.356194490192345"}, {"180.0", "3.141592653589793"}, {"225.0", "3.9269908169872414"},
                {"270.0", "4.71238898038469"}, {"315.0", "5.497787143782138"}, {"360.0", "6.283185307179586"}};
    }

    @Issue("5770")
    @Test
    public void testPiFunction() {
        String sqlQuery = "SELECT PI()";

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "3.141592653589793", "Pi value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideDegreesValues")
    public void testDegreesFunction(String parameterValue, String expectedValue) {
        String sqlQuery = String.format("SELECT DEGREES(%s)",
                parameterValue);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedValue, "Degrees value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideRadianValues")
    public void testRadiansFunction(String parameterValue, String expectedValue) {
        String sqlQuery = String.format("SELECT RADIANS(%s)",
                parameterValue);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedValue, "Radians value differed");
    }

    @Issue("5770")
    @Test()
    public void testNullValues(String functionName) {
        String sqlQuery = String.format("SELECT %s(null)"
                , functionName);

        Response response = SqlMethod.queryResponse(sqlQuery);

        Assert.assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("5770")
    @Test(dataProvider = "provideFunctionNames")
    public void testNanValues(String functionName) {
        String sqlQuery = String.format("SELECT %s(NaN)"
                , functionName);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", functionName + " value differed");
    }
}
