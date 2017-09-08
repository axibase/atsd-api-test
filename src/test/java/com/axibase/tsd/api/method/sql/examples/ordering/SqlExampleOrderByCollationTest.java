package com.axibase.tsd.api.method.sql.examples.ordering;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlExampleOrderByCollationTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-order-by-collation-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    private static final List<String> NAMES = Arrays.asList(null,
            "b", "U", "C", "z", "a", "T", "A", "t", "Б", "Ё", "б", "В", "ж", "З", "к", "0", "1", " .",
            "01", "11", "10", "AB", "á", "ä", "é", "ÿ", "ǎ", "ā", "а", "α", "resume", "résumé",
            "Résumé", "Resumes", "resumes", "résumés", "a¨b", "äa", "äc"
    );

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        Series nullSeries = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        nullSeries.addSamples(Sample.ofDateInteger("2016-06-03T09:24:00.000Z", 0));
        seriesList.add(nullSeries);

        Series series;
        int i = 1;
        for (final String name : NAMES) {
            if (name != null) {
                final int value = i;
                series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME, "tag", name);
                series.addSamples(Sample.ofDateInteger("2016-06-03T09:24:00.000Z", value));
                seriesList.add(series);
                i++;
            }
        }
        SeriesMethod.insertSeriesCheck(seriesList);
    }


    /**
     * #3162
     */
    @Test
    public void testOrderByEntityTagNameASC() {
        String sqlQuery =
                "SELECT tags.tag FROM \"" + TEST_METRIC_NAME + "\"\n" +
                        "ORDER BY tags.tag ASC";
        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);
        List<String> expectedColumn = sortList(trimList(NAMES), false);
        assertTableContainsColumnValues(expectedColumn, resultTable, "tags.tag");
    }

    /**
     * #3162
     */
    @Test
    public void testOrderByEntityTagNameDESC() {
        String sqlQuery =
                "SELECT tags.tag FROM \"" + TEST_METRIC_NAME + "\"\n" +
                        "ORDER BY tags.tag DESC";
        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);
        List<String> expectedColumn = sortList(trimList(NAMES), true);
        assertTableContainsColumnValues(expectedColumn, resultTable, "tags.tag");
    }


    private List<String> trimList(List<String> list) {
        List<String> resultList = new ArrayList<>();
        for (String s : list) {
            if (s == null) {
                resultList.add(null);
                continue;
            }
            String trimmedString = s.trim();
            if (!trimmedString.isEmpty()) {
                resultList.add(trimmedString);
            }
        }
        return resultList;
    }

    private List<String> sortList(List<String> list, boolean reverse) {
        List<String> resultList = new ArrayList<>();
        resultList.addAll(list);
        resultList.sort((o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.compareTo(o2);
        });

        if (reverse) {
            Collections.reverse(resultList);
        }

        for (int i = 0; i < resultList.size(); i++) {
            String e = resultList.get(i);
            if (e == null) {
                resultList.set(i, "null");
            }
        }
        return resultList;
    }
}
