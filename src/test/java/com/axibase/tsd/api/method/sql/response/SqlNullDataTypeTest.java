package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;


public class SqlNullDataTypeTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-data-type-null-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void initialize() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        seriesList.add(new Series(TEST_ENTITY_NAME, TEST_METRIC1_NAME) {{
            addSamples(new Sample("2016-06-29T08:00:00.000Z", 0));
        }});

        seriesList.add(new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME) {{
            addSamples(new Sample("2016-06-29T08:00:01.000Z", 0));
        }});

        SeriesMethod.insertSeriesCheck(seriesList);
    }


    /*
      Arithmetical function
     */

    /**
     * #2934
     */
    @Test
    public void testDivisionExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT t1.value, t2.value, t1.value/t2.value, t1.value/t1.value as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);
        assertEquals("null", resultTable.getValueAt(2, 0));
    }

    /**
     * #2934
     */
    @Test
    public void testExpressionNaNDataType() {
        final String sqlQuery = String.format(
                "SELECT t1.value, t2.value, t1.value/t2.value, t1.value/t1.value as nancol %n" +
                        "FROM '%s' t1 %nOUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);
        assertEquals("NaN", resultTable.getValueAt(3, 0));
    }


    /**
     * #2934
     */
    @Test
    public void testMinusExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT t1.value, t2.value, t1.value-t2.value, t1.value/t1.value as nancol %n" +
                        "FROM '%s' t1 %nOUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);
        assertEquals("null", resultTable.getValueAt(2, 0));
    }

    /**
     * #2934
     */
    @Test
    public void testPlusExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT t1.value, t2.value, t1.value+t2.value, t1.value/t1.value as nancol %n" +
                        "FROM '%s' t1 %nOUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        assertEquals("null", resultTable.getValueAt(2, 0));
    }


    /**
     * #2934
     */
    @Test
    public void testMultiplicationExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT t1.value, t2.value, t1.value*t2.value, t1.value/t1.value as nancol %n" +
                        "FROM '%s' t1 %nOUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);
        assertEquals("null", resultTable.getValueAt(2, 0));
    }


    /*
      Aggregate function
     */


    /**
     * #2934
     */
    @Test
    public void testCountExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  COUNT(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testSumExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  SUM(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testAvgExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  AVG(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testMinExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  Min(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testMaxExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  MAX(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testFirstExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  FIRST(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testCounterExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  COUNTER(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("NaN")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testDeltaExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  DELTA(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #2934
     */
    @Test
    public void testLastExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT  LAST(t2.value) as nancol %nFROM '%s' t1 %n" +
                        "OUTER JOIN '%s' t2 %nWHERE t1.entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.0")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

}