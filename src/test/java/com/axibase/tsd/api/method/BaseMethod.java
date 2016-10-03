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
import java.text.SimpleDateFormat;
import java.util.logging.Level;


public abstract class BaseMethod {
    public static final Long DEFAULT_EXPECTED_PROCESSING_TIME = 2000L;
    public static final String MIN_QUERYABLE_DATE = "1000-01-01T00:00:00.000Z";
    public static final String MAX_QUERYABLE_DATE = "9999-12-31T23:59:59.999Z";
    public static final String MIN_STORABLE_DATE = "1970-01-01T00:00:00.000Z";
    protected static final Long REQUEST_INTERVAL = 500L;
    protected static final Long UPPER_BOUND_FOR_CHECK = 10000L;
    protected static final String MAX_STORABLE_DATE = "2106-02-07T06:59:59.999Z";
    protected static final String ALERT_OPEN_VALUE = "1";
    protected static final String ENTITY_TAGS_PROPERTY_TYPE = "$entity_tags";


    private static final Logger logger = LoggerFactory.getLogger(BaseMethod.class);

    private static final String METHOD_VERSION = "/version";
    private static TCPSender tcpSender;
    private static ObjectMapper jacksonMapper;
    private static WebTarget httpApiResource;
    private static WebTarget httpRootResource;
    private static Config config;

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
            setHttpApiResource(getHttpRootResource().path(config.getApiPath()));
            setTcpSender(new TCPSender(config.getServerName(), config.getTcpPort()));

            setJacksonMapper(new ObjectMapper());
            getJacksonMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssXXX"));
        } catch (FileNotFoundException fne) {
            logger.error("Failed prepare BaseMethod class. Reason: {}", fne.getMessage());
            throw new RuntimeException(fne);
        }

    }

    public static boolean compareJsonString(String expected, String given) throws Exception {

        return compareJsonString(expected, given, false);
    }

    public static boolean compareJsonString(String expected, String given, boolean strict) throws Exception {
        logger.debug("===Comparing Json String===\nStrict: {}\nExpected:\n{}\nReceived:\n{}", strict, expected, given);
        try {
            JSONAssert.assertEquals(expected, given, strict ? JSONCompareMode.NON_EXTENSIBLE : JSONCompareMode.LENIENT);
            logger.debug("===Json strings are equal===");
            return true;
        } catch (JSONException e) {
            throw new Exception("Can not deserialize response");
        } catch (AssertionError e) {
            logger.debug("===Json strings are NOT equal===");
            return false;
        }

    }

    public static int calculateJsonArraySize(String jsonArrayString) throws JSONException {
        return new JSONArray(jsonArrayString).length();
    }

    public static Response queryATSDVersion() {
        Response response = getHttpApiResource().path(METHOD_VERSION).request().get();
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

    public static TCPSender getTcpSender() {
        return tcpSender;
    }

    public static void setTcpSender(TCPSender tcpSender) {
        BaseMethod.tcpSender = tcpSender;
    }

    public static ObjectMapper getJacksonMapper() {
        return jacksonMapper;
    }

    public static void setJacksonMapper(ObjectMapper jacksonMapper) {
        BaseMethod.jacksonMapper = jacksonMapper;
    }

    public static WebTarget getHttpApiResource() {
        return httpApiResource;
    }

    public static void setHttpApiResource(WebTarget httpApiResource) {
        BaseMethod.httpApiResource = httpApiResource;
    }

    public static WebTarget getHttpRootResource() {
        return httpRootResource;
    }

    public static void setHttpRootResource(WebTarget httpRootResource) {
        BaseMethod.httpRootResource = httpRootResource;
    }
}
