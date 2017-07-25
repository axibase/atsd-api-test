package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereTagInListTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[][] tags = {
                {"t1", "a"},
                {"t1", "b"},
                {"t1", "a"},
                {"t2", "c"},
                {"t1", "d"},
                {null, null},
        };

        for (int i = 0; i < tags.length; i++) {
            Series series = new Series(ENTITY_NAME, METRIC_NAME);
            series.addSamples(new Sample(String.format("2017-01-0%dT12:00:00.000Z", i + 1), i + 1));

            if (tags[i][0] != null) {
                series.addTag(tags[i][0], tags[i][1]);
            }

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #4047
     */
    @Test
    public void testWhereTagInList() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE tags.t1 IN ('a', 'b')",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"}, {"2"}, {"3"}
        };

        assertSqlQueryRows("Wrong result with WHERE <tag> IN <list>", expectedRows, sqlQuery);
    }

    /**
     * #4047
     */
    @Test
    public void testWhereTagNotInList() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE tags.t1 NOT IN ('a', 'b')",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"5"}
        };

        assertSqlQueryRows("Wrong result with WHERE <tag> NOT IN <list>", expectedRows, sqlQuery);
    }
}
