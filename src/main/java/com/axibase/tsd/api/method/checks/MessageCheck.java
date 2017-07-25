package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;


public class MessageCheck extends AbstractCheck {
    private Message message;

    public MessageCheck(Message message) {
        this.message = message;
    }

    @Override
    public boolean isChecked() {
        try {
            return MessageMethod.messageExist(message);
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to check existence of message: %s",
                            e.getMessage()
                    )
            );
        }
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    public Message getMessage() {
        return message;
    }
}
