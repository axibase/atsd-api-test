package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class DetailInterpolationTest extends SqlTest {
    private final String TEST_ENTITY1 = entity();
    private final String TEST_ENTITY2 = entity();

    private final String TEST_METRIC1 = metric();
    private final String TEST_METRIC2 = metric();

    private final String TEST_METRIC3 = metric();
    private final String TEST_METRIC4 = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        Series series = new Series(TEST_ENTITY1, TEST_METRIC1, Mocks.TAGS);
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T23:50:00Z", 1),
                Sample.ofDateInteger("2017-01-03T00:00:00Z", 3),
                Sample.ofDateInteger("2017-01-05T00:00:00Z", 5)
        );
        seriesList.add(series);

        series = new Series(TEST_ENTITY1, TEST_METRIC2, Mocks.TAGS);
        series.addSamples(
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-04T00:00:00Z", 4),
                Sample.ofDateInteger("2017-01-05T00:10:00Z", 6)
        );
        seriesList.add(series);

        series = new Series(TEST_ENTITY1, TEST_METRIC3, Mocks.TAGS);
        series.addSamples(
                Sample.ofDateInteger("2016-12-31T23:50:00Z", 0),
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("2017-01-03T00:00:00Z", 3)
        );
        seriesList.add(series);

        series = new Series(TEST_ENTITY1, TEST_METRIC4, Mocks.TAGS);
        series.addSamples(
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-04T00:00:00Z", 4),
                Sample.ofDateInteger("2017-01-04T00:10:00Z", 5)
        );
        seriesList.add(series);

        series = new Series(TEST_ENTITY2, TEST_METRIC4, Mocks.TAGS);
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-03T00:00:00Z", 3),
                Sample.ofDateInteger("2017-01-04T00:00:00Z", 4)
        );
        seriesList.add(series);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoin() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "null", "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "3"},
                {"2017-01-04T00:00:00.000Z", "4",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinLinear() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "null", "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "3"},
                {"2017-01-04T00:00:00.000Z", "4",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Issue("4814")
    @Test
    public void testDetailOuterJoinPrevious() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "null", "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "2"},
                {"2017-01-04T00:00:00.000Z", "3",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "4"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinLinearInner() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, INNER)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "null", "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "3"},
                {"2017-01-04T00:00:00.000Z", "4",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Issue("4814")
    @Test
    public void testDetailOuterJoinPreviousInner() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS, INNER)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "null", "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "2"},
                {"2017-01-04T00:00:00.000Z", "3",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "4"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinLinearOuter() {
        String sqlQuery = String.format(
                "SELECT datetime, ROUND(m1.value, 3), ROUND(m2.value, 3) " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, OUTER)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "1.014",   "2"},
                {"2017-01-03T00:00:00.000Z", "3",       "3"},
                {"2017-01-04T00:00:00.000Z", "4",       "4"},
                {"2017-01-05T00:00:00.000Z", "5",       "5.986"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinPreviousOuter() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS, OUTER)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "1",    "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "2"},
                {"2017-01-04T00:00:00.000Z", "3",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "4"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinLinearInnerNan() {
        String sqlQuery = String.format(
                "SELECT datetime, ROUND(m1.value, 3), m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, INNER, VALUE NAN)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "NaN", "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "3"},
                {"2017-01-04T00:00:00.000Z", "4",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "NaN"}
        };


        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinLinearOuterNan() {
        String sqlQuery = String.format(
                "SELECT datetime, ROUND(m1.value, 3), ROUND(m2.value, 3) " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, OUTER, FALSE)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "1.014", "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "3"},
                {"2017-01-04T00:00:00.000Z", "4",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "5.986"}
        };


        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Issue("4814")
    @Test
    public void testDetailOuterJoinPreviousInnerNan() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS, INNER, VALUE NAN)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "NaN",  "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "2"},
                {"2017-01-04T00:00:00.000Z", "3",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "4"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinPreviousOuterNan() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-03T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS, OUTER, FALSE)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-03T00:00:00.000Z", "3",    "2"},
                {"2017-01-04T00:00:00.000Z", "3",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "4"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinLinearInnerExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, ROUND(m1.value, 3), m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, INNER, TRUE)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "3",       "2"},
                {"2017-01-03T00:00:00.000Z", "3",       "3"},
                {"2017-01-04T00:00:00.000Z", "3",       "4"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Issue("4803")
    @Test
    public void testDetailOuterJoinLinearInnerValue() {
        String sqlQuery = String.format(
                "SELECT datetime, ROUND(m1.value, 3), m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, INNER, VALUE 5)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "5",       "2"},
                {"2017-01-03T00:00:00.000Z", "3",       "3"},
                {"2017-01-04T00:00:00.000Z", "5",       "4"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinLinearOuterExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, ROUND(m1.value, 3), ROUND(m2.value, 3) " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, OUTER, TRUE)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T23:50:00.000Z", "1",       "2"},
                {"2017-01-02T00:00:00.000Z", "1.014",   "2"},
                {"2017-01-03T00:00:00.000Z", "3",       "3"},
                {"2017-01-04T00:00:00.000Z", "4",       "4"},
                {"2017-01-05T00:00:00.000Z", "5",       "5.986"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Issue("4803")
    @Test
    public void testDetailOuterJoinLinearOuterValue() {
        String sqlQuery = String.format(
                "SELECT datetime, ROUND(m1.value, 3), ROUND(m2.value, 3) " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, OUTER, VALUE 6)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T23:50:00.000Z", "1",       "6"},
                {"2017-01-02T00:00:00.000Z", "1.014",   "2"},
                {"2017-01-03T00:00:00.000Z", "3",       "3"},
                {"2017-01-04T00:00:00.000Z", "4",       "4"},
                {"2017-01-05T00:00:00.000Z", "5",       "5.986"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinPreviousOuterExtend() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS, OUTER, TRUE)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T23:50:00.000Z", "1",    "2"},
                {"2017-01-02T00:00:00.000Z", "1",    "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "2"},
                {"2017-01-04T00:00:00.000Z", "3",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "4"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Issue("4803")
    @Test
    public void testDetailOuterJoinPreviousOuterValue() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS, OUTER, VALUE 6)",
                TEST_METRIC1, TEST_METRIC2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T23:50:00.000Z", "1",    "6"},
                {"2017-01-02T00:00:00.000Z", "1",    "2"},
                {"2017-01-03T00:00:00.000Z", "3",    "2"},
                {"2017-01-04T00:00:00.000Z", "3",    "4"},
                {"2017-01-05T00:00:00.000Z", "5",    "4"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinUsingEntityLinearInner() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, ROUND(m2.value, 3) " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN USING ENTITY \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-04T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, INNER)",
                TEST_METRIC3, TEST_METRIC4
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z", "1",       "null"},
                {"2017-01-01T00:00:00.000Z", "null",    "1"},
                {"2017-01-02T00:00:00.000Z", "2",       "2"},
                {"2017-01-02T00:00:00.000Z", "null",    "2"},
                {"2017-01-03T00:00:00.000Z", "3",       "3"},
                {"2017-01-03T00:00:00.000Z", "null",    "3"},
                {"2017-01-04T00:00:00.000Z", "null",    "4"},
                {"2017-01-04T00:00:00.000Z", "null",    "4"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testDetailOuterJoinUsingEntityPreviousInnerNaNAdditionalFilter() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, ROUND(m2.value, 3) " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN USING ENTITY \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-04T00:00:00Z' AND " +
                            "m1.entity = '%s' " +
                        "WITH INTERPOLATE (DETAIL, LINEAR, INNER, VALUE NAN)",
                TEST_METRIC3, TEST_METRIC4, TEST_ENTITY2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z", "NaN",    "1"},
                {"2017-01-02T00:00:00.000Z", "NaN",    "2"},
                {"2017-01-03T00:00:00.000Z", "NaN",    "3"},
                {"2017-01-04T00:00:00.000Z", "NaN",    "4"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("3435")
    @Test
    public void testNonAcceptableCalendarAlignment() throws Exception {
        String sqlQuery = String.format(
                "SELECT datetime, m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-05T00:00:00Z' " +
                        "WITH INTERPOLATE (DETAIL, PREVIOUS, OUTER, TRUE, START_TIME)",
                TEST_METRIC1, TEST_METRIC2
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest(
                "Syntax error at line 1 position 316: no viable alternative at input 'WITH INTERPOLATE (DETAIL, PREVIOUS, OUTER, TRUE,'",
                response);
    }
}
