package com.axibase.tsd.api.method.message;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.Check;
import com.axibase.tsd.api.method.checks.MessageCheck;
import com.axibase.tsd.api.method.entity.EntityTest;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.Util;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.fail;

public class MessageTest extends MessageMethod {

    public static void assertMessageExisting(Message message) {
        String assertMessage = String.format(
                DefaultMessagesTemplates.MESSAGE_NOT_EXIST,
                message
        );
        assertMessageExisting(assertMessage, message, true);
    }

    public static void assertMessageExisting(String assertMessage, Message message) {
        assertMessageExisting(assertMessage, message, true);
    }

    /**
     * @param checkThatMessageEntityIsCreated If true, then get entity name from the {@param message},
     *                                 and check that ATSD has entity with this name
     *                                 before check the message itself.
     */
    public static void assertMessageExisting(String assertMessage, Message message, boolean checkThatMessageEntityIsCreated) {
        if (checkThatMessageEntityIsCreated) {
            EntityTest.assertEntityNameExist(message.getEntity());
        }
        assertMessageExists(assertMessage, message);
    }

    private static void assertMessageExists(String assertMessage, Message message) {
        try {
            Checker.check(new MessageCheck(message));
        } catch (NotCheckedException e) {
            fail(assertMessage);
        }
    }

    public static void assertMessageQuerySize(final MessageQuery query, final Integer size) {
        try {
            Checker.check(new AbstractCheck() {
                @Override
                public boolean isChecked() {
                    Response response = MessageMethod.queryMessageResponse(query);
                    if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
                        return false;
                    }
                    List<Message> messageList = response.readEntity(ResponseAsList.ofMessages());
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

    /**
     * Try to find provided message in ATSD, and check that found message has the same values of the specified
     * fields as the provided message.
     */
    public static void assertMessageExist(Message message, String... fields) {
        String assertMessage = String.format("Fail to find in ATSD message: %s", message);
        try {
            Check messageCheck = new Check(assertMessage,
                    () -> MessageMethod.messageExistWithSameFields(message, "entity", "message"));
            Checker.check(messageCheck, 10, TimeUnit.SECONDS);
        } catch (NotCheckedException e) {
            fail(assertMessage);
        }
    }

    private static final class DefaultMessagesTemplates {
        private static final String MESSAGE_NOT_EXIST = "Message: %s%n doesn't exist!";
    }
}
