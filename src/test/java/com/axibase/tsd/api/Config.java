package com.axibase.tsd.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Dmitry Korchagin.
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String DEFAULT_CONFIG_FILE = "client.properties";
    private static final String DEV_CONFIG_FILE = "dev.client.properties";
    private static Config instance = null;
    private String login;
    private String password;
    private String protocol;
    private String serverName;
    private int httpPort;
    private int tcpPort;
    private String dataPath;
    private String metadataPath;

    private Config(String configPath) {
        logger.debug("Load client properties from file: {}", configPath);
        Properties clientProperties = new Properties();
        try (InputStream stream = new FileInputStream(configPath)) {
            clientProperties.load(stream);
        } catch (Exception e) {
            logger.error("Fail to load client properties");
            e.printStackTrace();
        }

        login = load("login", clientProperties, null);
        password = load("password", clientProperties, null);
        protocol = load("protocol", clientProperties, null);
        serverName = load("serverName", clientProperties, null);
        httpPort = Integer.parseInt(load("httpPort", clientProperties, null));
        tcpPort = Integer.parseInt(load("tcpPort", clientProperties, null));
        dataPath = load("dataPath", clientProperties, null);
        metadataPath = load("metadataPath", clientProperties, null);
        logger.debug(this.toString());
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Config getInstance() throws FileNotFoundException {
        if (null == instance) {
            if (tryInitConfig(DEV_CONFIG_FILE)) return instance;
            if (tryInitConfig(DEFAULT_CONFIG_FILE)) return instance;
            throw new FileNotFoundException("*client.properties not found");
        }
        return instance;
    }

    private static boolean tryInitConfig(String config) {
        URL configUrl = Config.class.getClassLoader().getResource(config);
        if (configUrl != null) {
            logger.debug("Trying to use {} for config", config);
            instance = new Config(configUrl.getFile());
            return true;
        }
        return false;
    }

    private static String load(String name, Properties clientProperties, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            value = clientProperties.getProperty(name);
            if (value == null) {
                if (defaultValue == null) {
                    logger.error("Could not find required property: {}", name);
                    throw new IllegalStateException(name + " property is null");
                } else {
                    value = defaultValue;
                }
            }
        }
        return value;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getServerName() {
        return serverName;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getMetadataPath() {
        return metadataPath;
    }

    @Override
    public String toString() {
        return "Config{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", protocol='" + protocol + '\'' +
                ", serverName='" + serverName + '\'' +
                ", httpPort=" + httpPort +
                ", tcpPort=" + tcpPort +
                ", dataPath='" + dataPath + '\'' +
                ", metadataPath='" + metadataPath + '\'' +
                '}';
    }
}
