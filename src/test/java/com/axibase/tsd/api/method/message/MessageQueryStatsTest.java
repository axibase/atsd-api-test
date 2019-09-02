package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageStatsQuery;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.*;

public class MessageQueryStatsTest extends MessageMethod {
    private final static String MESSAGE_STATS_ENTITY = Mocks.entity();
    private final static String MESSAGE_STATS_TYPE = "stats-type-1";
    private final static List<String> DATES = Arrays.asList(
            "2018-05-21T00:00:01.000Z",
            "2018-05-21T00:01:01.000Z",
            "2018-05-21T00:02:01.000Z",
            "2018-05-21T00:03:01.000Z",
            "2018-05-21T00:04:01.000Z");
    private static final String TAG_KEY = "key";
    private static final String TAG_VALUE = "value";
    private static final TypeReference<List<Series>> SERIES_LIST_TYPE_REFERENCE =
            new TypeReference<List<Series>>() {}; //Message stats are actually series, see documentation https://axibase.com/docs/atsd/api/data/messages/stats.html#response

    @BeforeClass
    public void insertMessages() throws Exception {
        Message message = new Message(MESSAGE_STATS_ENTITY, MESSAGE_STATS_TYPE);
        message.setMessage("message-stats-test");
        for (String date : DATES) {
            message.setDate(date);
            message.setTags(ImmutableMap.of(TAG_KEY, TAG_VALUE));
            insertMessageCheck(message);
        }
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testNoAggregate() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);

        List<Series> messageStatsList = queryMessageStatsReturnSeries(statsQuery);

        assertEquals("Response should contain only 1 series", 1, messageStatsList.size());
        List<Sample> samples = messageStatsList.get(0).getData();
        assertEquals("Response should contain only 1 sample", 1, samples.size());
        assertEquals("Message count mismatch", new BigDecimal(DATES.size()), samples.get(0).getValue());
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testAggregateCount() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate(AggregationType.COUNT));

        List<Series> messageStatsList = queryMessageStatsReturnSeries(statsQuery);

        assertEquals("Response should contain only 1 series", 1, messageStatsList.size());
        List<Sample> samples = messageStatsList.get(0).getData();
        assertEquals("Response should contain only 1 sample", 1, samples.size());
        assertEquals("Message count mismatch", new BigDecimal(DATES.size()), samples.get(0).getValue());
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testAggregateDetail() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate(AggregationType.DETAIL));

        List<Series> messageStatsList = queryMessageStatsReturnSeries(statsQuery);

        assertEquals("Response should contain only 1 series", 1, messageStatsList.size());
        List<Sample> samples = messageStatsList.get(0).getData();
        assertEquals("Response should contain only 1 sample", 1, samples.size());
        assertEquals("Message count mismatch", new BigDecimal(DATES.size()), samples.get(0).getValue());
    }

    @Issue("2945")
    @Test
    public void testAggregateUnknownRaiseError() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate(AggregationType.SUM));

        Response response = queryMessageStats(statsQuery);

        assertEquals("Query with unknown aggregate type should fail", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("2945")
    @Test
    public void testAggregateNoTypeRaiseError() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate());

        Response response = queryMessageStats(statsQuery);

        assertEquals("Query with unknown aggregate type should fail", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("6460")
    @Test(
            description = "Tests that messages that are found for matching tagsExpression field."
    )
    public void testTagsExpressionSelection() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagsExpression("tags.key='value'");

        Response response = queryMessageStats(statsQuery);
        Series stats = TestUtil.readFromJson(response.readEntity(String.class), SERIES_LIST_TYPE_REFERENCE).get(0);
        BigDecimal value = stats.getData().get(0).getValue();
        assertEquals(value, BigDecimal.valueOf(DATES.size())); //dates count and messages count are equal
    }

    @Issue("6460")
    @Test(
            description = "Tests that messages that not found for expression that does not match any field."
    )
    public void testTagsExpressionNoData() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagsExpression("false");

        Response response = queryMessageStats(statsQuery);
        Series stats = TestUtil.readFromJson(response.readEntity(String.class), SERIES_LIST_TYPE_REFERENCE).get(0);
        assertEquals(stats.getData().size(), 0);
    }

    @Issue("6460")
    @Test(
            description = "Tests that warning is returned if executing query with invalid expression. Error in \"lke\""
    )
    public void testTagsExpressionError() throws Exception{
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagsExpression("type lke 'something'");

        Response response = queryMessageStats(statsQuery);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String warning = TestUtil.readFromJson(response.readEntity(String.class), new TypeReference<List<Map<String, Object>>>() {})
                .get(0).get("warning").toString();
        assertEquals(warning, "IllegalStateException: Syntax error at line 1 position 5: no viable alternative at input 'type lke'");
    }

    @Issue("6460")
    @Test(
            description = "Tests that request is not stuck if executing query with call to nonexistent field. Problems were found for this case while testing. Field \"non_existent_key\" does not exist"
    )
    public void testTagsExpressionNotValidField() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagsExpression("non_existent_key='value'");

        Response response = queryMessageStats(statsQuery);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private MessageStatsQuery prepareSimpleMessageStatsQuery(String entityName) {
        MessageStatsQuery statsQuery = new MessageStatsQuery();
        statsQuery.setEntity(entityName);
        statsQuery.setType(MESSAGE_STATS_TYPE);
        statsQuery.setStartDate(MIN_QUERYABLE_DATE);
        statsQuery.setEndDate(MAX_QUERYABLE_DATE);
        return statsQuery;
    }
}
