package com.axibase.tsd.api.method;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.logging.Level;


public abstract class BaseMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String METHOD_VERSION = "/version";
    protected static TCPSender tcpSender;
    protected static ObjectMapper jacksonMapper;
    protected static WebTarget httpApiResource;
    protected static WebTarget httpRootResource;
    protected static Config config;

    static {
        java.util.logging.LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("");
        julLogger.setLevel(Level.FINEST);

        prepare();
    }

    private static void prepare() {
        try {
            config = Config.getInstance();
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.connectorProvider(new ApacheConnectorProvider());
            clientConfig.register(MultiPartFeature.class);
            clientConfig.register(new LoggingFeature());
            clientConfig.register(HttpAuthenticationFeature.basic(config.getLogin(), config.getPassword()));
            httpRootResource = ClientBuilder.newClient(clientConfig).target(UriBuilder.fromPath("")
                    .scheme(config.getProtocol())
                    .host(config.getServerName())
                    .port(config.getHttpPort())
                    .build());
            httpApiResource = httpRootResource.path(config.getApiPath());
            tcpSender = new TCPSender(config.getServerName(), config.getTcpPort());

            jacksonMapper = new ObjectMapper();
            jacksonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssXXX"));
        } catch (FileNotFoundException fne) {
            logger.error("Failed prepare BaseMethod class. Reason: {}", fne.getMessage());
            throw new RuntimeException(fne);
        }

    }

    public static boolean compareJsonString(String expected, String given) throws Exception {

        return compareJsonString(expected, given, false);
    }

    public static boolean compareJsonString(String expected, String given, boolean strict) throws Exception {
        logger.debug("===Comparing Json String===\nStrict: {}\nExpected:\n{}\nGiven:\n{}", strict, expected, given);
        try {
            JSONAssert.assertEquals(expected, given, strict ? JSONCompareMode.NON_EXTENSIBLE : JSONCompareMode.LENIENT);
            return true;
        } catch (JSONException e) {
            throw new Exception("Can not deserialize response");
        } catch (AssertionError e) {
            return false;
        }

    }

    public static String formatToJsonString(final Response response) {
        return response.readEntity(String.class);
    }

    public static int calculateJsonArraySize(String jsonArrayString) throws JSONException {
        return new JSONArray(jsonArrayString).length();
    }

    public static Response queryATSDVersion() {
        Response response = httpApiResource.path(METHOD_VERSION).request().get();
        response.bufferEntity();
        return response;
    }

    public static String extractErrorMessage(Response response) throws Exception {
        String jsonText = response.readEntity(String.class);

        JSONObject json;
        try {
            json = new JSONObject(jsonText);
        } catch (JSONException e) {
            throw new JSONException("Fail to parse response as JSON");
        }
        try {
            return json.getString("error");
        } catch (JSONException e) {
            throw new IllegalStateException("Fail to get error message from response. Perhaps response does not contain error message when it should.");
        }
    }


}
