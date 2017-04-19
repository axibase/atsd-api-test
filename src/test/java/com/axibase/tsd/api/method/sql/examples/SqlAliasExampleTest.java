package com.axibase.tsd.api.method.sql.examples;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlAliasExampleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-alias-";


    @BeforeClass
    public static void prepareData() {
        sendSamplesToSeries(
                new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric"),
                new Sample("2016-06-17T19:16:00.000Z", "3.0")
        );
    }

    /**
     * Issue #3047
     * Test for alias documentation example.
     *
     * @see <a href="Alias example">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/alias.md</a>
     */
    @Test
    public void testAlias() {
        String sqlQuery =
                "SELECT datetime, value, entity, metric AS \"measurement\" \n" +
                        "FROM 'sql-example-alias-metric' \n" +
                        "WHERE entity = 'sql-example-alias-entity' \n" +
                        "AND datetime >= '2016-06-17T19:15:00.000Z' AND datetime < '2016-06-17T19:17:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList("datetime", "value", "entity", "measurement");

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }


}
