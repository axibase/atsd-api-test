package com.axibase.tsd.api.method.sql.function.other;

import com.axibase.tsd.api.method.collections.NamedCollectionMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.collections.NamedCollection;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class SqlCollectionTest extends SqlTest {
    private static final String METRIC = Mocks.metric();
    private static final String TAG_KEY = "tag_key";
    private static final String TAG_VALUE = "tag_value";
    private static final NamedCollection NAMED_COLLECTION = new NamedCollection(Mocks.namedCollection(),
            Arrays.asList(
                    "val1", "val2", "val3", TAG_VALUE
            ));

    @BeforeClass
    public void prepareData() throws Exception {
        NamedCollectionMethod.insertCollection(NAMED_COLLECTION);
        Series series = new Series(Mocks.entity(), METRIC)
                .addSamples(Mocks.SAMPLE)
                .addTag(TAG_KEY, TAG_VALUE);
        SeriesMethod.insertSeriesCheck(series);
    }

    @Test(description = "Tests SELECT collection('...') clause")
    @Issue("6559")
    public void testSelect() {
        String sqlQuery = String.format("SELECT collection('%s')", NAMED_COLLECTION.getName());
        String[][] expectedResult = {
                {String.format("[\"%s\"]", String.join("\",\"", NAMED_COLLECTION.getItems()))}
        };
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Test(description = "Tests SELECT collection('...') where collection does not exist")
    @Issue("6559")
    public void testSelectNotValidCollection() {
        String nonExistentCollection = "non-existent-collection";
        String sqlQuery = String.format("SELECT collection('%s')", nonExistentCollection);
        String expectedError = String.format("Collection not found for name '%s'", nonExistentCollection);
        assertBadRequest(sqlQuery + " did not return expected error", expectedError, sqlQuery);
    }

    @Test(description = "Tests that operator IN can be applied to collection")
    @Issue("6559")
    public void testTagsInCollection() {
        String sqlQuery = String.format("SELECT metric FROM \"%s\" WHERE tags.\"%s\" IN collection('%s')", METRIC, TAG_KEY, NAMED_COLLECTION.getName());
        String[][] expectedResult = {
                {METRIC}
        };
        assertSqlQueryRows(expectedResult, sqlQuery);
    }
}
