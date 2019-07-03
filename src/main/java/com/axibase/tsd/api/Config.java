package com.axibase.tsd.api;

import com.axibase.tsd.api.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Dmitry Korchagin.
 * @author raipc
 */
@Slf4j
@Getter
public class Config {
    private static final String DEFAULT_CONFIG_FILE = "client.properties";
    private static final String DEV_CONFIG_FILE = "dev.client.properties";
    private final String login;
    private final String password;
    private final String protocol;
    private final String serverName;
    private final int httpPort;
    private final int tcpPort;
    private final String apiPath;
    private final String loggerLevel;
    private final boolean checkLoggingEnable;

    public static Config getInstance() {
        return ConfigInstanceHolder.INSTANCE;
    }

    private Config(String configPath) {
        log.debug("Load client properties from file: {}", configPath);
        Properties clientProperties = new Properties();
        try (InputStream stream = new FileInputStream(configPath)) {
            clientProperties.load(stream);
        } catch (Exception e) {
            log.error("Failed to load client properties. {}", e.getMessage());
        }

        login = load("login", clientProperties, null);
        password = load("password", clientProperties, null);
        protocol = load("protocol", clientProperties, null);
        serverName = load("serverName", clientProperties, null);
        httpPort = Integer.parseInt(load("httpPort", clientProperties, null));
        tcpPort = Integer.parseInt(load("tcpPort", clientProperties, null));
        apiPath = load("apiPath", clientProperties, null);
        loggerLevel = load("loggerLevel", clientProperties, "debug");
        checkLoggingEnable = Boolean.valueOf(load("isCheckLoggingEnable", clientProperties, "false"));
        System.setProperty("loggerLevel", loggerLevel);
        if (StringUtils.isEmpty(login)) {
            throw new IllegalStateException("Empty login");
        }
        if (StringUtils.isEmpty(password)) {
            throw new IllegalStateException("Empty password");
        }
        if (StringUtils.isEmpty(serverName)) {
            throw new IllegalStateException("Empty server name");
        }
    }

    private static String load(String name, Properties clientProperties, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            value = clientProperties.getProperty(name);
            if (value == null) {
                if (defaultValue == null) {
                    log.error("Could not find required property: {}", name);
                    throw new IllegalStateException(name + " property is null");
                } else {
                    value = defaultValue;
                }
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }

    private static final class ConfigInstanceHolder {
        private static final Config INSTANCE = initializeInstance();

        private static Config initializeInstance() {
            Config config = tryInitConfig(DEV_CONFIG_FILE);
            if (config == null) {
                config = tryInitConfig(DEFAULT_CONFIG_FILE);
            }
            if (config == null) {
                throw new IllegalStateException(new FileNotFoundException("*client.properties not found"));
            }
            return config;
        }

        private static Config tryInitConfig(String config) {
            URL configUrl = Config.class.getClassLoader().getResource(config);
            if (configUrl != null) {
                log.debug("Trying to use {} for config", config);
                return new Config(configUrl.getFile());
            }
            return null;
        }
    }
}
