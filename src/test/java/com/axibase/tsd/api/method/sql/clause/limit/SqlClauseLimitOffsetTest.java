package com.axibase.tsd.api.method.sql.clause.limit;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmgainskiy
 */
public class SqlClauseLimitOffsetTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-clause-limit-offset-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TESTS_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String ERROR_MESSAGE_TEMPLATE =
            "Syntax error at line %s position %s: %s";
    private static final String DEFAULT_ASSERT_MESSAGE = "Query must return  correct table";


    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TESTS_ENTITY_NAME, TEST_METRIC_NAME);
        series.setData(
                Arrays.asList(
                        new Sample("2016-06-03T09:23:00.000Z", "0"),
                        new Sample("2016-06-03T09:23:01.000Z", "1"),
                        new Sample("2016-06-03T09:23:02.000Z", "2"),
                        new Sample("2016-06-03T09:23:03.000Z", "3")
                )
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * Issue #3229
     */
    @Test
    public void testCorrectOffsetByLimitClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT 1,2",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = Arrays.asList("1", "2");

        assertTableContainsColumnValues(expectedColumn, resultTable, "value");
    }

    /**
     * Issue #3229
     */
    @Test
    public void testCorrectOffsetByOffsetClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT 2 OFFSET 1",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = Arrays.asList("1", "2");

        assertTableContainsColumnValues(expectedColumn, resultTable, "value");
    }


    /**
     * Issue #3229
     */
    @Test
    public void testCorrectOutOffsetByOffsetClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT 3 OFFSET 5",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = Collections.emptyList();

        assertTableContainsColumnValues(expectedColumn, resultTable, "value");
    }


    /**
     * Issue #3229
     */
    @Test
    public void testCorrectOutOffsetByLimitClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT 5,3",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = Collections.emptyList();

        assertTableContainsColumnValues(expectedColumn, resultTable, "value");
    }


    /**
     * Issue #3229
     */
    @Test
    public void testInCorrectNotIntegerOffsetByLimitClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT -1,3",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(
                DEFAULT_ASSERT_MESSAGE,
                response,
                String.format(
                        ERROR_MESSAGE_TEMPLATE,
                        "2", "6", "no viable alternative at input '-'"
                )
        );
    }


    /**
     * Issue #3229
     */
    @Test
    public void testInCorrectNotIntegerOffsetByOffsetClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT 3 OFFSET -1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(
                DEFAULT_ASSERT_MESSAGE,
                response,
                String.format(
                        ERROR_MESSAGE_TEMPLATE,
                        "2", "15", "extraneous input '-' expecting INTEGER_LITERAL"
                )
        );
    }

    /**
     * Issue #3229
     */
    @Test
    public void testInCorrectLetterOffsetByLimitClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT 3 OFFSET A",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(
                DEFAULT_ASSERT_MESSAGE,
                response,
                String.format(
                        ERROR_MESSAGE_TEMPLATE,
                        "2", "15", "mismatched input 'A' expecting INTEGER_LITERAL"
                )
        );
    }


    /**
     * Issue #3229
     */
    @Test
    public void testInCorrectLetterOffsetByOffsetClause() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nLIMIT A,3",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(
                DEFAULT_ASSERT_MESSAGE,
                response,
                String.format(
                        ERROR_MESSAGE_TEMPLATE,
                        "2", "6", "no viable alternative at input 'A'"
                )
        );
    }


    /**
     * Issue #3229
     */
    @Test
    public void testInCorrectOffsetWithoutLimit() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'\nOFFSET 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertBadRequest(
                DEFAULT_ASSERT_MESSAGE,
                response,
                String.format(
                        ERROR_MESSAGE_TEMPLATE,
                        "2", "0", "mismatched input 'OFFSET' expecting {<EOF>, WHERE, ORDER, GROUP, LIMIT, " +
                                "WITH, INNER, OUTER, JOIN}"
                )
        );
    }
}
