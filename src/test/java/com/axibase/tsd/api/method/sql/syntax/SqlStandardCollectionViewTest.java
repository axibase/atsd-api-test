package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;


public class SqlStandardCollectionViewTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-standard-collection-view-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private static final String TEST_ENTITY_GROUP1_NAME = TEST_PREFIX + "entity-group-1";
    private static final String TEST_ENTITY_GROUP2_NAME = TEST_PREFIX + "entity-group-2";


    private static final Map<String, String> TAGS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("b", "a");
        put("a", "b");
        put("Tag", "VaLue");
    }});


    @BeforeClass
    public static void prepareData() throws Exception {
        //Series data
        List<Series> seriesList = new ArrayList<>();
        seriesList.add(new Series(TEST_ENTITY1_NAME, TEST_METRIC1_NAME, TAGS) {{
            addSamples(new Sample("2016-06-29T08:00:00.000Z", 0));
        }});

        seriesList.add(new Series(TEST_ENTITY2_NAME, TEST_METRIC2_NAME) {{
            addSamples(new Sample("2016-06-29T08:00:00.000Z", 1));
        }});

        //Entity data
        EntityMethod.createOrReplaceEntity(new Entity(TEST_ENTITY1_NAME, TAGS));

        //Metric data
        MetricMethod.createOrReplaceMetric(new Metric(TEST_METRIC1_NAME, TAGS));

        SeriesMethod.insertSeriesCheck(seriesList);

        //Entity groups data
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP1_NAME));
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP2_NAME));
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY1_NAME));
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP2_NAME, Collections.singletonList(TEST_ENTITY1_NAME));
    }

    /*
    #3126 issue
    Standard collection view
     */

    /**
     * #3126
     * Test will fail until #3141 won't be fixed
     */
    @Test
    public void testAlphabeticalOrder() {
        String sqlQuery = String.format(
                "SELECT tags, metric.tags, entity.tags, entity.groups FROM '%s'  %nWHERE entity = '%s'",
                TEST_METRIC1_NAME, TEST_ENTITY1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(
                        "a=b;b=a;tag=VaLue",
                        "a=b;b=a;tag=VaLue",
                        "a=b;b=a;tag=VaLue",
                        String.format("%s;%s", TEST_ENTITY_GROUP1_NAME, TEST_ENTITY_GROUP2_NAME)
                )
        );

        assertTableContainsColumnsValues(expectedRows, resultTable, "tags", "metric.tags", "entity.tags", "entity.groups");
    }

    /**
     * #3126
     */
    @Test
    public void testEmptyTags() {
        String sqlQuery = String.format(
                "SELECT tags, metric.tags, entity.tags, entity.groups FROM '%s' %nWHERE entity = '%s'",
                TEST_METRIC2_NAME, TEST_ENTITY2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("null", "null", "null", "null")
        );

        assertTableContainsColumnsValues(expectedRows, resultTable, "tags", "metric.tags", "entity.tags", "entity.groups");
    }
}
