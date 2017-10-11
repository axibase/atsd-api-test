package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WhereDatesWithJoinTest extends SqlTest {
    private static final String ENTITY_NAME1 = Mocks.entity();
    private static final String ENTITY_NAME2 = Mocks.entity();
    private static final String LEFT_METRIC_NAME = Mocks.metric();
    private static final String RIGHT_METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
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
        Series series5 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME, "t1", "Tag1");
        series5.addSamples(Sample.ofDateInteger("2017-09-03T00:00:00.000Z", 5));

        Series series6 = new Series(ENTITY_NAME2, RIGHT_METRIC_NAME,"t2", "Tag2");
        series6.addSamples(Sample.ofDateInteger("2017-09-03T00:00:00.000Z", 6));

        /* Different entity, same tags */
        Series series7 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME, "t3", "Tag3");
        series7.addSamples(Sample.ofDateInteger("2017-09-04T00:00:00.000Z", 7));

        Series series8 = new Series(ENTITY_NAME2, RIGHT_METRIC_NAME, "t3", "Tag3");
        series8.addSamples(Sample.ofDateInteger("2017-09-04T00:00:00.000Z", 8));

        /* Same entity, different tags */
        Series series9 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME, "t1", "Tag1");
        series9.addSamples(Sample.ofDateInteger("2017-09-05T00:00:00.000Z", 9));

        Series series10 = new Series(ENTITY_NAME1, RIGHT_METRIC_NAME, "t2", "Tag2");
        series10.addSamples(Sample.ofDateInteger("2017-09-05T00:00:00.000Z", 10));

        /* Same entity, same tags */
        Series series11 = new Series(ENTITY_NAME1, LEFT_METRIC_NAME, "t3", "Tag3");
        series11.addSamples(Sample.ofDateInteger("2017-09-06T00:00:00.000Z", 11));

        Series series12 = new Series(ENTITY_NAME1, RIGHT_METRIC_NAME, "t3", "Tag3");
        series12.addSamples(Sample.ofDateInteger("2017-09-06T00:00:00.000Z", 12));


        SeriesMethod.insertSeriesCheck(
                series1, series2, series3, series4,
                series5, series6, series7, series8,
                series9, series10, series11, series12
        );
    }

    @DataProvider
    public Object[][] provideJoinTypeAndCondition() {
        return new Object[][] {
                {"JOIN", "t1.time = t2.time"},
                {"JOIN", "t1.time = t2.datetime"},
                {"JOIN", "t1.datetime = t2.datetime"},

                {"FULL JOIN", "t1.time = t2.time"},
                {"FULL JOIN", "t1.time = t2.datetime"},
                {"FULL JOIN", "t1.datetime = t2.datetime"},
        };
    }

    @Issue("4597")
    @Test(dataProvider = "provideJoinTypeAndCondition")
    public void testWhereTimeEqualityWithJoin(String joinType, String joinCondition) {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "%s \"%s\" t2 " +
                        "WHERE %s " +
                        "ORDER by time",
                LEFT_METRIC_NAME, joinType,
                RIGHT_METRIC_NAME, joinCondition
        );

        String[][] expectedRows = {
                {"2017-09-02T00:00:00.000Z",  "3",  "4"},
                {"2017-09-06T00:00:00.000Z", "11", "12"}
        };

        String assertMessage = String.format("Wrong result with join using entity and '%s' WHERE condtiton",
                joinCondition);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }

    @Issue("4597")
    @Test(dataProvider = "provideJoinTypeAndCondition")
    public void testWhereTimeEqualityWithJoinUsingEntity(String joinType, String joinCondition) {
        String sqlQuery = String.format(
                "SELECT datetime, t1.value, t2.value " +
                        "FROM \"%s\" t1 " +
                        "%s USING ENTITY \"%s\" t2 " +
                        "WHERE %s " +
                        "ORDER by time",
                LEFT_METRIC_NAME, joinType,
                RIGHT_METRIC_NAME, joinCondition
        );

        String[][] expectedRows = {
                {"2017-09-02T00:00:00.000Z",  "3",  "4"},
                {"2017-09-05T00:00:00.000Z",  "9", "10"},
                {"2017-09-06T00:00:00.000Z", "11", "12"}
        };

        String assertMessage = String.format("Wrong result with join using entity and '%s' WHEREcondtiton",
                joinCondition);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }
}
