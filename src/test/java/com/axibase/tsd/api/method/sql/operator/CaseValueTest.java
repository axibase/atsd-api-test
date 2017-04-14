package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class CaseValueTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series s = new Series(entity(), METRIC_NAME);
        s.addData(new Sample("2017-01-01T09:30:00.000Z", "1", "a"));
        s.addData(new Sample("2017-01-02T09:30:00.000Z", "2", "b"));
        s.addData(new Sample("2017-01-03T09:30:00.000Z", "3", "c"));
        s.addData(new Sample("2017-01-04T09:30:00.000Z", "4", "d"));
        s.addData(new Sample("2017-01-05T09:30:00.000Z", "5", "e"));
        s.addData(new Sample("2017-01-06T09:30:00.000Z", "6", "f"));

        SeriesMethod.insertSeriesCheck(s);
    }

    /**
     * #4021
     */
    @Test
    public void testCaseValueSelf() {
        String sqlQuery = String.format(
                "SELECT CASE value " +
                        "WHEN value THEN value END " +
                        "FROM '%s' " +
                        "ORDER BY value",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"}, {"2"}, {"3"}, {"4"}, {"5"}, {"6"}
        };

        assertSqlQueryRows("CASE <value> ... (simple case) without ELSE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #4021
     */
    @Test
    public void testCaseValueWithElse() {
        String sqlQuery = String.format(
                "SELECT CASE value " +
                        "WHEN 1 THEN 'a' " +
                        "WHEN 2 THEN 'b' " +
                        "WHEN 3 THEN 'c' " +
                        "WHEN 4 THEN 'd' " +
                        "WHEN 5 THEN 'e' " +
                        "ELSE 'x' " +
                        "END " +
                        "FROM '%s' " +
                        "ORDER BY value",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"a"}, {"b"}, {"c"}, {"d"}, {"e"}, {"x"}
        };

        assertSqlQueryRows("CASE <value> ... (alternatives) with ELSE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #4021
     */
    @Test
    public void testCaseValueWithoutElse() {
        String sqlQuery = String.format(
                "SELECT CASE value " +
                        "WHEN 1 THEN 'a' " +
                        "WHEN 2 THEN 'b' " +
                        "WHEN 3 THEN 'c' " +
                        "WHEN 4 THEN 'd' " +
                        "WHEN 5 THEN 'e' " +
                        "END " +
                        "FROM '%s' " +
                        "ORDER BY value",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"a"}, {"b"}, {"c"}, {"d"}, {"e"}, {"null"}
        };

        assertSqlQueryRows("CASE <value> ... (alternatives) without ELSE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #4021
     */
    @Test
    public void testCaseValueExpr() {
        String sqlQuery = String.format(
                "SELECT CASE MOD(value, 4) " +
                        "WHEN 0/1 THEN 'a' " +
                        "WHEN SQRT(1) THEN 'b' " +
                        "WHEN 3*4-10 THEN 'c' " +
                        "WHEN MOD(57, 9) THEN 'd' " +
                        "ELSE 'x' " +
                        "END, value " +
                        "FROM '%s' " +
                        "ORDER BY 1,2",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"a", "4"},
                {"b", "1"},
                {"b", "5"},
                {"c", "2"},
                {"c", "6"},
                {"d", "3"}
        };

        assertSqlQueryRows("CASE <value> ... (WHEN <expression>) with ELSE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #4021
     */
    @Test
    public void testCaseValueText() {
        String sqlQuery = String.format(
                "SELECT CASE text " +
                        "WHEN 'a' THEN 1 " +
                        "WHEN 'b' THEN 2 " +
                        "WHEN 'c' THEN 3 " +
                        "WHEN 'd' THEN 4 " +
                        "WHEN 'e' THEN 5 " +
                        "END " +
                        "FROM '%s' " +
                        "ORDER BY text",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"}, {"2"}, {"3"}, {"4"}, {"5"}, {"null"}
        };

        assertSqlQueryRows("CASE <value> ... for strings gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #4021
     */
    @Test
    public void testCaseNoFrom() {
        String sqlQuery = "SELECT CASE WHEN 0 < 1 THEN 1 ELSE 0 END";

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("CASE WHEN <condition> ... without FROM doesn't work", expectedRows, sqlQuery);
    }

    /**
     * 4057
     */
    @Test
    public void testCaseMultipleCompare() {
        String sqlQuery = String.format(
                "SELECT CASE value " +
                        "WHEN 1 OR 2 THEN 'a' " +
                        "WHEN 2 OR 3 OR 4 THEN 'b' " +
                        "WHEN 5 THEN 'c' " +
                        "END " +
                        "FROM '%s' " +
                        "ORDER BY value",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"a"}, {"a"}, {"b"}, {"b"}, {"c"}, {"null"}
        };

        assertSqlQueryRows("CASE <value> ... with WHEN alternatives gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * 4057
     */
    @Test
    public void testCaseNestedWhen() {
        String sqlQuery = String.format(
                "SELECT CASE value " +
                        "WHEN 1 OR 2 THEN 'a' " +
                        "WHEN 2 OR 3 OR (CASE text WHEN 'd' THEN 4 END) THEN 'b' " +
                        "WHEN 5 THEN 'c' " +
                        "END " +
                        "FROM '%s' " +
                        "ORDER BY value",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"a"}, {"a"}, {"b"}, {"b"}, {"c"}, {"null"}
        };

        assertSqlQueryRows("CASE <value> ... with WHEN alternatives (nested OR CASE...) without ELSE gives wrong result",
                expectedRows, sqlQuery);
    }

    /**
     * 4057
     */
    @Test
    public void testCaseNestedThen() {
        String sqlQuery = String.format(
                "SELECT CASE value " +
                        "WHEN 1 OR 2 THEN 'a' " +
                        "WHEN 2 OR 3 OR 4 THEN " +
                            "(CASE text WHEN 'c' OR 'd' THEN 'x' END) " +
                        "WHEN 5 THEN 'c' " +
                        "END " +
                        "FROM '%s' " +
                        "ORDER BY value",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"a"}, {"a"}, {"x"}, {"x"}, {"c"}, {"null"}
        };

        assertSqlQueryRows("CASE <value> ... with WHEN alternatives (nested THEN CASE...) without ELSE gives wrong result",
                expectedRows, sqlQuery);
    }

    /**
     * 4057
     */
    @Test
    public void testCaseNestedCase() {
        String sqlQuery = String.format(
                "SELECT CASE CASE text " +
                        "WHEN 'a' OR 'b' THEN 2 " +
                        "WHEN 'c' OR 'd' OR 'e' THEN 1 " +
                        "END " +
                        "WHEN 1 THEN 'a' " +
                        "WHEN 2 THEN 'b' " +
                        "ELSE 'c'" +
                        "END " +
                        "FROM '%s' " +
                        "ORDER BY value",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"b"}, {"b"}, {"a"}, {"a"}, {"a"}, {"c"}
        };

        assertSqlQueryRows("CASE CASE <value> ... with ELSE gives wrong result", expectedRows, sqlQuery);
    }
}
