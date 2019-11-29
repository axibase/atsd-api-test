package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageStats;
import com.axibase.tsd.api.model.message.MessageStatsQuery;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.ResponseAsList;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;

public class MessageQueryStatsTest extends MessageMethod {
    private static final String MESSAGE_STATS_ENTITY = Mocks.entity();
    private static final String MESSAGE_STATS_TYPE = "stats-type-1";
    private static final List<String> DATES = Arrays.asList(
            "2018-05-21T00:00:01.000Z",
            "2018-05-21T00:01:01.000Z",
            "2018-05-21T00:02:01.000Z",
            "2018-05-21T00:03:01.000Z",
            "2018-05-21T00:04:01.000Z");
    private static final String TAG_KEY = "key";
    private static final String TAG_VALUE = "value";

    @BeforeClass
    public void insertMessages() {
        Message message = new Message(MESSAGE_STATS_ENTITY, MESSAGE_STATS_TYPE)
            .setMessage("message-stats-test");
        for (String date : DATES) {
            message.setDate(date);
            message.setTags(ImmutableMap.of(TAG_KEY, TAG_VALUE));
            insertMessageCheck(message);
        }
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testNoAggregate() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);

        List<Series> messageStatsList = queryMessageStatsReturnSeries(statsQuery);

        assertEquals("Response should contain only 1 series", 1, messageStatsList.size());
        List<Sample> samples = messageStatsList.get(0).getData();
        assertEquals("Response should contain only 1 sample", 1, samples.size());
        assertEquals("Message count mismatch", new BigDecimal(DATES.size()), samples.get(0).getValue());
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testAggregateCount() {
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
    public void testAggregateDetail() {
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
    public void testAggregateUnknownRaiseError() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate(AggregationType.SUM));

        Response response = queryMessageStats(statsQuery);

        assertEquals("Query with unknown aggregate type should fail", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("2945")
    @Test
    public void testAggregateNoTypeRaiseError() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate());

        Response response = queryMessageStats(statsQuery);

        assertEquals("Query with unknown aggregate type should fail", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("6460")
    @Test(
            description = "Tests that messages are found for matching tagExpression field."
    )
    public void testTagsExpressionSelection() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagExpression("tags.key='value'");

        Response response = queryMessageStats(statsQuery);
        List<MessageStats> messageStatsList = response.readEntity(ResponseAsList.ofMessageStats());
        assertEquals(1, messageStatsList.size());
        MessageStats stats = messageStatsList.get(0);
        BigDecimal value = stats.getData().get(0).getValue();
        assertEquals(value, BigDecimal.valueOf(DATES.size())); //dates count and messages count are equal
    }

    @Issue("6460")
    @Test(
            description = "Tests that messages are not found for expression that does not match any field."
    )
    public void testTagsExpressionNoData() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagExpression("false");

        Response response = queryMessageStats(statsQuery);
        List<MessageStats> messageStatsList = response.readEntity(ResponseAsList.ofMessageStats());
        assertEquals(1, messageStatsList.size());
        MessageStats stats = messageStatsList.get(0);
        assertEquals(stats.getData().size(), 0);
    }

    @Issue("6460")
    @Test(
            description = "Tests that warning is returned if executing query with invalid expression. Error in \"lke\""
    )
    public void testTagsExpressionError() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagExpression("type lke 'something'");

        Response response = queryMessageStats(statsQuery);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<MessageStats> messageStatsList = response.readEntity(ResponseAsList.ofMessageStats());
        assertEquals(1, messageStatsList.size());
        String warning = messageStatsList.get(0).getWarning();
        assertEquals(warning, "IllegalStateException: Syntax error at line 1 position 5: no viable alternative at input 'type lke'");
    }

    @Issue("6460")
    @Test(
            description = "Tests that request is not stuck if executing query with call to non-existent field"
    )
    public void testTagsExpressionNotValidField() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setTagExpression("non_existent_key='value'");

        Response response = queryMessageStats(statsQuery);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Issue("6460")
    @Test(
            description = "Tests that OK response is returned if expression field is blank"
    )
    public void testBlankExpressionField() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setExpression("");

        Response response = queryMessageStats(statsQuery);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Issue("6460")
    @Test(
            description = "Tests that error response is returned if expression field is not blank"
    )
    public void testNotBlankExpressionField() {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY)
                .setExpression("test");

        Response response = queryMessageStats(statsQuery);
        String errorMessage = "IllegalArgumentException: The expression field is not supported";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(errorMessage, response.readEntity(LinkedHashMap.class).get("error"));
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
