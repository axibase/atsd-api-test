package com.axibase.tsd.api.method.sql.examples;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlFilterByTagExampleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-filter-by-tags-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        series.addTag("file_system", "/dev/mapper/vg_nurswgvml007-lv_root");
        sendSamplesToSeries(series,
                new Sample(1446033205000L, "72.2738"),
                new Sample(1446033220000L, "72.275"),
                new Sample(1446033236000L, "72.2697"),
                new Sample(1446033282000L, "72.2749"),
                new Sample(1446033297000L, "72.2763"),
                new Sample(1446033312000L, "72.2706"),
                new Sample(1446033327000L, "72.2722"),
                new Sample(1446033342000L, "72.2736"),
                new Sample(1446033357000L, "72.2747")
        );
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("file_system", "a");
        }}));
        sendSamplesToSeries(series,
                new Sample(1446033342002L, "72.27556")
        );
    }

    /**
     * Issue #3047
     * Test for filter by tag documentation example.
     *
     * @see <a href="Filter By Tag">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/filter-by-tag.md</a>
     */
    @Test
    public void testDateTimeFormat() {
        String sqlQuery =
                "SELECT time, value, tags.file_system \n" +
                        "FROM 'sql-example-filter-by-tags-metric' \n" +
                        "WHERE entity = 'sql-example-filter-by-tags-entity'\n" +
                        "AND tags.file_system LIKE '/d%' \n" +
                        "AND time between 1446033205000 AND 1446033357001";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("1446033205000", "72.2738", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033220000", "72.275", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033236000", "72.2697", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033282000", "72.2749", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033297000", "72.2763", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033312000", "72.2706", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033327000", "72.2722", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033342000", "72.2736", "/dev/mapper/vg_nurswgvml007-lv_root"),
                Arrays.asList("1446033357000", "72.2747", "/dev/mapper/vg_nurswgvml007-lv_root")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
