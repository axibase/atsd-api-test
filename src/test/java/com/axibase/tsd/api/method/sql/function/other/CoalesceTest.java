package com.axibase.tsd.api.method.sql.function.other;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;

public class CoalesceTest extends SqlTest {
    private static final String METRIC_NAME1 = metric();
    private static final String METRIC_NAME2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String entityName = entity();

        Series series1 = new Series(entityName, METRIC_NAME1);
        series1.addSamples(
                new Sample("2017-06-21T12:00:00Z", 0),
                new Sample("2017-06-21T13:00:00Z", 1)
        );

        Series series2 = new Series(entityName, METRIC_NAME2);
        series2.addSamples(
                new TextSample("2017-06-21T12:00:00Z", "text"),
                new Sample("2017-06-21T13:00:00Z", 2),
                new Sample("2017-06-21T14:00:00Z", 3)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Test
    public void testCoalesceTwoArguments() {
        String sqlQuery = String.format(
                "SELECT coalesce(t1.value, t2.value) as v " +
                        "FROM '%s' t1 " +
                        "OUTER JOIN '%s' t2 " +
                        "ORDER BY time",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedResult = {
                {"0"},
                {"1"},
                {"3"}
        };

        assertSqlQueryRows("", expectedResult, sqlQuery);
    }

    @Test
    public void testCoalesceThreeArguments() {
        String sqlQuery = String.format(
                "SELECT coalesce(t2.text, t1.value, t2.value) as v " +
                        "FROM '%s' t1 " +
                        "OUTER JOIN '%s' t2 " +
                        "ORDER BY time",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedResult = {
                {"text"},
                {"1"},
                {"3"}
        };

        assertSqlQueryRows("", expectedResult, sqlQuery);
    }

    @Test
    public void testCoalesceOneNaN() {
        String sqlQuery = String.format(
                "SELECT coalesce(nan, value, value) as v " +
                        "FROM '%s' " +
                        "ORDER BY v",
                METRIC_NAME1
        );

        String[][] expectedResult = {
                {"0"},
                {"1"}
        };

        assertSqlQueryRows("Wrong COALSESCE() result when one argument is NaN", expectedResult, sqlQuery);
    }

    @Test
    public void testCoalesceOneNull() {
        String sqlQuery = String.format(
                "SELECT coalesce(case 0 when 1 then 0 end, value, value) as v " +
                        "FROM '%s' " +
                        "ORDER BY v",
                METRIC_NAME1
        );

        String[][] expectedResult = {
                {"0"},
                {"1"}
        };

        assertSqlQueryRows("Wrong COALSESCE() result when one argument is null", expectedResult, sqlQuery);
    }

    @Test
    public void testCoalesceAllNaN() {
        String sqlQuery = String.format(
                "SELECT coalesce(value/value, value/value, value/value) as v " +
                        "FROM '%s' " +
                        "ORDER BY v",
                METRIC_NAME1
        );

        String[][] expectedResult = {
                {"1"},
                {"null"}
        };

        assertSqlQueryRows("Wrong COALSESCE() result when all arguments are NaN", expectedResult, sqlQuery);
    }

    @Test
    public void testCoalesceAllNull() {
        String sqlQuery = String.format(
                "SELECT coalesce(lag(value), lag(value), lag(value)) as v " +
                        "FROM '%s'" +
                        "ORDER BY v",
                METRIC_NAME1
        );

        String[][] expectedResult = {
                {"null"},
                {"0"}
        };

        assertSqlQueryRows("Wrong COALSESCE() result when all arguments are null", expectedResult, sqlQuery);
    }

    @Test
    public void testCoalesceNumericTypes() {
        String sqlQuery = "SELECT coalesce(1, 1.0)";
        StringTable table = queryTable(sqlQuery);
        String actualType = table.getColumnMetaData(0).getDataType();

        assertEquals("Wrong COALESCE() function result type", "double", actualType);
    }

    @Test
    public void testCoalesceObjectType() {
        String sqlQuery = "SELECT coalesce('str', 2)";
        StringTable table = queryTable(sqlQuery);
        String actualType = table.getColumnMetaData(0).getDataType();

        assertEquals("Wrong COALESCE() function result type", "java_object", actualType);
    }
}
