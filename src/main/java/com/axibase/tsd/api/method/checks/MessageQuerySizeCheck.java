package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.Util;

import javax.ws.rs.core.Response;
import java.util.List;

public class MessageQuerySizeCheck extends AbstractCheck {
    private MessageQuery query;
    private Integer size;

    public MessageQuerySizeCheck(MessageQuery query, Integer size) {
        this.query = query;
        this.size = size;
    }

    @Override
    public boolean isChecked() {
        Response response = MessageMethod.queryMessageResponse(query);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            return false;
        }
        List<Message> storedMessageList = response.readEntity(ResponseAsList.ofMessages());
        return storedMessageList.size() == size;
    }
}
