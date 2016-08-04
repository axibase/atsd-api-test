package com.axibase.tsd.api.method.sql.orderby;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlOrderByAlphabeticalTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-order-by-alphabetical-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final List<String> NAMES = Arrays.asList(" ", " .",
            "b", "U", "C", "z", "a", "T", "A", "t", "Б", "Ё", "б", "В", "ж", "З", "к", "0", "1", " ", " .",
            "01", "11", "10", "AB", "á", "ä", "é", "ÿ", "ǎ", "ā", "а", "α", "resume", "résumé",
            "Résumé", "Resumes", "resumes", "résumés", "a¨b", "äa", "äc"
    );

    @BeforeClass
    public static void prepareData() {
        Series series = new Series();
        series.setMetric(TEST_METRIC_NAME);
        int i = 0;
        for (final String name : NAMES) {
            series.setEntity((TEST_PREFIX + name).replace(" ", ""));
            series.setTags(new HashMap<String, String>() {{
                put("tag", name);
            }});
            sendSamplesToSeries(series,
                    new Sample("2016-06-03T09:24:00.000Z", i));
            i++;
        }
    }

    /*
    Following tests related to issue #3162
     */

    /**
     * Issue #3162
     * Will fail until #3164 won't be fixed
     */
    @Test
    void testOrderByEntityTagNameASC() {
        String sqlQuery =
                "SELECT tags.tag FROM '" + TEST_METRIC_NAME + "'\n" +
                        "ORDER BY tags.tag ASC";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        List<String> expectedColumn = sortList(NAMES, false);

        assertTableContainsColumnValues(expectedColumn, resultTable, "tags.tag");
    }

    /**
     * Issue #3162
     * Will fail until #3164 won't be fixed
     */
    @Test
    void testOrderByEntityTagNameDESC() {
        String sqlQuery =
                "SELECT tags.tag FROM '" + TEST_METRIC_NAME + "'\n" +
                        "ORDER BY tags.tag DESC";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        List<String> expectedColumn = sortList(NAMES, true);

        assertTableContainsColumnValues(expectedColumn, resultTable, "tags.tag");
    }


    /**
     * Issue #3162
     */
    @Test
    void testOrderByEntityASC() {
        String sqlQuery =
                "SELECT entity FROM '" + TEST_METRIC_NAME + "'\n" +
                        "ORDER BY entity ASC";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = sortList(generateEntityNames(), false);

        assertTableContainsColumnValues(expectedColumn, resultTable, "entity");
    }


    /**
     * Issue #3162
     */
    @Test
    void testOrderByEntityDESC() {
        String sqlQuery =
                "SELECT entity FROM '" + TEST_METRIC_NAME + "'\n" +
                        "ORDER BY entity DESC";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumn = sortList(generateEntityNames(), true);

        assertTableContainsColumnValues(expectedColumn, resultTable, "entity");
    }

    private List<String> generateEntityNames() {
        List<String> entityNames = new ArrayList<>();
        for (String name : NAMES) {
            entityNames.add(String.format("%s%s", TEST_PREFIX, name).toLowerCase().replace(" ", ""));
        }
        return entityNames;
    }

    private List<String> sortList(List<String> list, boolean reverse) {
        List<String> resultList = new ArrayList<>();
        resultList.addAll(list);
        Collections.sort(resultList);
        if (reverse) {
            Collections.reverse(resultList);
        }
        return resultList;
    }
}
