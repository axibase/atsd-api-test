package com.axibase.tsd.api.sql;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.Method;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlExecuteMethod extends Method {

    private static final String METHOD_SQL_CONSOLE = "/sql/console";
    private CloseableHttpClient httpClient;
    private Config config;
    private static final Logger logger = LoggerFactory.getLogger(SqlExecuteMethod.class);

    private List<NameValuePair> loginFormData() {
        List<NameValuePair> loginData = new ArrayList<>();
        loginData.add(new BasicNameValuePair("login", "true"));
        loginData.add(new BasicNameValuePair("atsd_user", config.getLogin()));
        loginData.add(new BasicNameValuePair("atsd_pwd", config.getPassword()));
        loginData.add(new BasicNameValuePair("commit", "Login"));
        return loginData;
    }

    private List<NameValuePair> sqlFormData(String sqlQuery) {
        List<NameValuePair> sqlData = new ArrayList<>();
        sqlData.add(new BasicNameValuePair("sql", sqlQuery));
        sqlData.add(new BasicNameValuePair("numericPrecision", "1"));
        sqlData.add(new BasicNameValuePair("_addMetaData", "on"));
        sqlData.add(new BasicNameValuePair("jsonSchema", "STANDARD"));
        sqlData.add(new BasicNameValuePair("exportJson", "Export JSON"));
        return sqlData;
    }


    private void logIn() throws IOException {
        List<NameValuePair> loginData = loginFormData();
        HttpPost loginHttpPost = new HttpPost(buildURL() + "/login-processing");
        loginHttpPost.setEntity(new UrlEncodedFormEntity(loginData));
        httpClient.execute(loginHttpPost);
        logger.debug(httpClient.toString());
    }

    private void setUpHttpClient() {
        HttpHost target = new HttpHost(config.getServerName(), config.getHttpPort(), config.getProtocol());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(config.getLogin(), config.getPassword()));
        httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
    }

    /**
     *
     */
    public SqlExecuteMethod() throws IOException {
        config = Config.getInstance();
        setUpHttpClient();
        logIn();
    }

    /**
     * Build base url for sql queries execution
     *
     * @return full URL
     */
    private String buildURL() {
        Config config = Config.getInstance();
        return config.getProtocol() +
                "://" +
                config.getServerName() +
                ":" +
                config.getHttpPort();
    }

    /**
     * Execute a sql query on ATSD instance,
     * that described in properties file
     *
     * @param sqlQuery query to ATSD, with syntax described in docs.
     * @return HttpResponse  http responses
     * @throws IOException for incorrect url.
     * @see <a href="SQL API">https://github.com/axibase/atsd-docs/tree/master/api/sql</a>
     */
    public HttpResponse query(String sqlQuery) throws IOException {
        HttpPost sqlHttpPost = new HttpPost(buildURL() + METHOD_SQL_CONSOLE);
        List<NameValuePair> sqlFormData = sqlFormData(sqlQuery);
        sqlHttpPost.setEntity(new UrlEncodedFormEntity(sqlFormData));
        return httpClient.execute(sqlHttpPost);
    }

    public String queryAsString(String sqlQuery){
        try {
            return Util.HttpResponseTranslator.asString(query(sqlQuery));
        } catch (IOException e) {
            logger.error("Failed to execute following sql query: {} \n. {}",sqlQuery, e.getMessage());
        }
        return null;
    }

    public JSONObject queryAsJson(String sqlQuery){
        try {
            return  Util.HttpResponseTranslator.asJson(query(sqlQuery));
        } catch (IOException e) {
            logger.error("Failed to execute following sql query: {} \n. {}",sqlQuery, e.getMessage());
        } catch (JSONException e) {
            logger.error("Failed to parse HttpResponse to JSON Object", e.getMessage());
        }
        return null;
    }
}
