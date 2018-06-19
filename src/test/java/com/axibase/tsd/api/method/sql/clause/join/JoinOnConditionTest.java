package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class JoinOnConditionTest extends SqlTest {
    private static final String ENTITY_NAME1 = Mocks.entity();
    private static final String ENTITY_NAME2 = Mocks.entity();
    private static final String LEFT_METRIC_NAME = Mocks.metric();
    private static final String RIGHT_METRIC_NAME = Mocks.metric();
    private static final String[] ENTITY_NAME_THREE = {Mocks.entity(), Mocks.entity(), Mocks.entity()};
    private static final String[] METRIC_NAME_THREE = {Mocks.metric(), Mocks.metric(), Mocks.metric()};

    @BeforeClass
    public static void prepareDataSingleJoin() throws Exception {
        /* Different entity, no tags */
        Series series1 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME);
        series1.addSamples(Sample.ofDateInteger("2017-09-01T00:00:00.000Z", 1));

        Series series2 = new Series(ENTITY_NAME2, RIGHT_METRIC_NAME);
        series2.addSamples(Sample.ofDateInteger("2017-09-01T00:00:00.000Z", 2));

        /* Same entity, no tags */
        Series series3 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME);
        series3.addSamples(Sample.ofDateInteger("2017-09-02T00:00:00.000Z", 3));

        Series series4 = new Series(ENTITY_NAME1, RIGHT_METRIC_NAME);
        series4.addSamples(Sample.ofDateInteger("2017-09-02T00:00:00.000Z", 4));

        /* Different entity, different tags */
        Series series5 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME);
        series5.addTag("t1", "Tag1");
        series5.addSamples(Sample.ofDateInteger("2017-09-03T00:00:00.000Z", 5));

        Series series6 = new Series(ENTITY_NAME2, RIGHT_METRIC_NAME);
        series5.addTag("t2", "Tag2");
        series6.addSamples(Sample.ofDateInteger("2017-09-03T00:00:00.000Z", 6));

        /* Different entity, same tags */
        Series series7 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME);
        series7.addTag("t3", "Tag3");
        series7.addSamples(Sample.ofDateInteger("2017-09-04T00:00:00.000Z", 7));

        Series series8 = new Series(ENTITY_NAME2, RIGHT_METRIC_NAME);
        series8.addTag("t3", "Tag3");
        series8.addSamples(Sample.ofDateInteger("2017-09-04T00:00:00.000Z", 8));

        /* Same entity, different tags */
        Series series9 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME);
        series9.addTag("t1", "Tag1");
        series9.addSamples(Sample.ofDateInteger("2017-09-05T00:00:00.000Z", 9));

        Series series10 = new Series(ENTITY_NAME1, RIGHT_METRIC_NAME);
        series10.addTag("t2", "Tag2");
        series10.addSamples(Sample.ofDateInteger("2017-09-05T00:00:00.000Z", 10));

        /* Same entity, same tags */
        Series series11 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME);
        series11.addTag("t3", "Tag3");
        series11.addSamples(Sample.ofDateInteger("2017-09-06T00:00:00.000Z", 11));

        Series series12 = new Series(ENTITY_NAME1, RIGHT_METRIC_NAME);
        series12.addTag("t3", "Tag3");
        series12.addSamples(Sample.ofDateInteger("2017-09-06T00:00:00.000Z", 12));

        SeriesMethod.insertSeriesCheck(
                series1, series2, series3, series4,
                series5, series6, series7, series8,
                series9, series10, series11, series12
        );
    }

    @BeforeClass
    public static void prepareDataDoubleJoin() throws Exception {
        /* All possible partition of three elements (entities/tags) into different sets */
        final int[][] partitions = {
                {0, 0, 0}, /* All in single set */
                {1, 0, 0}, /* One in another set */
                {0, 1, 0},
                {0, 0, 1},
                {0, 1, 2}, /* All elements in their own sets*/
        };

        int day = 0, k = 0;
        List<Series> seriesList = new ArrayList<>();
        for (int[] partition : partitions) {
            for (int tagsPartitionId = 0; tagsPartitionId <= partitions.length; tagsPartitionId++) {
                day++;
                for (int metricId = 0; metricId < 3; metricId++) {
                    int entityId = partition[metricId];
                    Series series = new Series(ENTITY_NAME_THREE[entityId], METRIC_NAME_THREE[metricId]);
                    if (tagsPartitionId < partitions.length) {
                        int tagId = partitions[tagsPartitionId][metricId];
                        series.addTag("t" + tagId, "Tag" + tagId);
                    }
                    String date = String.format("2017-09-%02dT00:00:00.000Z", day);
                    series.addSamples(Sample.ofDateInteger(date, ++k));
                    seriesList.add(series);
                }
            }
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    private static final JoinCondition[] COMBINATIONS = {
            new JoinCondition(JoinType.JOIN,              JoinType.JOIN),
            new JoinCondition(JoinType.JOIN,              JoinType.JOIN_USING_ENTITY),
            new JoinCondition(JoinType.JOIN_USING_ENTITY, JoinType.JOIN),
            new JoinCondition(JoinType.JOIN_USING_ENTITY, JoinType.JOIN_USING_ENTITY)
    };

    @DataProvider
    public static Object[][] provideConditions() {
        Object[][] result = new Object[COMBINATIONS.length][];
        for (int i = 0; i < COMBINATIONS.length; i++) {
            result[i] = new Object[]{COMBINATIONS[i]};
        }
        return result;
    }

    @Issue("4397")
    @Test(dataProvider = "provideConditions", description = "tests if explicit JOIN implemented properly")
    public void testJoinOnExplicitCondition(JoinCondition condition) {
        // arrange
        final String[][] joinResult = {
                { "3",  "4", "2017-09-02T00:00:00.000Z"},
                {"11", "12", "2017-09-06T00:00:00.000Z"},
        };

        final String[][] joinUsingEntityResult = {
                { "3",  "4", "2017-09-02T00:00:00.000Z"},
                { "9", "10", "2017-09-05T00:00:00.000Z"},
                {"11", "12", "2017-09-06T00:00:00.000Z"},
        };

        String sqlQuery = String.format(
                "SELECT t1.value, t2.value, datetime " +
                        "FROM \"%1$s\" t1 " +
                        "%3$s \"%2$s\" t2 " +
                        "ON (%4$s)",
                LEFT_METRIC_NAME, RIGHT_METRIC_NAME,
                condition.implicit.syntax,
                condition.explicit.expressionFor("t1", "t2")
        );

        // action
        String[][] expectedResult =
                JoinType.JOIN == condition.explicit ? joinResult : joinUsingEntityResult;

        String assertMessage = String.format("Wrong result of JOIN: implicit condition is %s (%s)," +
                        " explicit condition is %s (%s)",
                condition.implicit.syntax, condition.implicit.expressionFor("t1", "t2"),
                condition.explicit.syntax, condition.explicit.expressionFor("t1", "t2"));

        // assert
        assertSqlQueryRows(assertMessage, expectedResult, sqlQuery);
    }

    @Issue("4397")
    @Test(dataProvider = "provideConditions", description = "tests if double JOIN (explicit and implicit) implemented properly")
    public void testDoubleJoinOnExplicitCondition(JoinCondition condition) {
        // arrange
        final String sqlQuery = String.format(
                "SELECT t1.value, t2.value, t3.value, datetime " +
                        "FROM \"%1$s\" t1 " +
                        "%4$s \"%2$s\" t2 " +
                        "ON (%5$s) " +
                        "%6$s \"%3$s\" t3 " +
                        "ON (%7$s)",
                METRIC_NAME_THREE[0], METRIC_NAME_THREE[1], METRIC_NAME_THREE[2],
                condition.implicit.syntax, condition.explicit.expressionFor("t1", "t2"),
                condition.implicit.syntax, condition.explicit.expressionFor("t2", "t3")
        );
        final String assertMessage = "Double JOIN ON explicit condition should cause an error";
        final String expectedMessage = "Unexpected ON clause expression: \'t2.entity = t3.entity\'";

        // assert
        assertBadRequest(assertMessage, expectedMessage, sqlQuery);
    }

    @Issue("4397")
    @Test(description = "tests if always true condition in ON clause causes an error")
    public void testJoinAlwaysTrueCondition() {
        // arrange
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "ON (t1.entity = t2.entity AND t1.time = t1.time AND t2.entity = t2.entity)",
                LEFT_METRIC_NAME, RIGHT_METRIC_NAME
        );

        // assert
        assertBadSqlRequest("Unexpected ON clause expression: 't1.time = t1.time'", sqlQuery);
    }

    @Issue("4397")
    @Test(description = "tests if extra condition in ON clause causes an error")
    public void testJoinExtraConditions() {
        // arrange
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "ON (t1.entity = t2.entity AND t1.time = t2.time AND t1.value = t2.value)",
                LEFT_METRIC_NAME, RIGHT_METRIC_NAME
        );

        // assert
        assertBadSqlRequest("Unexpected ON clause expression: 't1.value = t2.value'", sqlQuery);
    }

    @Issue("4397")
    @Test(description = "tests if cross condition in ON clause causes an error")
    public void testJoinCrossConditions() {
        // arrange
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value, t3.value, datetime " +
                        "FROM \"%s\" t1 " +
                        "JOIN \"%s\" t2 " +
                        "ON (t1.time = t3.time AND t2.entity = t3.entity) " +
                        "JOIN \"%s\" t3 " +
                        "ON (t2.time = t3.time AND t1.entity = t3.entity)",
                METRIC_NAME_THREE[0], METRIC_NAME_THREE[1], METRIC_NAME_THREE[2]
        );

        // assert
        assertBadSqlRequest("Unexpected ON clause expression: 't1.time = t3.time'", sqlQuery);
    }

    @AllArgsConstructor
    private enum JoinType {
        JOIN(
                "JOIN",
                "%1$s.entity = %2$s.entity AND %1$s.time = %2$s.time AND %1$s.tags = %2$s.tags"
        ),
        JOIN_USING_ENTITY(
                "JOIN USING ENTITY",
                "%1$s.entity = %2$s.entity AND %1$s.time = %2$s.time"
        );

        String syntax;
        String expressionTemplate;

        String expressionFor(String t1, String t2) {
            return String.format(expressionTemplate, t1, t2);
        }
    }

    @AllArgsConstructor
    @Data
    static class JoinCondition {
        JoinType implicit;
        JoinType explicit;
    }
}
