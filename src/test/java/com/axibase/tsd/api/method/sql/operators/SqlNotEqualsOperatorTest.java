package com.axibase.tsd.api.method.sql.operators;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import javax.ws.rs.ProcessingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlNotEqualsOperatorTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-not-equals-syntax-";


    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        Sample sample1 = new Sample(Util.parseDate("2016-06-03T09:23:00.000Z").getTime(), "1.01");
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("a", "b");
        }}));
        sendSamplesToSeries(series, sample1);
    }

    /**
     * Following tests related to issue #2933
     */


    /**
     * issue #2933
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testNotEqualsWithDatetimeIsFalse() {
        final String sqlQuery = "" +
                "SELECT entity, value, datetime FROM 'sql-not-equals-syntax-metric'" +
                "WHERE datetime <> '2016-06-03T09:23:00.000Z' AND entity = 'sql-not-equals-syntax-entity'";

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

    /**
     * issue #2933
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testNotEqualsWithDatetimeIsTrue() {
        final String sqlQuery = "" +
                "SELECT entity, value, datetime FROM 'sql-not-equals-syntax-metric'" +
                "WHERE datetime <> '2016-06-03T09:25:00.000Z' AND entity = 'sql-not-equals-syntax-entity'";

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWithNumericIsFalse() {
        final String sqlQuery = "" +
                "SELECT entity, value FROM 'sql-not-equals-syntax-metric'" +
                "WHERE value <> 1.2 AND entity = 'sql-not-equals-syntax-entity'";

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("value");

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("1.01")
        );

        assertEquals("Table value rows must be identical", expectedRows, resultRows);
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWithNumericIsTrue() {
        final String sqlQuery = "" +
                "SELECT entity, value FROM 'sql-not-equals-syntax-metric'" +
                "WHERE value <> 1.01 AND entity = 'sql-not-equals-syntax-entity'";
        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getRows();
        assertTrue("Result rows must be empty", resultRows.isEmpty());
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWitStringIsFalse() {
        final String sqlQuery = "" +
                "SELECT entity, value, datetime FROM 'sql-not-equals-syntax-metric'" +
                "WHERE tags.a <> 'b'";
        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getRows();
        assertTrue("Result rows must be empty", resultRows.isEmpty());
    }

    /**
     * issue #2933
     */
    @Test
    public void testNotEqualsWithStringIsTrue() {
        final String sqlQuery = "" +
                "SELECT entity, tags.a FROM 'sql-not-equals-syntax-metric'\n" +
                "WHERE tags.a <> 'a' AND entity = 'sql-not-equals-syntax-entity'";

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("tags.a");

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("b")
        );

        assertEquals("Table value rows must be identical", expectedRows, resultRows);
    }
}
