package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.replacementtable.ReplacementTableMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.model.replacementtable.SupportedFormat;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class CastTest extends SqlTest {
    private static final String TEST_METRIC1_NAME = Mocks.metric();
    private static final String TEST_METRIC2_NAME = Mocks.metric();
    private static final String TEST_METRIC3_NAME = Mocks.metric();
    private static final String TEST_ENTITY_NAME = Mocks.entity();

    private static final String TEST_METRIC1_NAME_3841 = Mocks.metric();
    private static final String TEST_METRIC2_NAME_3841 = Mocks.metric();
    private static final String TEST_ENTITY_NAME_3841 = Mocks.entity();

    private static final String REPLACEMENT_TABLE_NAME = Mocks.replacementTable();

    private Series castNumberAsStringSeries;


    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] metricNames = {TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_METRIC3_NAME};
        String[] tags = {"4", "123", "text12a3a"};

        for (int i = 0; i < metricNames.length; i++) {
            String metricName = metricNames[i];
            Series series = new Series(TEST_ENTITY_NAME, metricName, "numeric_tag", tags[i]);
            series.addSamples(Sample.ofDateInteger("2016-06-03T09:20:00.000Z", 1));
            seriesList.add(series);
        }

        // reproducing data for ticket #3841 behaviour
        Series series1 = new Series(TEST_ENTITY_NAME_3841, TEST_METRIC1_NAME_3841);
        series1.addSamples(Sample.ofDateInteger("2016-06-03T09:20:00.000Z", 1),
                Sample.ofDateInteger("2016-06-03T09:20:01.000Z", 2),
                Sample.ofDateInteger("2016-06-03T09:20:02.000Z", 3));
        series1.addTag("tag", "1001");
        seriesList.add(series1);

        Series series2 = new Series(null, TEST_METRIC2_NAME_3841);
        series2.setEntity(TEST_ENTITY_NAME_3841);
        series2.addSamples(Sample.ofDateInteger("2016-06-03T09:20:00.000Z", 1),
                Sample.ofDateInteger("2016-06-03T09:20:01.000Z", 2),
                Sample.ofDateInteger("2016-06-03T09:20:02.000Z", 3));
        series2.addTag("tag", "2001");
        seriesList.add(series2);

        SeriesMethod.insertSeriesCheck(seriesList);

        ReplacementTableMethod.createCheck(ReplacementTable.of(REPLACEMENT_TABLE_NAME, SupportedFormat.LIST)
                                                            .addValue("0", "0")); //Replacement table cannot be created without values
    }

    @Issue("3661")
    @Test
    public void testCastSumJoin() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag) + cast(t2.tags.numeric_tag) FROM \"%s\" t1 JOIN \"%s\" t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
        };

        assertSqlQueryRows("Sum of CASTs with Join gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3661")
    @Test
    public void testCastSumJoinUsingEntity() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag) + cast(t2.tags.numeric_tag) FROM \"%s\" t1 JOIN USING ENTITY \"%s\" t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"127"}
        };

        assertSqlQueryRows("Sum of CASTs with Join Using Entity gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3661")
    @Test
    public void testCastMultiply() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag)*2, cast(t2.tags.numeric_tag)*2, cast(t3.tags.numeric_tag)*2 " +
                        "FROM \"%s\" t1 JOIN USING ENTITY \"%s\" t2 JOIN USING ENTITY \"%s\" t3",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"8", "246", "NaN"}
        };

        assertSqlQueryRows("Multiplication of CASTs gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3661")
    @Test
    public void testCastConcat() {
        String sqlQuery = String.format(
                "SELECT cast(concat(t1.tags.numeric_tag, t2.tags.numeric_tag))*2 FROM \"%s\" t1 JOIN USING ENTITY \"%s\" t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"8246"}
        };

        assertSqlQueryRows("CAST of CONCAT gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3661")
    @Test
    public void testCastGroupBy() {
        String sqlQuery = String.format(
                "SELECT count(t1.value),CAST(t1.tags.numeric_tag) FROM \"%s\" t1 OUTER JOIN \"%s\" t2 OUTER JOIN \"%s\" t3 " +
                        "GROUP BY CAST(t1.tags.numeric_tag)",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "4"},
                {"0", "NaN"}
        };

        assertSqlQueryRows("CAST in GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3661")
    @Test
    public void testCastWhere() {
        String sqlQuery = String.format(
                "SELECT t1.value" +
                        " FROM \"%s\" t1 JOIN USING ENTITY \"%s\" t2" +
                        " WHERE CAST(t1.tags.numeric_tag) + CAST(t2.tags.numeric_tag) = 127",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("CAST in WHERE gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3661")
    @Test
    public void testCastWhereAndConcat() {
        String sqlQuery = String.format(
                "SELECT t1.value" +
                        " FROM \"%s\" t1 JOIN USING ENTITY \"%s\" t2" +
                        " WHERE CAST(CONCAT(t1.tags.numeric_tag, t2.tags.numeric_tag)) = 4123",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("CAST in WHERE with CONCAT gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3661")
    @Test
    public void testCastHaving() {
        String sqlQuery = String.format(
                "SELECT count(t1.value), CAST(t1.tags.numeric_tag) FROM \"%s\" t1 OUTER JOIN \"%s\" t2 " +
                "OUTER JOIN \"%s\" t3 " +
                "GROUP BY CAST(t1.tags.numeric_tag) " +
                "HAVING SUM(CAST(t1.tags.numeric_tag)) != 0",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "4.0"}
        };

        assertSqlQueryRows("CAST in HAVING gives wrong result", expectedRows, sqlQuery);
    }

    @BeforeClass
    public void createCastNumberAsStringTestData() throws Exception {
        castNumberAsStringSeries = Mocks.series();
        castNumberAsStringSeries.setSamples(Collections.singleton(Sample.ofDateDecimal(Mocks.ISO_TIME, new BigDecimal("12345.6789"))));
        SeriesMethod.insertSeriesCheck(castNumberAsStringSeries);
    }

    @DataProvider(name = "castNumberArgumentsProvider")
    public Object[][] provideCastNumberArguments() {
        return new Object[][] {
                {"value"},
                {"value * 2"},
                {"value + 2"},
                {"2"},
                {"0"},
                {"-1"},
                {"30e4"},
                {"5e-2"},
                {"0.3456789"},
                {"-0.23456789"},
                {"MIN(value)"},
                {"MAX(value)"},
                {"FIRST(value)"},
                {"LAST(value)"},
                {"COUNT(value)"},
                {"CAST('1.23756' as number)"},
                {"AVG(value)"},
                {"MIN(value)"},
                {"SQRT(value)"},
        };
    }

    @Issue("3770")
     @Test(dataProvider = "castNumberArgumentsProvider")
     public void testCastNumberAsStringApplied(String castArgument) throws Exception {
         Series series = castNumberAsStringSeries;
         String sqlQuery = String.format(
                 "SELECT CAST(%s AS string) FROM \"%s\"",
                 castArgument, series.getMetric()
         );

         StringTable resultTable = SqlMethod.queryTable(sqlQuery);

         assertEquals(
                 "Bad column type for CAST as string column",
                 "string", resultTable.getColumnMetaData(0).getDataType()
         );
     }

    @Issue("3770")
    @Test(dataProvider = "castNumberArgumentsProvider")
    public void testCastNumberAsStringPassedToStringFunction(String castArgument) throws Exception {
        Series series = castNumberAsStringSeries;
        String sqlQuery = String.format(
                "SELECT CONCAT('foo', CAST(%s AS string)) FROM \"%s\"",
                castArgument, series.getMetric()
        );

        StringTable resultTable = SqlMethod.queryTable(sqlQuery);

        assertEquals(
                "'foo' has not been concatenated with casted number",
                "foo", resultTable.getValueAt(0, 0).substring(0, 3)
        );
    }

    @DataProvider(name = "constNumbersWithFormatProvider")
    Object[][] provideNumericConstantsWithFormat() {
        return new String[][] {
            {"0", "0"},
            {"1", "1"},
            {"-1", "-1"},
            {"1.00", "1"},
            {"0.00", "0"},
            {"-1.00", "-1"},
            {"1.000003", "1"},
            {"-1.000003", "-1"},
            {"1231243124", "1231243124"},
            {"1.23", "1.23"},
            {"-1.23", "-1.23"},
            {"1.235", "1.23"},
            {"-1.235", "-1.23"},
            {"0/0", "null"},
        };
    }

    @Issue("3770")
    @Test(dataProvider = "constNumbersWithFormatProvider")
    public void testCastConstantAsStringAppliesFormat(String castArgument, String expected) throws Exception {
        /**
         * Proper format of number is #.##
         */
        Series series = castNumberAsStringSeries;
        String sqlQuery = String.format(
                "SELECT CAST(%s AS string) FROM \"%s\"",
                castArgument, series.getMetric()
        );

        StringTable resultTable = SqlMethod.queryTable(sqlQuery);

        String castValue = resultTable.getValueAt(0, 0);

        assertEquals("Inproper format applied", expected, castValue);
    }

    @Issue("3770")
    @Test(dataProvider = "castNumberArgumentsProvider")
    public void testCastNumberAsStringAppliesFormat(String castArgument) throws Exception {
        /**
         * Proper format of number is #.##
         */
        Series series = castNumberAsStringSeries;
        String sqlQuery = String.format(
                "SELECT %1$s, CAST(%1$s AS string) FROM \"%2$s\"",
                castArgument, series.getMetric()
        );

        StringTable resultTable = SqlMethod.queryTable(sqlQuery);

        BigDecimal rawValue = new BigDecimal(resultTable.getValueAt(0, 0));
        BigDecimal castValue = new BigDecimal(resultTable.getValueAt(1, 0));

        // assertTrue used instead of assertEquals to prevent meaningless comparision in failure message
        assertTrue(
                "Inproper format (" + castValue + ") applied to " + rawValue,
                // Check value is rounded to 0.01
                rawValue.subtract(castValue).abs().compareTo(new BigDecimal("0.01")) < 0
        );
    }

    @Issue("4020")
    @Test
    public void testImplicitCastToNumber() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT metric%n" +
                "FROM \"%s\"%n" +
                "WHERE value = '%s'",
                series.getMetric(), series.getData().get(0).getValue()
        );

        String[][] expected = {
                { series.getMetric() }
        };

        assertSqlQueryRows("String constant was not implicitly casted to number", expected, sql);
    }

    @Issue("4020")
    @Test
    public void testImplicitCastStringColumnToNumber() throws Exception {
        Series series = Mocks.series();
        series.setTags(new HashMap<String, String>());
        series.addTag("value", "10");
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT metric%n" +
                "FROM \"%s\"%n" +
                "WHERE tags.\"value\" = 10",
                series.getMetric()
        );

        String[][] expected = {
                { series.getMetric() }
        };

        assertSqlQueryRows("String column was not implicitly casted to number", expected, sql);
    }

    @Issue("4020")
    @Test
    public void testImplicitCastToNumberInFunction() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS('10')%n" +
                "FROM \"%s\"",
                series.getMetric()
        );

        String[][] expected = {
                { "10" }
        };

        assertSqlQueryRows("String constant argument was not implicitly casted to number", expected, sql);
    }


    @Issue("4020")
    @Test
    public void testImplicitCastOfNonNumericReturnsNaN() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS('foo')%n" +
                        "FROM \"%s\"",
                series.getMetric()
        );

        String[][] expected = {
                { "NaN" }
        };

        assertSqlQueryRows("Non-numeric string 'foo' was implicitly casted to number with bad result", expected, sql);
    }

    @Issue("4020")
    @Test
    public void testImplicitCastStringColumnToNumberInFunction() throws Exception {
        Series series = Mocks.series();
        series.setTags(new HashMap<String, String>());
        series.addTag("value", "10");
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS(tags.\"value\")%n" +
                "FROM \"%s\"",
                series.getMetric()
        );

        String[][] expected = {
                { "10" }
        };

        assertSqlQueryRows("String column argument was not implicitly casted to number", expected, sql);
    }

    @Issue("4020")
    @Test
    public void testImplicitCastOfStringFunctionResult() throws Exception {
        Series series = Mocks.series();
        series.setSamples(Collections.singletonList(Sample.ofDateInteger(Mocks.ISO_TIME, 10)));
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS(CONCAT(value, ''))%n" +
                "FROM \"%s\"",
                series.getMetric()
        );

        String[][] expected = {
                { "10" }
        };

        assertSqlQueryRows("String function result was not implicitly casted to number", expected, sql);
    }

    @Issue("4020")
    @Test
    public void testImplicitCastInMathExpressionsRaisesError() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT CONCAT(value, '')+10%n" +
                "FROM \"%s\"",
                series.getMetric()
        );

        assertBadRequest(
                "Math expression with string variable applied",
                "Invalid expression: 'concat(value, '') + 10' at line 1 position 7 near \"CONCAT\"", queryResponse(sql)
        );
    }

    @Issue("4182")
    @Test
    public void testCastIsNullString() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL('5', '3') AS NUMBER) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"5"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullEmptyString() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(text, '3') AS NUMBER) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullStringExpression() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(CONCAT('5', '8'), '3') AS NUMBER) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"58"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Issue("6366")
    @Test
    public void testCastIsNullLookup() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(LOOKUP('%s', value), '3') AS NUMBER) FROM \"%s\" ",
                REPLACEMENT_TABLE_NAME,
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Issue("6366")
    @Test
    public void testCastIsNullLookupExpression() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(LOOKUP('%s', value), LENGTH(CONCAT('test', '123'))) AS NUMBER) FROM \"%s\" ",
                REPLACEMENT_TABLE_NAME,
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"7"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullNumeric() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(value, 3) AS STRING) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullNumericNaN() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(NaN, 3) AS STRING) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullNumericExpression() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(5 + SQRT(9), 3) AS STRING) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"8"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullStringNumericAsString() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL('5', 3) AS STRING) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"5"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullEmptyStringNumericAsString() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(text, 3) AS STRING) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullNumericStringAsString() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(5, '3') AS STRING) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"5"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullNaNNumericStringAsString() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(NaN, '3') AS STRING) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullStringNumericAsNumber() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL('5', 3) AS NUMBER) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"5"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullEmptyStringNumericAsNumber() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(text, 3) AS NUMBER) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullNumericStringAsNumber() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(5, '3') AS NUMBER) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"5"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4182")
    @Test
    public void testCastIsNullNaNNumericStringAsNumber() {
        String sqlQuery = String.format(
                "SELECT CAST(ISNULL(NaN, '3') AS NUMBER) FROM \"%s\" ",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3841")
    @Test
    public void testCastReproducingBehaviourInTicket3841() {
        // reproducing ticket #3841 behaviour
        // in that case grouping parameter becomes (1000 second)
        String sqlQuery = String.format(
                "SELECT SUM(e.value) AS export, SUM(i.value) AS import, " +
                "  SUM(e.value)-SUM(i.value) AS trade_balance " +
                "  FROM \"%s\" e " +
                "  JOIN USING ENTITY \"%s\" i " +
                "WHERE e.datetime >= '1970-01-01T00:00:00Z' and e.datetime < '2016-12-01T00:00:00Z' " +
                "  AND CAST(e.tags.tag AS number) > 1000 " +
                "GROUP BY e.period(1 second)",
                TEST_METRIC1_NAME_3841,
                TEST_METRIC2_NAME_3841
        );

        String[][] expectedRows = {
                {"1" , "1", "0"},
                {"2" , "2", "0"},
                {"3" , "3", "0"}
        };

        assertSqlQueryRows("Parsing '>1000' in CAST failed like in ticket #3841", expectedRows, sqlQuery);
    }
}
