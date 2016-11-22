package com.axibase.tsd.api.method.message;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.MessageCheck;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.util.NotCheckedException;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.Assert.fail;

public class MessageTest extends MessageMethod {
    public static void assertMessageExisting(String assertMessage, Message message) {
        try {
            Checker.check(new MessageCheck(message));
        } catch (NotCheckedException e) {
            fail(assertMessage);
        }
    }

    public static void assertMessageExisting(Message message) {
        String assertMessage = String.format(
                DefaultMessagesTemplates.MESSAGE_NOT_EXIST,
                message
        );
        assertMessageExisting(assertMessage, message);
    }


    public static void assertMessageQuerySize(final MessageQuery query, final Integer size) {
        try {
            Checker.check(new AbstractCheck() {
                @Override
                public boolean isChecked() {
                    Response response = MessageMethod.queryMessageResponse(query);
                    if (response.getStatus() != OK.getStatusCode()) {
                        return false;
                    }
                    List<Message> messageList = response.readEntity(new GenericType<List<Message>>() {
                    });
                    return messageList.size() == size;
                }
            });
        } catch (NotCheckedException e) {
            String assertMessage = String.format(
                    "Query Response array size is not equal to expected (%s).%n Query: %s",
                    size, query
            );
            fail(assertMessage);
        }
    }

    private static final class DefaultMessagesTemplates {
        private static final String MESSAGE_NOT_EXIST = "Message: %s%n doesn't exist!";
    }
}
