package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class CastTest extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_METRIC3_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();

    private Series castNumberAsStringSeries;


    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] metricNames = {TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_METRIC3_NAME};
        String[] tags = {"4", "123", "text12a3a"};

        Registry.Entity.register(TEST_ENTITY_NAME);

        for (int i = 0; i < metricNames.length; i++) {
            String metricName = metricNames[i];
            Registry.Metric.register(metricName);

            Series series = new Series();
            series.setEntity(TEST_ENTITY_NAME);
            series.setMetric(metricName);

            series.setData(Collections.singletonList(
                    new Sample("2016-06-03T09:20:00.000Z", "1")));

            String tag = tags[i];
            series.addTag("numeric_tag", tag);

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3661
     */
    @Test
    public void testCastSumJoin() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag) + cast(t2.tags.numeric_tag) FROM '%s' t1 JOIN '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
        };

        assertSqlQueryRows("Sum of CASTs with Join gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3661
     */
    @Test
    public void testCastSumJoinUsingEntity() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag) + cast(t2.tags.numeric_tag) FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"127"}
        };

        assertSqlQueryRows("Sum of CASTs with Join Using Entity gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3661
     */
    @Test
    public void testCastMultiply() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag)*2, cast(t2.tags.numeric_tag)*2, cast(t3.tags.numeric_tag)*2 " +
                        "FROM '%s' t1 JOIN USING ENTITY '%s' t2 JOIN USING ENTITY '%s' t3",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"8", "246", "NaN"}
        };

        assertSqlQueryRows("Multiplication of CASTs gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3661
     */
    @Test
    public void testCastConcat() {
        String sqlQuery = String.format(
                "SELECT cast(concat(t1.tags.numeric_tag, t2.tags.numeric_tag))*2 FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"8246"}
        };

        assertSqlQueryRows("CAST of CONCAT gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3661
     */
    @Test
    public void testCastGroupBy() {
        String sqlQuery = String.format(
                "SELECT count(t1.value),CAST(t1.tags.numeric_tag) FROM '%s' t1 OUTER JOIN '%s' t2 OUTER JOIN '%s' t3 " +
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

    /**
     * #3661
     */
    @Test
    public void testCastWhere() {
        String sqlQuery = String.format(
                "SELECT t1.value" +
                        " FROM '%s' t1 JOIN USING ENTITY '%s' t2" +
                        " WHERE CAST(t1.tags.numeric_tag) + CAST(t2.tags.numeric_tag) = 127",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("CAST in WHERE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3661
     */
    @Test
    public void testCastWhereAndConcat() {
        String sqlQuery = String.format(
                "SELECT t1.value" +
                        " FROM '%s' t1 JOIN USING ENTITY '%s' t2" +
                        " WHERE CAST(CONCAT(t1.tags.numeric_tag, t2.tags.numeric_tag)) = 4123",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("CAST in WHERE with CONCAT gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3661
     */
    @Test
    public void testCastHaving() {
        String sqlQuery = String.format(
                "SELECT count(t1.value), CAST(t1.tags.numeric_tag) FROM '%s' t1 OUTER JOIN '%s' t2 " +
                "OUTER JOIN '%s' t3 " +
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
        castNumberAsStringSeries.setData(Collections.singleton(new Sample(Mocks.ISO_TIME, "12345.6789")));
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

    /**
     * #3770
     */
     @Test(dataProvider = "castNumberArgumentsProvider")
     public void testCastNumberAsStringApplied(String castArgument) throws Exception {
         Series series = castNumberAsStringSeries;
         String sqlQuery = String.format(
                 "SELECT CAST(%s AS string) FROM '%s'",
                 castArgument, series.getMetric()
         );

         StringTable resultTable = SqlMethod.queryTable(sqlQuery);

         assertEquals(
                 "Bad column type for CAST as string column",
                 "string", resultTable.getColumnMetaData(0).getDataType()
         );
     }

    /**
     * #3770
     */
    @Test(dataProvider = "castNumberArgumentsProvider")
    public void testCastNumberAsStringPassedToStringFunction(String castArgument) throws Exception {
        Series series = castNumberAsStringSeries;
        String sqlQuery = String.format(
                "SELECT CONCAT('foo', CAST(%s AS string)) FROM '%s'",
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
            {"1.235", "1.24"},
            {"-1.235", "-1.24"},
            {"0/0", "null"},
        };
    }

    /**
     * #3770
     */
    @Test(dataProvider = "constNumbersWithFormatProvider")
    public void testCastConstantAsStringAppliesFormat(String castArgument, String expected) throws Exception {
        /**
         * Proper format of number is #.##
         */
        Series series = castNumberAsStringSeries;
        String sqlQuery = String.format(
                "SELECT CAST(%s AS string) FROM '%s'",
                castArgument, series.getMetric()
        );

        StringTable resultTable = SqlMethod.queryTable(sqlQuery);

        String castValue = resultTable.getValueAt(0, 0);

        assertEquals("Inproper format applied", expected, castValue);
    }

    /**
     * #3770
     */
    @Test(dataProvider = "castNumberArgumentsProvider")
    public void testCastNumberAsStringAppliesFormat(String castArgument) throws Exception {
        /**
         * Proper format of number is #.##
         */
        Series series = castNumberAsStringSeries;
        String sqlQuery = String.format(
                "SELECT %1$s, CAST(%1$s AS string) FROM '%2$s'",
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

    /**
     * #4020
     */
    @Test
    public void testImplicitCastToNumber() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT metric%n" +
                "FROM '%s'%n" +
                "WHERE value = '%s'",
                series.getMetric(), series.getData().get(0).getV()
        );

        String[][] expected = {
                { series.getMetric() }
        };

        assertSqlQueryRows("String constant was not implicitly casted to number", expected, sql);
    }

    /**
     * #4020
     */
    @Test
    public void testImplicitCastStringColumnToNumber() throws Exception {
        Series series = Mocks.series();
        series.setTags(new HashMap<String, String>());
        series.addTag("value", "10");
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT metric%n" +
                "FROM '%s'%n" +
                "WHERE tags.'value' = 10",
                series.getMetric()
        );

        String[][] expected = {
                { series.getMetric() }
        };

        assertSqlQueryRows("String column was not implicitly casted to number", expected, sql);
    }

    /**
     * #4020
     */
    @Test
    public void testImplicitCastToNumberInFunction() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS('10')%n" +
                "FROM '%s'",
                series.getMetric()
        );

        String[][] expected = {
                { "10" }
        };

        assertSqlQueryRows("String constant argument was not implicitly casted to number", expected, sql);
    }


    /**
     * #4020
     */
    @Test
    public void testImplicitCastOfNonNumericReturnsNaN() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS('foo')%n" +
                        "FROM '%s'",
                series.getMetric()
        );

        String[][] expected = {
                { "NaN" }
        };

        assertSqlQueryRows("Non-numeric string 'foo' was implicitly casted to number with bad result", expected, sql);
    }

    /**
     * #4020
     */
    @Test
    public void testImplicitCastStringColumnToNumberInFunction() throws Exception {
        Series series = Mocks.series();
        series.setTags(new HashMap<String, String>());
        series.addTag("value", "10");
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS(tags.'value')%n" +
                "FROM '%s'",
                series.getMetric()
        );

        String[][] expected = {
                { "10" }
        };

        assertSqlQueryRows("String column argument was not implicitly casted to number", expected, sql);
    }

    /**
     * #4020
     */
    @Test
    public void testImplicitCastOfStringFunctionResult() throws Exception {
        Series series = Mocks.series();
        series.setData(Arrays.asList(new Sample(Mocks.ISO_TIME, 10)));
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT ABS(CONCAT(value, ''))%n" +
                "FROM '%s'",
                series.getMetric()
        );

        String[][] expected = {
                { "10" }
        };

        assertSqlQueryRows("String function result was not implicitly casted to number", expected, sql);
    }

    /**
     * #4020
     */
    @Test
    public void testImplicitCastInMathExpressionsRizesError() throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(series);

        String sql = String.format(
                "SELECT CONCAT(value, '')+10%n" +
                "FROM '%s'",
                series.getMetric()
        );

        assertBadRequest(
                "Math expression with string variable applied",
                "Invalid expression: 'concat(value, '') + 10'", queryResponse(sql)
        );
    }
}
