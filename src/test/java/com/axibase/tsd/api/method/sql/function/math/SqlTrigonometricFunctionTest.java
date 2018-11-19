package com.axibase.tsd.api.method.sql.function.math;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.Function;

public class SqlTrigonometricFunctionTest extends SqlTest {
    private static double DELTA = 1.0E-15;

    private enum TrigonometricAccessor {
        SIN("SIN", Math::sin),
        COS("COS", Math::cos),
        TAN("TAN", Math::tan),
        COT("COT", param -> 1 / Math.tan(param));

        String functionName;
        Function<Double, Double> trigonometricFunction;

        TrigonometricAccessor(String functionName, Function<Double, Double> trigonometricFunction) {
            this.functionName = functionName;
            this.trigonometricFunction = trigonometricFunction;
        }

        double apply(double parameter) {
            return trigonometricFunction.apply(parameter);
        }
    }

    private static Object[][] getArguments() {
        return new Object[][]{{"-pi()", -Math.PI},
                {"-pi()*3/4", -Math.PI * 3 / 4},
                {"-pi()/2", -Math.PI / 2},
                {"-pi()/4", -Math.PI / 4},
                {"0", 0.0},
                {"pi()/4", Math.PI / 4},
                {"pi()/2", Math.PI / 2},
                {"pi()*3/4", Math.PI * 3 / 4},
                {"pi()", Math.PI}};
    }

    @DataProvider
    public static Object[][] provideTrigonometricValues() {
        TrigonometricAccessor[] accessors = TrigonometricAccessor.values();
        Object[][] arguments = getArguments();
        Object[][] result = new Object[arguments.length * accessors.length][];

        for (int i = 0; i < accessors.length; i++)
            for (int j = 0; j < arguments.length; j++) {
                result[i * arguments.length + j] = new Object[]{arguments[j][0], arguments[j][1], accessors[i]};
            }

        return result;
    }

    @DataProvider
    public static Object[][] provideAsinParameters() {
        return new Object[][]{{"-1", "-1.5707963267948966"}, {"-1/2", "-0.5235987755982989"}, {"0", "0.0"},
                {"1/2", "0.5235987755982989"}, {"1", "1.5707963267948966"}};
    }

    @DataProvider
    public static Object[][] provideProhibitedAsinAcosParameters() {
        return new Object[][]{{"-2"}, {"2"}};
    }

    @DataProvider
    public static Object[][] provideAcosParameters() {
        return new Object[][]{{"-1", "3.141592653589793"}, {"-1/2", "2.0943951023931957"}, {"0", "1.5707963267948966"},
                {"1/2", "1.0471975511965979"}, {"1", "0.0"}};
    }

    @DataProvider
    public static Object[][] provideAtanParameters() {
        return new Object[][]{{"-1/0", "-1.5707963267948966"}, {"-1", "-0.7853981633974483"},
                {"-1/2", "-0.4636476090008061"}, {"0", "0.0"}, {"1/2", "0.4636476090008061"},
                {"1", "0.7853981633974483"}, {"1/0", "1.5707963267948966"}};
    }

    @DataProvider
    public static Object[][] provideFunctionNames() {
        return new Object[][]{{"sin"}, {"asin"}, {"cos"}, {"acos"}, {"cot"}, {"tan"}, {"atan"}};
    }

    @Issue("5764")
    @Test(dataProvider = "provideTrigonometricValues")
    public void testTrigonometricFunctions(String functionParameter, Double expectedResult, TrigonometricAccessor accessor) {
        String sqlQuery = String.format("SELECT %s(%s)", accessor.functionName, functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                accessor.functionName + "(" + functionParameter + ") wrong data type");
        Assert.assertEquals(Double.valueOf(resultTable.getRows().get(0).get(0)), accessor.apply(expectedResult), DELTA,
                accessor.functionName + "(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideAsinParameters")
    public void testAsin(String functionParameter, String expectedResult) {
        String sqlQuery = String.format("SELECT ASIN(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                "Asin(" + functionParameter + ") wrong data type");
        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedResult,
                "Asin(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideAcosParameters")
    public void testAcos(String functionParameter, String expectedResult) {
        String sqlQuery = String.format("SELECT ACOS(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                "Acos(" + functionParameter + ") wrong data type");
        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedResult,
                "Acos(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideAtanParameters")
    public void testAtan(String functionParameter, String expectedResult) {
        String sqlQuery = String.format("SELECT ATAN(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                "Atan(" + functionParameter + ") wrong data type");
        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedResult,
                "Atan(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideFunctionNames")
    public void testNullValue(String functionParameter) {
        String sqlQuery = String.format("SELECT %s(cast(null as number))", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", "Wrong response");
    }

    @Issue("5764")
    @Test(dataProvider = "provideFunctionNames")
    public void testNanValue(String functionParameter) {
        String sqlQuery = String.format("SELECT %s(NaN)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", "Wrong response");
    }

    @Issue("5764")
    @Test(dataProvider = "provideProhibitedAsinAcosParameters")
    public void testAsinProhibitedValue(String functionParameter) {
        String sqlQuery = String.format("SELECT ASIN(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", "Wrong response");
    }

    @Issue("5764")
    @Test(dataProvider = "provideProhibitedAsinAcosParameters")
    public void testAcosProhibitedValue(String functionParameter) {
        String sqlQuery = String.format("SELECT ACOS(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", "Wrong response");
    }
}
