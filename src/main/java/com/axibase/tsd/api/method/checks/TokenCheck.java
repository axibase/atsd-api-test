package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.util.Util;

import javax.ws.rs.core.Response;


public class TokenCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Token creation failed!";
    private String token;

    public TokenCheck(String token) {
        this.token = token;
    }

    @Override
    public boolean isChecked() {
        try {
            String url = "/admin/users/tokens/" + token;
            Response response = BaseMethod.executeRootRequest(webTarget -> webTarget.path(url)
                    .request()
                    .get());
            response.bufferEntity();
            return Response.Status.Family.SUCCESSFUL.equals(Util.responseFamily(response));
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
