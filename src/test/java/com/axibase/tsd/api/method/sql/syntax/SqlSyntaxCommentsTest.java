package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;

/**
 *
 */
public class SqlSyntaxCommentsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-syntax-comments-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(Sample.ofDateInteger("2016-06-03T09:24:00.000Z", 0));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #1956
     */
    @Test
    public void testCorrectSimpleLineComment() {
        String sqlQuery = String.format(
                "--line comment %nSELECT * FROM \"%s\"",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    /**
     * #1956
     */
    @Test
    public void testCorrectMultiLineComment() {
        String sqlQuery = String.format(
                "/* multi %nline %ncomment*/ %nSELECT * FROM \"%s\" %n",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }


    /**
     * #1956
     */
    @Test
    public void testCorrectCommentAfterFrom() {
        String sqlQuery = String.format(
                "/*comment*/ %nSELECT *FROM   /*comment*/  \"%s\" %nWHERE datetime > now -5*minute",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    /**
     * #1956
     */
    @Test
    public void testCorrectNestedComment() {
        String sqlQuery = String.format(
                "/*'/**/'*/ %nSELECT * FROM \"%s\"",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    /**
     * #1956
     */
    @Test
    public void testInCorrectCommentAfterDelimiter() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\";    /*--*/",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    /**
     * #1956
     */
    @Test
    public void testInCorrectCommentAsOperand() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\"; %nWHERE entity = /*--*/",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * #1956
     */
    @Test
    public void testCorrectCommentBeforeDelimiter() {
        String sqlQuery = String.format(
                "SELECT * FROM \"%s\" %n/*--*/;",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }
}
