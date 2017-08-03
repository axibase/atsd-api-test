package com.axibase.tsd.api.method.sql.meta;

import com.axibase.tsd.api.method.sql.SqlMetaTest;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Test;

public class MetaDataPlaceholderTest extends SqlMetaTest {

    /**
     * #4368
     */
    @Test
    public void testSelectExpression() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT value * ? FROM '%s'",
                metricName
        );

        String[] expectedNames = {
                "value * ?"
        };

        String[] expectedTypes = {
                "double"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testBetween() {
        String metricNameA = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM '%s' " +
                        "WHERE datetime between ? and ?",
                metricNameA
        );

        String[] expectedNames = {
                "datetime",
                "value"
        };

        String[] expectedTypes = {
                "xsd:dateTimeStamp",
                "float"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testCaseOperator() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT value, case ? when ? then ? else ? end " +
                        "FROM '%s' " +
                        "WHERE datetime < ? GROUP BY tags.t " +
                        "ORDER BY value * ?",
                metricName
        );

        String[] expectedNames = {
                "value",
                "case ?\nwhen ? then ?\nelse ?\nend"
        };

        String[] expectedTypes = {
                "float",
                "java_object"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testGroupBy() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT entity, avg(value) " +
                        "FROM '%s' " +
                        "WHERE datetime < ? " +
                        "GROUP BY entity",
                metricName
        );

        String[] expectedNames = {
                "entity",
                "avg(value)"
        };

        String[] expectedTypes = {
                "string",
                "double"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testOrderBy() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT entity, value " +
                        "FROM '%s' " +
                        "WHERE datetime < ? " +
                        "ORDER BY entity",
                metricName
        );

        String[] expectedNames = {
                "entity",
                "value"
        };

        String[] expectedTypes = {
                "string",
                "float"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testJoin() {
        String metricNameA = Mocks.metric();
        String metricNameB = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM '%s' " +
                        "JOIN '%s'  " +
                        "WHERE datetime < ? ",
                metricNameA,
                metricNameB
        );

        String[] expectedNames = {
                "entity",
                "metric",
                "value"
        };

        String[] expectedTypes = {
                "string",
                "string",
                "float"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testJoinUsingEntity() {
        String metricNameA = Mocks.metric();
        String metricNameB = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM '%s' " +
                        "JOIN USING ENTITY '%s'  " +
                        "WHERE datetime < ? ",
                metricNameA,
                metricNameB
        );

        String[] expectedNames = {
                "entity",
                "metric",
                "value"
        };

        String[] expectedTypes = {
                "string",
                "string",
                "float"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testOption() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT time, value " +
                        "FROM '%s' " +
                        "OPTION(ROW_MEMORY_THRESHOLD 0)",
                metricName
        );

        String[] expectedNames = {
                "time",
                "value"
        };

        String[] expectedTypes = {
                "bigint",
                "float"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testHaving() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT time, sum(value) AS sum_v " +
                        "FROM '%s' " +
                        "GROUP BY time " +
                        "HAVING sum_v = ? and ? < ?",
                metricName
        );

        String[] expectedNames = {
                "time",
                "sum(value)"
        };

        String[] expectedTypes = {
                "bigint",
                "double"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testMathFunctions() {
        String metricName = Mocks.metric();
        String sqlQuery = "SELECT " +
                "sqrt(?)," +
                "ceil(?)," +
                "floor(?)," +
                "round(?)," +
                "mod(?, ?)," +
                "power(?, ?)," +
                "exp(?)," +
                "ln(?)," +
                "log(?, ?) " +
                "FROM '" + metricName + "'";

        String[] expectedNames = {
                "sqrt(?)",
                "ceil(?)",
                "floor(?)",
                "round(?)",
                "mod(?, ?)",
                "power(?, ?)",
                "exp(?)",
                "ln(?)",
                "log(?, ?)"
        };

        String[] expectedTypes = {
                "double",
                "double",
                "double",
                "double",
                "double",
                "double",
                "double",
                "double",
                "double"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testStringFunctions() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT " +
                        "upper(?)," +
                        "lower(?)," +
                        "replace(?, ?, ?)," +
                        "length(?)," +
                        "concat(?, ?)," +
                        "locate(?, ?)," +
                        "substr(?, ?, ?) " +
                        "FROM '%s'",
                metricName
        );

        String[] expectedNames = {
                "upper(?)",
                "lower(?)",
                "replace(?, ?, ?)",
                "length(?)",
                "concat(?, ?)",
                "locate(?, ?)",
                "substr(?, ?, ?)",
        };

        String[] expectedTypes = {
                "string",
                "string",
                "string",
                "integer",
                "string",
                "integer",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4368
     */
    @Test
    public void testOtherFunctions() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format(
                "SELECT " +
                        "isnull(?, ?), " +
                        "coalesce(?, ?, ?) " +
                        "FROM '%s'",
                metricName
        );

        String[] expectedNames = {
                "isnull(?, ?)",
                "coalesce(?, ?, ?)"
        };

        String[] expectedTypes = {
                "java_object",
                "java_object"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }
}
