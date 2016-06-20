package com.axibase.tsd.api.method;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.transport.http.HTTPClientPure;
import com.axibase.tsd.api.transport.http.HTTPSender;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;

/**
 * @author Dmitry Korchagin.
 */
public abstract class Method {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected static HTTPSender httpSender;
    protected static TCPSender tcpSender;
    protected static ObjectMapper jacksonMapper;

    @BeforeClass
    public static void prepare() {
        Config config = Config.getInstance();
        HTTPClientPure driver = new HTTPClientPure(config.getProtocol(), config.getServerName(), config.getHttpPort(), config.getLogin(), config.getPassword());
        httpSender = new HTTPSender(driver, config.getDataPath(), config.getMetadataPath());
        tcpSender = new TCPSender(config.getServerName(), config.getTcpPort());
        jacksonMapper = new ObjectMapper();
        jacksonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssXXX"));
    }

}
