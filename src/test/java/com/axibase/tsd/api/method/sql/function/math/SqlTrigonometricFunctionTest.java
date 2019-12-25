package com.axibase.tsd.api.method.sql.function.math;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.Function;

public class SqlTrigonometricFunctionTest extends SqlTest {
    private static final double DELTA = 1.0E-15;

    private enum TrigonometricAccessor {
        SIN("SIN", Math::sin),
        COS("COS", Math::cos),
        TAN("TAN", Math::tan),
        COT("COT", param -> 1 / Math.tan(param)),
        ASIN("ASIN", Math::asin),
        ACOS("ACOS", Math::acos),
        ATAN("ATAN", Math::atan);

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

    private static Object[][] getStringDoubleArgumentsForTrigonometricFunctions() {
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

    private static Object[][] getStringDoubleArgumentsForInverseTrigonometricFunctions() {
        return new Object[][]{{"-1", -1.0}, {"-1/2", -1.0 / 2.0}, {"0", 0.0}, {"1/2", 1.0 / 2.0}, {"1", 1.0}};
    }

    private static Object[][] getParametersWithTrigonometricAccessor(TrigonometricAccessor[] accessors, Object[][] trigonometricArguments) {
        Object[][] result = new Object[trigonometricArguments.length * accessors.length][];

        int i = 0;
        for (TrigonometricAccessor accessor : accessors)
            for (Object[] arguments : trigonometricArguments) {
                result[i++] = new Object[]{arguments[0], arguments[1], accessor};
            }

        return result;
    }

    @DataProvider
    public static Object[][] provideParametersWithTrigonometricFunction() {
        return getParametersWithTrigonometricAccessor(new TrigonometricAccessor[]{
                        TrigonometricAccessor.SIN, TrigonometricAccessor.COS,
                        TrigonometricAccessor.TAN, TrigonometricAccessor.COT},
                getStringDoubleArgumentsForTrigonometricFunctions());
    }

    @DataProvider
    public static Object[][] provideParametersWithInverseTrigonometricFunction() {
        return getParametersWithTrigonometricAccessor(new TrigonometricAccessor[]{
                        TrigonometricAccessor.ASIN, TrigonometricAccessor.ACOS, TrigonometricAccessor.ATAN},
                getStringDoubleArgumentsForInverseTrigonometricFunctions());
    }

    @DataProvider
    public static Object[][] provideProhibitedAsinAcosParameters() {
        return new Object[][]{{"ASIN", "-2"}, {"ASIN", "2"}, {"ACOS", "-2"}, {"ACOS", "2"}};
    }

    @DataProvider
    public static Object[][] provideFunctionNames() {
        return new Object[][]{{"sin"}, {"asin"}, {"cos"}, {"acos"}, {"cot"}, {"tan"}, {"atan"}};
    }

    @Issue("5764")
    @Test(dataProvider = "provideParametersWithTrigonometricFunction")
    public void testTrigonometricFunctions(String functionParameter, Double accessorParameter, TrigonometricAccessor accessor) {
        String sqlQuery = String.format("SELECT %s(%s)", accessor.functionName, functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);
        String message = String.format("%s(%s) wrong ", accessor.functionName, functionParameter);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                message + "datatype");
        Assert.assertEquals(Double.parseDouble(resultTable.getRows().get(0).get(0)), accessor.apply(accessorParameter), DELTA,
                message + "result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideParametersWithInverseTrigonometricFunction")
    public void testInverseTrigonometricFunctions(String functionParameter, Double accessorParameter, TrigonometricAccessor accessor) {
        String sqlQuery = String.format("SELECT %s(%s)", accessor.functionName, functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);
        String message = String.format("%s(%s) wrong ", accessor.functionName, functionParameter);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                message + "datatype");
        Assert.assertEquals(Double.parseDouble(resultTable.getRows().get(0).get(0)), accessor.apply(accessorParameter), DELTA,
                message + "result");
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
    public void testAsinProhibitedValue(String functionName, String functionParameter) {
        String sqlQuery = String.format("SELECT %s(%s)", functionName, functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", "Wrong response");
    }
}
