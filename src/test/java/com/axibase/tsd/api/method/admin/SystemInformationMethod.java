package com.axibase.tsd.api.method.admin;

import com.axibase.tsd.api.method.BaseMethod;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;

public class SystemInformationMethod extends BaseMethod {
    private static final String METHOD_SYSTEM_INFORMATION = "/admin/system-information";

    public static String getJavaVersion(){
        Response response = executeRootRequest(webTarget -> webTarget.path(METHOD_SYSTEM_INFORMATION).request().get());
        response.bufferEntity();
        String systemInformation = response.readEntity(String.class);
        return StringUtils.substringBetween(systemInformation,"<tr><td>java.version</td><td>","</td></tr>");
    }
}