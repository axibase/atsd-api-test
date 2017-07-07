package com.axibase.tsd.api.method.admin;

import com.axibase.tsd.api.method.BaseMethod;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.client.WebTarget;

public class SystemInformationMethod extends BaseMethod {
    private static final String METHOD_SYSTEM_INFORMATION = "/admin/system-information";
    private static WebTarget httpSystemInformationResource = httpRootResource
            .path(METHOD_SYSTEM_INFORMATION);

    public static String getJavaVersion(){
        String systemInformation = httpSystemInformationResource.request().get(String.class);
        return StringUtils.substringBetween(systemInformation,"<tr><td>java.version</td><td>","</td></tr>");
    }
}