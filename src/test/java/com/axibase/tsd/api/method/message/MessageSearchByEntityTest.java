package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.method.entity.EntityTest;
import com.axibase.tsd.api.method.property.PropertyTest;
import com.axibase.tsd.api.method.series.SeriesTest;
import com.axibase.tsd.api.model.command.*;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.ResponseAsList;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.axibase.tsd.api.transport.Transport.TCP;
import static org.testng.AssertJUnit.assertTrue;

public class MessageSearchByEntityTest {
    private final static String prefix = "com.axibase.tsd.api.method.message.messagesearchbyentitytest.";
    private final static String testDate = "2021-01-20T01:01:01Z";
    private final static String queryEndDate = "2021-01-20T01:01:02Z";

    @BeforeClass
    public static void insertData() throws Exception {
        insertEntity("entity1" , "entityTagName1", "entityTagValue1");
        insertEntity("entity2" , "entityTagName2", "entityTagValue2");
        insertEntity("entity3" , "entityTagName1", "entityTagValue1");
        insertProperty("entity1", "propertyType", "propertyTagName1", "propertyTagValue1");
        insertProperty("entity2", "propertyType", "propertyTagName1", "propertyTagValue1");
        insertSeries("entity1", "metric1");
        insertSeries("entity3", "metric1");
        insertSeries("entity2", "metric2");
        insertSeries("entity3", "metric2");
        insertMessage("entity1", "message1");
        insertMessage("entity2", "message2");
        insertMessage("entity3", "message3");
    }
    @DataProvider
    public static Object[][] testCases() {
        String metricExpr = "hasMetric('" + prefix + "metric1')";
        String propertyExpr = "property('" + prefix + "propertyType::" + prefix + "propertyTagName1') like '*propertyTagValue1'";
        String metricAndPropertyExpr = metricExpr + " and " + propertyExpr;

        return new Object[][]{
                {"name like '*entity1'", new String[]{"message1"}},
                {"name like '*entity?'", new String[]{"message1", "message2", "message3"}},
                {metricExpr, new String[]{"message1", "message3"}},
                {propertyExpr, new String[]{"message1", "message2"}},
                {metricAndPropertyExpr, new String[]{"message1"}},
                {"name like '*entity?' and " + metricAndPropertyExpr, new String[]{"message1"}},
        };
    }

    @Test(dataProvider = "testCases")
    public void test(String entityExpression, String[] messages) {
        Set<String> expectedMessages = new HashSet<>(messages.length);
        Collections.addAll(expectedMessages, messages);
        MessageQuery messageQuery = new MessageQuery()
                .setEntityExpression(entityExpression)
                .setStartDate(testDate)
                .setEndDate(queryEndDate);
        List<Message> storedMessages = MessageInsertTest
                .queryMessageResponse(messageQuery)
                .readEntity(ResponseAsList.ofMessages());
        Assert.assertEquals(storedMessages.size(), expectedMessages.size());
        Assert.assertTrue(storedMessages.stream().map(Message::getMessage).allMatch(expectedMessages::contains));
    }

    private static void insertMessage(String entity, String text) throws Exception {
        Message message = new Message(prefix + entity, false)
                .setDate(testDate)
                .setMessage(text);
        PlainCommand command = new MessageCommand(message);
        assertTrue("Fail to send message via TCP command.", TCP.send(command));
        MessageTest.assertMessageExist(message, "entity", "message");
    }

    private static void insertSeries(String entity, String metric) throws Exception {
        Series series = new Series(prefix + entity, prefix + metric, false);
        series.addSamples(Sample.ofDateInteger(testDate, 111)); // doesn't matter
        PlainCommand command = new SeriesCommand(series);
        assertTrue("Fail to send series via TCP command.", TCP.send(command));
        SeriesQuery query = new SeriesQuery(series);
        SeriesTest.assertSeriesQueryDataSize(query, 1);
    }

    private static void insertProperty(String entity, String type, String tagName, String tagValue) throws Exception {
        Property property = new Property(prefix + type, prefix + entity, false);
        property.addTag(prefix + tagName, prefix + tagValue);
        PlainCommand command = new PropertyCommand(property);
        assertTrue("Fail to send property via TCP command.", TCP.send(command));
        PropertyTest.assertPropertyTypeExist(property.getType());
    }

    private static void insertEntity(String entityName, String tagName, String tagValue) throws IOException {
        Entity entity = new Entity(prefix + entityName, Collections.singletonMap(prefix + tagName, prefix + tagValue));
        PlainCommand command = new EntityCommand(entity);
        assertTrue("Fail to send entity via TCP command.", TCP.send(command));
        EntityTest.assertEntityNameExist(entity.getName());
    }
}
