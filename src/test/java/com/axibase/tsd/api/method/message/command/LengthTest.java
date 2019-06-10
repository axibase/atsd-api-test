package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.Severity;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;
import static com.axibase.tsd.api.util.TestUtil.getCurrentDate;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class LengthTest extends MessageMethod {
    private static final int MAX_LENGTH = 128 * 1024;

    @Issue("2412")
    @Test
    public void testMaxLength() throws Exception {
        final Message message = new Message("e-message-max-cmd-length", "t-message-max-cmd-length");
        message.setDate(getCurrentDate());
        message.setSeverity(Severity.MAJOR.name());
        String msg = "Length-Test-";
        message.setMessage(msg);
        MessageCommand command = new MessageCommand(message);

        int currentLength = command.compose().length();
        message.setMessage(msg + StringUtils.repeat('m', MAX_LENGTH - currentLength));
        command = new MessageCommand(message);
        assertEquals("Command length is not maximal", MAX_LENGTH, command.compose().length());
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2412")
    @Test
    public void testMaxLengthOverflow() throws Exception {
        final Message message = new Message("e-message-max-len-overflow", "t-message-max-len-overflow");
        message.setDate(getCurrentDate());
        message.setSeverity(Severity.MAJOR.name());
        String msg = "testMaxLengthOverflow";
        message.setMessage(msg);
        MessageCommand command = new MessageCommand(message);

        int currentLength = command.compose().length();
        message.setMessage(msg + StringUtils.repeat('m', MAX_LENGTH - currentLength + 1));
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
