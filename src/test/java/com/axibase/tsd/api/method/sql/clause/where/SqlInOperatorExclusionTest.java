package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class SqlInOperatorExclusionTest extends SqlTest {
    private static final String ENTITY_1 = Mocks.entity();
    private static final String ENTITY_2 = Mocks.entity();
    private static final String ENTITY_3 = Mocks.entity();

    private static final String METRIC = Mocks.metric();

    private static final int VALUE_1 = 1;
    private static final int VALUE_2 = 2;
    private static final int VALUE_3 = 3;

    private static final String NONEXISTENT_ENTITY_1 = Mocks.entity();
    private static final String NONEXISTENT_ENTITY_2 = Mocks.entity();

    private static final int NONEXISTENT_VALUE_1 = 11;
    private static final int NONEXISTENT_VALUE_2 = 22;

    private static String DATE = Mocks.ISO_TIME;

    @BeforeClass
    private void prepareData() throws Exception{
        Series series1 = new Series()
                .setMetric(METRIC)
                .setEntity(ENTITY_1);
        series1.addSamples(Sample.ofDateInteger(DATE, VALUE_1));
        SeriesMethod.insertSeriesCheck(series1);

        Series series2 = new Series()
                .setMetric(METRIC)
                .setEntity(ENTITY_2);
        series2.addSamples(Sample.ofDateInteger(DATE, VALUE_2));
        SeriesMethod.insertSeriesCheck(series2);

        Series series3 = new Series()
                .setMetric(METRIC)
                .setEntity(ENTITY_3);
        series3.addSamples(Sample.ofDateInteger(DATE, VALUE_3));
        SeriesMethod.insertSeriesCheck(series3);
    }


    @Issue("6360")
    @Test(description = "Tests that if entities used by IN operator do not exist, response will be empty.")
    public void inOperatorAbsentEntitiesExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE entity IN ('%s', '%s')", METRIC, NONEXISTENT_ENTITY_1, NONEXISTENT_ENTITY_2);
        assertSqlQueryRows(String.format("%s expected to give nothing at response.", sqlQuery), new ArrayList<>(), sqlQuery); //checking that response is empty
    }

    @Issue("6360")
    @Test(description = "Tests that if values used by IN operator do not exist, response will be empty.")
    public void inOperatorAbsentValuesExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE value IN (%d, %d)", METRIC, NONEXISTENT_VALUE_1, NONEXISTENT_VALUE_2);
        assertSqlQueryRows(String.format("%s expected to give nothing at response.", sqlQuery), new ArrayList<>(), sqlQuery); //checking that response is empty
    }

    @Issue("6360")
    @Test(description = "Tests that if there is one existing entity used by IN operator, others will not return.")
    public void inOperatorNotRequestedEntitiesExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE entity IN ('%s')", METRIC, ENTITY_1);
        String[][] expectedResult = {
                {ENTITY_1, DATE, String.format("%d",VALUE_1)}
        };
        assertSqlQueryRows(String.format("%s expected to return one row.", sqlQuery), expectedResult, sqlQuery);
    }

    @Issue("6360")
    @Test(description = "Tests that if there are two existing entities used by IN operator, other will not return.")
    public void inOperatorNotRequestedEntityExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE entity IN ('%s', '%s')", METRIC, ENTITY_1, ENTITY_2);
        String[][] expectedResult = {
                {ENTITY_1, DATE, String.format("%d",VALUE_1)},
                {ENTITY_2, DATE, String.format("%d",VALUE_2)}
        };
        assertSqlQueryRows(String.format("%s expected to return two rows.", sqlQuery), expectedResult, sqlQuery);
    }

    @Issue("6360")
    @Test(description = "Tests that if there is one existing value used by IN operator, others will not return.")
    public void inOperatorNotRequestedValuesExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE value IN (%d)", METRIC, VALUE_1);
        String[][] expectedResult = {
                {ENTITY_1, DATE, String.format("%d",VALUE_1)}
        };
        assertSqlQueryRows(String.format("%s expected to return one row.", sqlQuery), expectedResult, sqlQuery);
    }

    @Issue("6360")
    @Test(description = "Tests that if there are two existing values used by IN operator, others will not return.")
    public void inOperatorNotRequestedValueExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE value IN (%d, %d)", METRIC, VALUE_1, VALUE_2);
        String[][] expectedResult = {
                {ENTITY_1, DATE, String.format("%d",VALUE_1)},
                {ENTITY_2, DATE, String.format("%d",VALUE_2)}
        };
        assertSqlQueryRows(String.format("%s expected to return two rows.", sqlQuery), expectedResult, sqlQuery);
    }

    @Issue("6360")
    @Test(description = "Tests that if one of entities used by IN operator exist and other does not, result is returned.")
    public void inOperatorExistingAndAbsentEntitiesExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE entity IN ('%s', '%s')", METRIC, ENTITY_1, NONEXISTENT_ENTITY_1);
        String[][] expectedResult = {
                {ENTITY_1, DATE, String.format("%d",VALUE_1)}
        };
        assertSqlQueryRows(String.format("%s expected to return one row.", sqlQuery), expectedResult, sqlQuery);
    }

    @Issue("6360")
    @Test(description = "Tests that if one of values used by IN operator exist and other does not, result is returned.")
    public void inOperatorExistingAndAbsentValuesExclusionTest() {
        String sqlQuery = String.format("SELECT entity, datetime, value FROM \"%s\" WHERE value IN (%d, %d)", METRIC, VALUE_1, NONEXISTENT_VALUE_1);
        String[][] expectedResult = {
                {ENTITY_1, DATE, String.format("%d",VALUE_1)}
        };
        assertSqlQueryRows(String.format("%s expected to return one row.", sqlQuery), expectedResult, sqlQuery);
    }
}
