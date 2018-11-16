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

public class SqlTrigonometricFunctionTest extends SqlTest {

    @DataProvider
    public static Object[][] provideSinParameters() {
        return new Object[][]{{"-pi()", "-1.2246467991473532E-16"}, {"-pi()*3/4", "-0.7071067811865476"},
                {"-pi()/2", "-1.0"}, {"-pi()/4", "-0.7071067811865475"}, {"0", "0.0"},
                {"pi()/4", "0.7071067811865475"}, {"pi()/2", "1.0"}, {"pi()*3/4", "0.7071067811865476"},
                {"pi()", "1.2246467991473532E-16"}};
    }

    @DataProvider
    public static Object[][] provideCosParameters() {
        return new Object[][]{{"-pi()", "-1.0"}, {"-pi()*3/4", "-0.7071067811865475"},
                {"-pi()/2", "6.123233995736766E-17"}, {"-pi()/4", "0.7071067811865476"}, {"0", "1.0"},
                {"pi()/4", "0.7071067811865476"}, {"pi()/2", "6.123233995736766E-17"},
                {"pi()*3/4", "-0.7071067811865475"}, {"pi()", "-1.0"}};
    }

    @DataProvider
    public static Object[][] provideCotParameters() {
        return new Object[][]{{"-pi()*3/4", "0.9999999999999998"},
                {"-pi()/2", "-6.123233995736766E-17"}, {"-pi()/4", "-1.0000000000000002"},
                {"pi()/4", "1.0000000000000002"}, {"pi()/2", "6.123233995736766E-17"},
                {"pi()*3/4", "-0.9999999999999998"}};
    }

    @DataProvider
    public static Object[][] provideProhibitedCotParameters() {
        return new Object[][]{{"pi()"}, {"0"}};
    }

    @DataProvider
    public static Object[][] provideTanParameters() {
        return new Object[][]{{"-pi()", "1.2246467991473532E-16"}, {"-pi()*3/4", "1.0000000000000002"},
                {"-pi()/4", "-0.9999999999999999"}, {"0", "0.0"}, {"pi()/4", "0.9999999999999999"},
                {"pi()*3/4", "-1.0000000000000002"}, {"pi()", "-1.2246467991473532E-16"}};
    }

    @DataProvider
    public static Object[][] provideProhibitedTanParameters() {
        return new Object[][]{{"pi()/2"}};
    }

    @DataProvider
    public static Object[][] provideFunctionNames() {
        return new Object[][]{{"sin"}, {"asin"}, {"cos"}, {"acos"}, {"cot"}, {"tan"}, {"atan"}};
    }

    @Issue("5764")
    @Test(dataProvider = "provideSinParameters")
    public void testSin(String functionParameter, String expectedResult) {
        String sqlQuery = String.format("SELECT SIN(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                "Sin(" + functionParameter + ") wrong data type");
        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedResult,
                "Sin(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideCosParameters")
    public void testCos(String functionParameter, String expectedResult) {
        String sqlQuery = String.format("SELECT COS(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                "Cos(" + functionParameter + ") wrong data type");
        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedResult,
                "Cos(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideCotParameters")
    public void testCot(String functionParameter, String expectedResult) {
        String sqlQuery = String.format("SELECT COT(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                "Cot(" + functionParameter + ") wrong data type");
        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedResult,
                "Cot(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideTanParameters")
    public void testTan(String functionParameter, String expectedResult) {
        String sqlQuery = String.format("SELECT TAN(%s)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getTableMetaData().getColumnMeta(0).getDataType(), "double",
                "Tan(" + functionParameter + ") wrong data type");
        Assert.assertEquals(resultTable.getRows().get(0).get(0), expectedResult,
                "Tan(" + functionParameter + ") wrong result");
    }

    @Issue("5764")
    @Test(dataProvider = "provideFunctionNames")
    public void testNullValue(String functionParameter) {
        String sqlQuery = String.format("SELECT %s(null)", functionParameter);

        Response response = SqlMethod.queryResponse(sqlQuery);

        Assert.assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
    }

    @Issue("5764")
    @Test(dataProvider = "provideFunctionNames")
    public void testNanValue(String functionParameter) {
        String sqlQuery = String.format("SELECT %s(NaN)", functionParameter);

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        Assert.assertEquals(resultTable.getRows().get(0).get(0), "NaN", "Wrong response");
    }
}
