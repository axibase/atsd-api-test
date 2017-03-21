package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CaseOperatorTest extends SqlTest {
    private final Series DEFAULT_SERIES = Mocks.series();
    {
        DEFAULT_SERIES.setData(new ArrayList<Sample>());
    }
    private final String[] SERIES_VALUES = { "10", "0", "-5" };

    @BeforeClass
    public void insertDefaultSeriesData() throws Exception {
        final String ISO_MINUTES_FORMAT = "2016-06-03T09:%02d:00.000Z";

        int minutes = 0;
        for (int i = 0; i < SERIES_VALUES.length; i++) {
            String time = String.format(ISO_MINUTES_FORMAT, minutes);
            Sample sample = new Sample(time, new BigDecimal(SERIES_VALUES[i]));
            DEFAULT_SERIES.addData(sample);
            minutes +=5;
        }

        SeriesMethod.insertSeriesCheck(DEFAULT_SERIES);
    }

    /**
     * #3981
     */
    @Test
    public void testSimpleCaseOperator() {
        final String sql = String.format(
                "SELECT CASE WHEN value > 0 THEN 'high' ELSE 'low' END%n" +
                "FROM '%s'%n" +
                "ORDER BY datetime ASC",
                DEFAULT_SERIES.getMetric()
        );

        final String[][] expected = {
                { "high" }, // 10
                { "low" },  // 0
                { "low" }   // -5
        };

        assertSqlQueryRows("Bad CASE... result", expected, sql);
    }

    /**
     * #3981
     */
    @Test
    public void testCaseOperator() {
        final String sql = String.format(
                "SELECT CASE WHEN value > 0 THEN 'high' ELSE 'low' END%n" +
                "FROM '%s'%n" +
                "ORDER BY datetime ASC",
                DEFAULT_SERIES.getMetric()
        );

        final String[][] expected = {
                { "high" }, // 10
                { "low" },  // 0
                { "low" }   // -5
        };

        assertSqlQueryRows("Bad CASE... result", expected, sql);
    }

    /**
     * #3981
     */
    @Test
    public void testCaseOperatorInNumericFunction() {
        final String sql = String.format(
                "SELECT SUM(CASE WHEN value > 0 THEN 100 ELSE 10 END)%n" +
                "FROM '%s'",
                DEFAULT_SERIES.getMetric()
        );

        final String[][] expected = {
                { "120" }   // 100 * 1 + 10 * 2
        };

        assertSqlQueryRows("Bad apply SUM to CASE...", expected, sql);
    }

    /**
     * #3981
     */
    @Test
    public void testCaseOperatorInStringFunction() {
        final String sql = String.format(
                "SELECT UPPER(CASE WHEN value > 0 THEN 'high' ELSE 'low' END)%n" +
                "FROM '%s'%n" +
                "ORDER by datetime ASC",
                DEFAULT_SERIES.getMetric()
        );

        final String[][] expected = {
                { "HIGH" }, // 10
                { "LOW" },  // 0
                { "LOW" }   // -5
        };

        assertSqlQueryRows("Bad apply UPPER to CASE...", expected, sql);
    }

    /**
     * #3981
     */
    @Test
    public void testAggregateCaseOperatorAsColumn() {
        final String sql = String.format(
                "SELECT (CASE WHEN value > 0 THEN 100 ELSE 10 END) AS 't', SUM(t)%n" +
                "FROM '%s'",
                DEFAULT_SERIES.getMetric()
        );

        final String[][] expected = {
                { "120" }   // 100 * 1 + 10 * 2
        };

        assertSqlQueryRows("Bad apply SUM to CASE... as separate column", expected, sql);
    }
}
