package com.axibase.tsd.api.method.message;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MessageCheck;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.util.NotCheckedException;

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

    private static final class DefaultMessagesTemplates {
        private static final String MESSAGE_NOT_EXIST = "Message: %s%n doesn't exist!";
    }
}
