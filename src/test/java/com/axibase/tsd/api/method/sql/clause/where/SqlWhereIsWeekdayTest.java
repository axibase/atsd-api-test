package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlWhereIsWeekdayTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME_1 = metric();
    private static final String METRIC_NAME_2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series seriesFirst = new Series(ENTITY_NAME, METRIC_NAME_1)
                .addSamples(Sample.ofDateInteger("2018-01-05T00:00:00Z", 5))
                .addSamples(Sample.ofDateInteger("2018-01-06T00:00:00Z", 6))
                .addSamples(Sample.ofDateInteger("2018-01-07T00:00:00Z", 7))
                .addSamples(Sample.ofDateInteger("2018-03-02T00:00:00Z", 2))
                .addSamples(Sample.ofDateInteger("2018-03-03T00:00:00Z", 3))
                .addSamples(Sample.ofDateInteger("2018-03-04T00:00:00Z", 4));

        final Series seriesSecond = new Series(ENTITY_NAME, METRIC_NAME_2)
                .addSamples(Sample.ofDateInteger("2018-01-05T00:00:00Z", 5))
                .addSamples(Sample.ofDateInteger("2018-01-06T00:00:00Z", 6))
                .addSamples(Sample.ofDateInteger("2018-01-07T00:00:00Z", 7))
                .addSamples(Sample.ofDateInteger("2018-03-02T00:00:00Z", 2))
                .addSamples(Sample.ofDateInteger("2018-03-03T00:00:00Z", 3))
                .addSamples(Sample.ofDateInteger("2018-03-04T00:00:00Z", 4));

        SeriesMethod.insertSeriesCheck(seriesFirst, seriesSecond);
    }

    @Test(description = "Test WHERE and HAVING with every possible clause in one query")
    public void testEveryClause() {
        final String query = String.format(
                "select is_workday(datetime, 'RUS'), is_weekday(dateadd(day, -1, datetime), 'RUS'), \n" +
                        "       is_weekday(dateadd(month, 1, datetime), 'RUS'), is_workday(datetime, 'RUS'), \n" +
                        "       datetime\n" +
                        "from (\n" +
                        "    select datetime from \"%s\" t1 \n" +
                        "    join using entity \"%s\" t2\n" +
                        "    where datetime between date_parse('2018', 'yyyy') and date_parse('2019', 'yyyy')\n" +
                        "    and is_weekday(dateadd(day, -1, datetime), 'RUS')\n" +
                        "    and not is_weekday(datetime, 'RUS')\n" +
                        "    with row_number(t1.entity order by time desc) >= 1\n" +
                        "    group by period(1 day, LINEAR)\n" +
                        "    having not is_workday(datetime, 'RUS')\n" +
                        "    and is_weekday(dateadd(month, 1, datetime), 'RUS')\n" +
                        ")\n" +
                        "group by period(1 day, PREVIOUS, EXTEND)\n" +
                        "with time >= last_time - 1*MONTH\n" +
                        "order by is_weekday(datetime, 'ISR')\n" +
                        "limit 10\n", METRIC_NAME_1, METRIC_NAME_2);
        final String[][] expectedRows = {
                {"false", "true", "true", "false", "2018-01-06T00:00:00.000Z"},
                {"false", "true", "true", "false", "2018-03-03T00:00:00.000Z"},
        };
        assertSqlQueryRows("Fail to execute a big query (anything could go wrong)", expectedRows, query);
    }
}
