package com.axibase.tsd.api.method.sql.function.math;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SqlPiFunctionTest extends SqlTest {
    private static double DELTA = 1.0E-15;

    @DataProvider
    public static Object[][] provideFunctionNames() {
        return new Object[][]{{"DEGREES"}, {"RADIANS"}};
    }

    @DataProvider
    public static Object[][] provideDegreesValues() {
        return new Object[][]{{"-pi()", Math.toDegrees(-Math.PI)},
                {"-pi()*3/4", Math.toDegrees(-Math.PI * 3 / 4)},
                {"-pi()/2", Math.toDegrees(-Math.PI / 2)},
                {"-pi()/4", Math.toDegrees(-Math.PI / 4)},
                {"0.0", Math.toDegrees(0.0)},
                {"pi()/4", Math.toDegrees(Math.PI / 4)},
                {"pi()/2", Math.toDegrees(Math.PI / 2)},
                {"pi()*3/4", Math.toDegrees(Math.PI * 3 / 4)},
                {"pi()", Math.toDegrees(Math.PI)}};
    }

    @DataProvider
    public static Object[][] provideRadianValues() {
        return new Object[][]{{"-360.0", Math.toRadians(-360.0)},
                {"-180.0", Math.toRadians(-180.0)},
                {"-90.0", Math.toRadians(-90.0)},
                {"-45.0", Math.toRadians(-45.0)},
                {"0.0", Math.toRadians(0.0)},
                {"45.0", Math.toRadians(45.0)},
                {"90.0", Math.toRadians(90.0)},
                {"180.0", Math.toRadians(180.0)},
                {"360.0", Math.toRadians(360.0)}};
    }

    @Issue("5770")
    @Test
    public void testPiFunction() {
        String sqlQuery = "SELECT PI()";

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(Double.valueOf(resultTable.getRows().get(0).get(0)), Math.PI, DELTA,
                "Pi value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideDegreesValues")
    public void testDegreesFunction(String parameterValue, Double expectedValue) {
        String sqlQuery = String.format("SELECT DEGREES(%s)",
                parameterValue);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(Double.valueOf(resultTable.getRows().get(0).get(0)), expectedValue, DELTA,
                "Degrees value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideRadianValues")
    public void testRadiansFunction(String parameterValue, Double expectedValue) {
        String sqlQuery = String.format("SELECT RADIANS(%s)",
                parameterValue);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(Double.valueOf(resultTable.getRows().get(0).get(0)), expectedValue, DELTA,
                "Radians value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideFunctionNames")
    public void testNullValues(String functionName) {
        String sqlQuery = String.format("SELECT %s(cast(null as number))",
                functionName);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", functionName + " value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideFunctionNames")
    public void testNanValues(String functionName) {
        String sqlQuery = String.format("SELECT %s(NaN)",
                functionName);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", functionName + " value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideFunctionNames")
    public void testPosInfinityValues(String functionName) {
        String sqlQuery = String.format("SELECT %s(1/0)",
                functionName);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "Infinity", functionName + " value differed");
    }

    @Issue("5770")
    @Test(dataProvider = "provideFunctionNames")
    public void testNegInfinityValues(String functionName) {
        String sqlQuery = String.format("SELECT %s(-1/0)",
                functionName);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "-Infinity", functionName + " value differed");
    }
}
