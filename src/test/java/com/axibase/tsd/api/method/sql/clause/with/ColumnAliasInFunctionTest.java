package com.axibase.tsd.api.method.sql.clause.with;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class ColumnAliasInFunctionTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(ENTITY_NAME, METRIC_NAME);

        series.addData(new Sample("2017-01-01T12:00:00.000Z", "2"));
        series.addData(new Sample("2017-01-02T12:00:00.000Z", "4"));
        series.addData(new Sample("2017-01-03T12:00:00.000Z", "1"));
        series.addData(new Sample("2017-01-04T12:00:00.000Z", "3"));
        series.addData(new Sample("2017-01-05T12:00:00.000Z", "5"));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * 3842
     */
    @Test
    public void testAliasInsideRowNumber() {
        String sqlQuery = String.format(
                "SELECT entity AS e, value AS v " +
                "FROM '%s' " +
                "WITH ROW_NUMBER(e ORDER BY v) <= 2",
                METRIC_NAME
        );

        String[][] expectedRows = new String[][] {
                {ENTITY_NAME, "1"},
                {ENTITY_NAME, "2"}
        };

        assertSqlQueryRows("Wrong result with alias inside ROW_NUMBER", expectedRows, sqlQuery);
    }

    /**
     * 3842
     */
    @Test
    public void testAliasInRowNumber() {
        String sqlQuery = String.format(
                "SELECT value AS v " +
                "FROM '%s' " +
                "ORDER BY CAST(v AS string)",
                METRIC_NAME
        );

        String[][] expectedRows = new String[][] {
                {"1"}, {"2"}, {"3"}, {"4"}, {"5"}
        };

        assertSqlQueryRows("Wrong result with alias inside ROW_NUMBER", expectedRows, sqlQuery);
    }
}
