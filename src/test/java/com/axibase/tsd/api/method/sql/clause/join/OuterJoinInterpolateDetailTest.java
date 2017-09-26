package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OuterJoinInterpolateDetailTest extends SqlTest {
    private static final String ENTITY_NAME1 = Mocks.entity();
    private static final String ENTITY_NAME2 = Mocks.entity();
    private static final String METRIC_NAME1 = Mocks.metric();
    private static final String METRIC_NAME2 = Mocks.metric();
    private static final String METRIC_NAME3 = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Sample[] samples = {
                Sample.ofDateInteger("2017-06-22T12:00:00.000Z", 1),
                Sample.ofDateInteger("2017-06-22T13:00:00.000Z", 1),
                Sample.ofDateInteger("2017-06-22T14:00:00.000Z", 1)
        };

        Series series1 = new Series(ENTITY_NAME1, METRIC_NAME1);
        series1.addSamples(samples);

        Series series2 = new Series(ENTITY_NAME2, METRIC_NAME2);
        series2.addSamples(samples);

        Series series3 = new Series(ENTITY_NAME1, METRIC_NAME3);
        series3.addSamples(samples);

        Series series4 = new Series(ENTITY_NAME2, METRIC_NAME3);
        series4.addSamples(samples);

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4);
    }

    @Test
    public void testOuterJoinInterpolateDetailDifferentEntity() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.entity, m2.entity " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WITH INTERPOLATE(DETAIL) " +
                        "ORDER BY time, m1.entity",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedResult = {
                {"2017-06-22T12:00:00.000Z", "null", ENTITY_NAME2},
                {"2017-06-22T12:00:00.000Z", ENTITY_NAME1, "null"},

                {"2017-06-22T13:00:00.000Z", "null", ENTITY_NAME2},
                {"2017-06-22T13:00:00.000Z", ENTITY_NAME1, "null"},

                {"2017-06-22T14:00:00.000Z", "null", ENTITY_NAME2},
                {"2017-06-22T14:00:00.000Z", ENTITY_NAME1, "null"},
        };

        assertSqlQueryRows("", expectedResult, sqlQuery);
    }

    @Test
    public void testOuterJoinInterpolateDetailSameEntity() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.entity, m2.entity " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WITH INTERPOLATE(DETAIL) " +
                        "ORDER BY time, m1.entity",
                METRIC_NAME1,
                METRIC_NAME3
        );

        String[][] expectedResult = {
                {"2017-06-22T12:00:00.000Z", "null", ENTITY_NAME2},
                {"2017-06-22T12:00:00.000Z", ENTITY_NAME1, ENTITY_NAME1},

                {"2017-06-22T13:00:00.000Z", "null", ENTITY_NAME2},
                {"2017-06-22T13:00:00.000Z", ENTITY_NAME1, ENTITY_NAME1},

                {"2017-06-22T14:00:00.000Z", "null", ENTITY_NAME2},
                {"2017-06-22T14:00:00.000Z", ENTITY_NAME1, ENTITY_NAME1},
        };

        assertSqlQueryRows("", expectedResult, sqlQuery);
    }
}
