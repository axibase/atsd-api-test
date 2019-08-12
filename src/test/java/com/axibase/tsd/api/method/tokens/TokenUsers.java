package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.DataProvider;

import javax.ws.rs.HttpMethod;


public class TokenUsers extends BaseMethod {
    public static final String USER_NAME = "apitokenuser_worktest";
    public static final String ADMIN_NAME = Config.getInstance().getLogin();

    static {
        String path = "/admin/users/edit.xhtml";
        String userPassword = RandomStringUtils.random(10, true, true);

        executeRootRequest(webTarget -> webTarget.path(path)
                .queryParam("enabled", "on")
                .queryParam("userBean.username", USER_NAME)
                .queryParam("userBean.password", userPassword)
                .queryParam("repeatPassword", userPassword)
                .queryParam("save", "Save")
                .queryParam("userBean.userRoles", "ROLE_API_DATA_WRITE")
                .queryParam("userBean.userRoles", "ROLE_API_META_WRITE")
                .queryParam("userBean.userRoles", "ROLE_USER")
                .queryParam("userBean.userRoles", "ROLE_ENTITY_GROUP_ADMIN")
                .queryParam("userBean.userGroups", "Users")
                .queryParam("create", "true")
                .request()
                .method(HttpMethod.POST))
                .bufferEntity();
    }

    @DataProvider
    public static Object[][] users() {
        return new String[][]{
                {ADMIN_NAME},
                {USER_NAME}
        };
    }
}
