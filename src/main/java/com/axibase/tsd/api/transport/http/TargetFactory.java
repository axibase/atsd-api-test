package com.axibase.tsd.api.transport.http;

import com.axibase.tsd.api.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class TargetFactory {

    private static final Integer DEFAULT_CONNECT_TIMEOUT = 180000;
    private static final URI DEFAULT_URI = baseURI();
    private final Client defaultHttpClient = createClient();

    private static URI baseURI() {
        final Config config = config();
        try {
            return new URIBuilder()
                    .setScheme("http")
                    .setHost(config.getServerName())
                    .setPort(config.getHttpPort())
                    .build();
        } catch (URISyntaxException e) {
            log.error("Failed to create base URL");
            throw new IllegalStateException(e);
        }
    }

    public static TargetFactory fromConfig() {
        return new TargetFactory();
    }

    private Client createClient() {
        return ClientBuilder.newClient(clientConfig());
    }

    public WebTarget base() {
        if (defaultHttpClient != null) {
            return defaultHttpClient.target(DEFAULT_URI);
        }
        log.error("Failed to create default Http client");
        throw new IllegalStateException("Http client is not available");
    }

    public WebTarget api() {
        return base().path(config().getApiPath());
    }


    private static ClientConfig clientConfig() {
        Config config = config();
        ClientConfig clientConfig = new ClientConfig(JacksonFeature.class);
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        clientConfig.register(MultiPartFeature.class);
        clientConfig.register(HttpAuthenticationFeature.basic(config.getLogin(), config.getPassword()));
        clientConfig.property(ClientProperties.READ_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setAuthenticationEnabled(true)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(8);
        connectionManager.setValidateAfterInactivity(20000);
        connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER_SHARED, true);
        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, requestConfig);
        clientConfig.property(ApacheClientProperties.RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        return clientConfig;
    }

    private static Config config() {
        try {
            return Config.getInstance();
        } catch (FileNotFoundException e) {
            log.error("Failed to retrieve app config!");
            throw new IllegalStateException(e);
        }
    }
}
