package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.Severity;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class LengthTest extends MessageMethod {
    private static final int MAX_LENGTH = 128 * 1024;

    @Issue("2412")
    @Test
    public void testMaxLength() throws Exception {
        final Message message = new Message("e-message-max-cmd-length", "t-message-max-cmd-length");
        message.setDate(Mocks.ISO_TIME);
        message.setSeverity(Severity.MAJOR.name());
        message.setMessage("");
        MessageCommand command = new MessageCommand(message);

        Integer currentLength = command.compose().length();

        String newMessage = new String(new char[MAX_LENGTH - currentLength]).replace("\0", "m");
        message.setMessage(newMessage);
        command = new MessageCommand(message);
        assertEquals("Command length is not maximal", command.compose().length(), MAX_LENGTH);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2412")
    @Test
    public void testMaxLengthOverflow() throws Exception {
        final Message message = new Message("e-message-max-len-overflow", "t-message-max-len-overflow");
        message.setDate(Mocks.ISO_TIME);
        message.setSeverity(Severity.MAJOR.name());
        message.setMessage("");
        MessageCommand command = new MessageCommand(message);

        Integer currentLength = command.compose().length();

        String newMessage = new String(new char[MAX_LENGTH - currentLength + 1]).replace("\0", "m");
        message.setMessage(newMessage);
        command = new MessageCommand(message);
        assertTrue("Command must have overflow length", command.compose().length() > MAX_LENGTH);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        String assertMessage = String.format(
                "Result must contain one failed command with length %s",
                command.compose().length()
        );
        assertEquals(assertMessage,
                expectedResult,
                CommandMethod.send(command)
        );
    }


}
