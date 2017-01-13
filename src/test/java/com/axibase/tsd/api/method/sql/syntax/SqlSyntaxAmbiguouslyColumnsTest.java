package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static com.axibase.tsd.api.util.ErrorTemplate.SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL;


public class SqlSyntaxAmbiguouslyColumnsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-syntax-ambiguously-columns-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity";
    private static final String BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE = "Query must raise ambiguously error for : %s";
    private static final String OK_REQUEST_ASSERT_MESSAGE_TEMPLATE = "Query mustn't raise any error for";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series1 = new Series(),
                series2 = new Series();


        series1.setEntity(TEST_ENTITY1_NAME);
        series1.setMetric(TEST_METRIC1_NAME);
        series1.addData(new Sample("2016-06-03T09:24:00.000Z", "0"));
        series1.addTag("a", "b");


        series2.setEntity(TEST_ENTITY1_NAME);
        series2.setMetric(TEST_METRIC2_NAME);
        series2.addData(new Sample("2016-06-03T09:24:01.000Z", "1"));
        series2.addTag("b", "a");

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }


    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyValueColumn() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column value"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "value"), response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyEntityColumn() {
        String sqlQuery = String.format(
                "SELECT entity FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column entity"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "entity"), response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyTimeColumn() {
        String sqlQuery = String.format(
                "SELECT time FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column time"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "time"), response);
    }


    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyDateTimeColumn() {
        String sqlQuery = String.format(
                "SELECT datetime FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "where clause"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "datetime"), response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyTagsColumn() {
        String sqlQuery = String.format(
                "SELECT tags FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column value"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "tags"), response);
    }


    /**
     * #3157
     */
    @Test
    public void testAmbiguouslySpecifiedTagColumn() {
        String sqlQuery = String.format(
                "SELECT tags.a FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column tags.a"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "tags.a"), response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyTagsAllColumn() {
        String sqlQuery = String.format(
                "SELECT tags.* FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "tags list"),
                "Tags list ambiguously defined", response);
    }


    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyMetricTagsAllColumn() {
        String sqlQuery = String.format(
                "SELECT metric.tags.* FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "metric tags list"),
                "Metric tags list ambiguously defined", response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslySpecifiedMetricTagAllColumn() {
        String sqlQuery = String.format(
                "SELECT metric.tags.a FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column metric.tags.a"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "metric.tags.a"), response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslySpecifiedEntityTagAllColumn() {
        String sqlQuery = String.format(
                "SELECT entity.tags.a FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column entity.tags.a"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "entity.tags.a"), response);
    }


    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyColumnWithMathFunction() {
        String sqlQuery = String.format(
                "SELECT ABS(value) FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column ABS(value)"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "ABS(value)"), response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyColumnWithAggregateFunction() {
        String sqlQuery = String.format(
                "SELECT AVG(value) FROM '%s' %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "column value"),
                String.format(SQL_SYNTAX_AMBIGUOUS_COLUMN_TPL, "AVG(value)"), response);
    }

    /**
     * #3157
     */
    @Test
    public void testAmbiguouslyColumnInWhereClause() {
        String sqlQuery = String.format(
                "SELECT t1.value FROM '%s' t1 %nJOIN '%s' %nWHERE value <> 0",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest(String.format(BAD_REQUEST_ASSERT_MESSAGE_TEMPLATE, "where clause"),
                "Condition in where clause ambiguously defined", response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectValueColumn() {
        String sqlQuery = String.format(
                "SELECT t1.value FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectEntityColumn() {
        String sqlQuery = String.format(
                "SELECT t1.entity FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectTimeColumn() {
        String sqlQuery = String.format(
                "SELECT t1.time FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }


    /**
     * #3157
     */
    @Test
    public void testCorrectDateTimeColumn() {
        String sqlQuery = String.format(
                "SELECT t1.datetime FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectTagsColumn() {
        String sqlQuery = String.format(
                "SELECT t1.tags FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }


    /**
     * #3157
     */
    @Test
    public void testCorrectSpecifiedTagColumn() {
        String sqlQuery = String.format(
                "SELECT t1.tags.a FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectTagsAllColumn() {
        String sqlQuery = String.format(
                "SELECT t1.tags.* FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }


    /**
     * #3157
     */
    @Test
    public void testCorrectMetricTagsAllColumn() {
        String sqlQuery = String.format(
                "SELECT t1.metric.tags.* FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }


    /**
     * #3157
     */
    @Test
    public void testCorrectSpecifiedMetricTagAllColumn() {
        String sqlQuery = String.format(
                "SELECT t1.metric.tags.a FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectSpecifiedEntityTagAllColumn() {
        String sqlQuery = String.format(
                "SELECT t1.entity.tags.a FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }


    /**
     * #3157
     */
    @Test
    public void testCorrectColumnWithMathFunction() {
        String sqlQuery = String.format(
                "SELECT ABS(t1.value) FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectColumnWithAggregateFunction() {
        String sqlQuery = String.format(
                "SELECT AVG(t1.value) FROM '%s' t1 %nJOIN '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }

    /**
     * #3157
     */
    @Test
    public void testCorrectColumnInWhereClause() {
        String sqlQuery = String.format(
                "SELECT t1.value FROM '%s' t1 %nJOIN '%s' %nWHERE t1.value <> 0",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        Response response = queryResponse(sqlQuery);

        assertOkRequest(OK_REQUEST_ASSERT_MESSAGE_TEMPLATE, response);
    }
}
