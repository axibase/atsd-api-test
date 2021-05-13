package com.axibase.tsd.api.method.forecast;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;

@Slf4j
public class ForecastMethod extends BaseMethod {
    private final static String FORECAST_PATH = "/forecast/settings/edit.xhtml";

    public static Response sendForecastFormData(final MultivaluedMap<String, String> formData) {
        RequestSenderWithBasicAuthorization sender = new RequestSenderWithBasicAuthorization(
                Config.getInstance().getLogin(),
                Config.getInstance().getPassword()
        );
        Response response = sender.executeRootRequest(
                FORECAST_PATH,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                HttpMethod.POST,
                Entity.form(formData)
        );
        response.bufferEntity();
        return response;
    }
}
